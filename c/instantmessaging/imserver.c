#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>

#define MAXUSR 100 // maximum number of users online at a time
#define MAXNAME 30 // maximum number of characters in a name
#define BUFFSIZ 2048 // maximum buffer size

int numOnline = 0; // number of people online

void syserr(const char *msg) { perror(msg); exit(-1); }

struct usr // a user is made up of a socket and a name
{
    int socket;
    char name[MAXNAME];
};

int main(int argc, char *argv[])
{
    if(argc != 2)
    {
        fprintf(stderr,"Usage: %s <server-port>\n", argv[0]);
        return 1;
    }
    
    int portno = atoi(argv[1]);
    int sockfd = socket(AF_INET, SOCK_STREAM, 0);
  
    if(sockfd < 0)
        syserr("can't open socket");
  
    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    if(bind(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0)
        syserr("can't bind");
  
    listen(sockfd, 5);

    char buffer[BUFFSIZ], backup[BUFFSIZ], backup2[BUFFSIZ]; // buffers and backup buffers
    struct usr u[MAXUSR]; // stores users
    int online[MAXUSR], i; // stores which indexes of the user array are online

    for(i = 0 ; i < MAXUSR ; i++) // initialize user array
    {
        online[i] = 0;
        u[i].socket = -1; 
    }

    socklen_t addrlen;
    fd_set readfds;

    int n, sd, max_sd, activity, new_socket;

    for(;;)
    {
        FD_ZERO(&readfds);
        FD_SET(sockfd, &readfds);
        max_sd = sockfd;

        for(i = 0 ; i < MAXUSR ; i++) // find which socket needs to be set
        {
            if(online[i] == 1)
            {
                sd = u[i].socket;
                
                if(sd > 0)
                    FD_SET(sd , &readfds);
                
                if(sd > max_sd)
                    max_sd = sd;
            }
        }

        activity = select(max_sd + 1 , &readfds , NULL , NULL , NULL); // select call
   
        if((activity < 0) && (errno!=EINTR)) 
            syserr("select error");

        if(FD_ISSET(sockfd, &readfds)) //new connection
        {
            if((new_socket = accept(sockfd, (struct sockaddr*)&serv_addr, (socklen_t*)&addrlen)) < 0)
                syserr("accept");
    
            n = recv(new_socket, buffer, BUFFSIZ, 0); // receive the new client's name
            if(n < 0) syserr("can't receive from client");
            else buffer[n] = '\0';

            int success = 1;
            
            for(i = 0 ; i < MAXUSR ; i++) // see if there is a spot for the client
            {
                if((online[i] == 1) && strncmp(buffer, u[i].name, BUFFSIZ) == 0)
                {
                    success = 0;
                    break;
                }
            }

            if(success == 1)
            {
                int spot = -1;

                for(i = 0 ; i < MAXUSR ; i++)
                {
                    if(online[i] == 0)
                    {
                        spot = i;
                        online[i] = 1;
                        break;
                    }
                }

                if(spot == -1) // no spot, notify client
                {
                    memset(buffer, 0, BUFFSIZ);
                    sprintf(buffer, "%s", "full");

                    if(send(new_socket, buffer, BUFFSIZ, 0) < 0)
                        syserr("can't send to server");
                }
                else // there is a spot
                {
                    numOnline++; // increment total number of clients

                    strcpy(u[spot].name, buffer);
                    u[spot].socket = new_socket;

                    memset(buffer, 0, BUFFSIZ); // send acceptance to server
                    sprintf(buffer, "%s", "accept");

                    if(send(new_socket, buffer, BUFFSIZ, 0) < 0)
                        syserr("can't send to server");

                    memset(buffer, 0, BUFFSIZ); // send number of people online to server
                    sprintf(buffer, "%d", numOnline);

                    if(send(new_socket, buffer, BUFFSIZ, 0) < 0)
                        syserr("can't send to server");

                    for(i = 0 ; i < MAXUSR ; i++) // send all online user names
                    {
                        if(online[i] == 1)
                        {
                            memset(buffer, 0, BUFFSIZ);   
                            sprintf(buffer, "join %s", u[i].name);
                            
                            if(send(new_socket, buffer, BUFFSIZ, 0) < 0)
                                syserr("can't send to server");
                        }
                    }

                    memset(buffer, 0, BUFFSIZ);
                    sprintf(buffer, "%s", "quit"); // send termination messages

                    if(send(new_socket, buffer, BUFFSIZ, 0) < 0)
                        syserr("can't send to server");

                    memset(buffer, 0, BUFFSIZ);
                    sprintf(buffer, "join %s", u[spot].name);

                    for(i = 0 ; i < MAXUSR ; i++) // notify all current online users of the new user who joined
                    {
                        if((online[i] == 1) && (i != spot))
                        {
                            if(send(u[i].socket, buffer, BUFFSIZ, 0) < 0)
                                syserr("can't send to server");
                        }
                    }
                }
            }
            else // user was instead denied
            {   
                memset(buffer, 0, BUFFSIZ);
                sprintf(buffer, "%s", "denied");

                if(send(new_socket, buffer, BUFFSIZ, 0) < 0) // notify user that he was denied due to duplicate username
                    syserr("can't send to server");
            }
        }
        else // one of the sockets for the users has sent information
        {
            for(i = 0; i < MAXUSR; i++) 
            {
                sd = u[i].socket;
                 
                if(FD_ISSET(sd , &readfds)) // for every connection that has new information
                {
                    memset(buffer, 0, BUFFSIZ);
                    n = recv(sd, buffer, BUFFSIZ, 0); // receive the information
                    
                    if(n < 1) // this happens when the client dies out of an unknown reason
                    {
                        numOnline--;
                        
                        online[i] = 0;

                        memset(buffer, 0, BUFFSIZ);
                        sprintf(buffer, "leave %s", u[i].name);

                        int j;

                        for(j = 0 ; j < MAXUSR ; j++)
                        {
                            if(online[j] == 1)
                            {
                                if(send(u[j].socket, buffer, BUFFSIZ, 0) < 0)
                                    syserr("can't send to server");   
                            }
                        }

                        close(sd);
                    }
                    else // the client is good
                    {
                        buffer[n] = '\0';
                        memset(backup, 0, BUFFSIZ);
                        
                        if(strncmp(buffer, "msg @ all", 9) == 0) // if message was to broadcast all
                        {
                            char* token = strtok(buffer, "\n");
                            token = strtok(NULL, "");

                            sprintf(backup, "msg @ %s\n", u[i].name); // compose the message
                            strcat(backup, token);

                            int j;

                            for(j = 0 ; j < MAXUSR ; j++) // send the message to all online users
                            {
                                if(online[j] == 1 && u[j].socket != sd)
                                {
                                    if(send(u[j].socket, backup, BUFFSIZ, 0) < 0)
                                        syserr("can't send to server");   
                                }
                            }

                        }
                        else // if message is for only some people only
                        {
                            memset(backup, 0, BUFFSIZ); // compose the message
                            sprintf(backup, "msg @ %s\n", u[i].name);

                            strcpy(backup2, buffer);

                            char* message = strtok(backup2, "\n");
                            message = strtok(NULL, "");

                            strcat(backup, message);

                            char* receivers = strtok(buffer, "\n");
                            char* token = strtok(receivers, " ");
                            token = strtok(NULL, " ");
                            token = strtok(NULL, "@,");

                            int j;

                            while(token != NULL) // send to all users who are designated receivers
                            {
                                for(j = 0 ; j < MAXUSR ; j++)
                                {
                                    if(online[j] == 1 && u[j].socket != sd && strcmp(token, u[j].name) == 0)
                                    {
                                        if(send(u[j].socket, backup, BUFFSIZ, 0) < 0) // send username
                                            syserr("can't send to server");   
                                    }
                                }

                                token = strtok(NULL, "@,");
                            }
                        }
                    }
                }
            }
        }
    }
  
    close(sockfd);
    return 0;
}