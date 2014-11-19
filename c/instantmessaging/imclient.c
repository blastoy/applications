#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <errno.h>

#define MAXUSR 100 // maximum number of users online at a time
#define MAXNAME 30 // maximum number of characters in a name
#define BUFFSIZ 2048 // maximum buffer size

int numOnline = 0; // number of people online
char *name; // global name of the client

void syserr(const char* msg) { perror(msg); exit(-1); }

int userExists(char* s, char u[MAXUSR][MAXNAME]) // returns true if the user exists, false if not
{
    int i;

    for(i = 0 ; i < MAXUSR ; i++)
        if(strcmp(u[i], s) == 0)
            return 1;

    return 0;
}

void printOnlineUsers(int online[MAXUSR], char u[MAXUSR][MAXNAME]) // prints online users
{
    printf("Connected (%d): ", numOnline);

    int i;

    for(i = 0 ; i < MAXUSR ; i++)
        if(online[i] == 1)
            printf("%s, ", u[i]);
    
    printf("\n");
}

int processSERVER(int sockfd, char buffer[BUFFSIZ], int online[MAXUSR], char u[MAXUSR][MAXNAME]) // process server input
{
    int n = recv(sockfd, buffer, BUFFSIZ, 0), i; 
            
    if(n < 0)
        syserr("can't receive from client");
    else if(n == 0)
    {
        printf("The server has disconnected!\n");
        return -1;
    }
    else
        buffer[n] = '\0';             
    
    if(buffer[0] == 'j') // someone has joined the server
    {
        numOnline++;

        for(i = 0 ; i < MAXUSR ; i++)
        {
            if(online[i] == 0)
            {
                online[i] = 1;

                char *tok = (char*)strtok(buffer, " ");
                tok = strtok(NULL, " ");

                memset(u[i], 0, MAXNAME);
                strcpy(u[i], tok);

                printf("%s joined!\n", u[i]);
                break;
            }
        }

        printOnlineUsers(online, u);
    }
    else if(buffer[0] == 'l') // someone has left the server
    {
        numOnline--;

        char *t = (char*)strtok(buffer, " ");
        t = strtok(NULL, " ");

        for(i = 0 ; i < MAXUSR ; i++)
        {
            if(strcmp(u[i], t) == 0)
            {
                online[i] = 0;

                printf("%s disconnected!\n", u[i]);
                break;
            }
        } 

        printOnlineUsers(online, u);
    } 
    else // a message from the server that needs to be printed
    {
        char* token = strtok(buffer, " ");
        token = strtok(NULL, " ");
        token = strtok(NULL, "\n");

        printf("@%s:", token);

        token = strtok(NULL, "");

        if(token[0] == ' ')
            printf("%s\n", token);
        else
            printf(" %s\n", token);
    }

    return 1;
}

int processSTDIN(int sockfd, char buffer[BUFFSIZ], char u[MAXUSR][MAXNAME]) // process stdin input
{
    char backup[BUFFSIZ], backup2[BUFFSIZ];

    fgets(buffer, BUFFSIZ, stdin);
    buffer[strlen(buffer) - 1] = '\0';

    if(strncmp(buffer, "quit", 4) == 0)
        return -1;

    if(strncmp(buffer, "@all:", 5) == 0)
    {
        memset(backup, 0, BUFFSIZ);
        sprintf(backup, "%s", "msg @ all\n");

        char *token = strtok(buffer, ":");
        token = strtok(NULL, "");

        strcat(backup, token);

        if(send(sockfd, backup, BUFFSIZ, 0) < 0) // send message
            syserr("can't send to server");

        return 1;
    }
    else if(strncmp(buffer, "@", 1) == 0) //@user1,user2,user3: message
    {
        int formatted = 0, i; // compose the message from STDIN to server-side understandable text

        for(i = 0 ; i < strlen(buffer) ; i++)
            if(buffer[i] == ':')
                formatted = 1;

        if(!formatted)
            goto FAIL;

        memset(backup, 0, BUFFSIZ);
        sprintf(backup, "%s", "msg @ ");

        memset(backup2, 0, BUFFSIZ);
        strcpy(backup2, buffer);

        char* receivers = strtok(buffer, ":");
        
        char* token = strtok(receivers, "@,");
        
        if(!userExists(token, u)) // make sure users to send to exists
            printf("%s is not online!\n", token);
        else if(strcmp(name, token) == 0)
            printf("You cannot send a message to yourself!\n");
        else
            strcat(backup, token);

        token = strtok(NULL, "@,");

        while(token != NULL)
        {
            if(!userExists(token, u))
                printf("%s is not online!\n", token);
            else if(strcmp(name, token) == 0)
                printf("You cannot send a message to yourself!\n");
            else
            {
                strcat(backup, ",");
                strcat(backup, token);       
            }

            token = strtok(NULL, "@,");
        }

        strcat(backup, "\n");
        
        token = strtok(backup2, ":");
        token = strtok(NULL, "");
        
        strcat(backup, token);

        if(send(sockfd, backup, BUFFSIZ, 0) < 0) // send message
            syserr("can't send to server");

        return 1;
    }

    FAIL:

    printf("Please enter '@user1,user2: message', '@all: message', or 'quit'.\n");    
    return 1;
}

void initializeUsers(int sockfd, char buffer[BUFFSIZ], int online[MAXUSR], char u[MAXUSR][MAXNAME]) // initializes users
{
    int i;

    int n = recv(sockfd, buffer, BUFFSIZ, 0); // get a message from the server    
    if(n < 0) syserr("can't receive from client");
    else buffer[n] = '\0';

    numOnline = atoi(buffer);

    memset(buffer, 0, BUFFSIZ);

    for(;;)
    {
        int n = recv(sockfd, buffer, BUFFSIZ, 0); // get a message from the server    
        if(n < 0) syserr("can't receive from client");
        else buffer[n] = '\0';

        if(buffer[0] == 'q') // if 'quit' then we finished getting all online users
            break;

        char *token = (char*)strtok(buffer, " "); // tokenize the name
        token = strtok(NULL, " ");
        
        for(i = 0 ; i < MAXUSR ; i++)
        {
            if(online[i] == 0) // find a spot to put the new user in
            {
                memset(u[i], 0, MAXNAME);
                strcpy(u[i], token); // put the user there
                online[i] = 1;
                break;
            }
        }
    }
}

void chat(int sockfd, char buffer[BUFFSIZ], int online[MAXUSR], char u[MAXUSR][MAXNAME])
{
    initializeUsers(sockfd, buffer, online, u); // get all users from the server
    printOnlineUsers(online, u); // print the users

    fd_set readfds;
    int activity;

    for(;;)
    {
        FD_ZERO(&readfds);
        FD_SET(STDIN_FILENO, &readfds);
        FD_SET(sockfd, &readfds);

        activity = select(sockfd + 1 , &readfds , NULL , NULL , NULL);
   
        if((activity < 0) && (errno!=EINTR)) 
            syserr("select error");

        memset(buffer, 0, BUFFSIZ);

        if(FD_ISSET(STDIN_FILENO, &readfds)) // if the stdin has input
        {
            if(processSTDIN(sockfd, buffer, u) < 0) // process, break if 'quit' (function returns -1)
                break;
        }
        if(FD_ISSET(sockfd, &readfds)) // if the server has sent a message
        {
            if(processSERVER(sockfd, buffer, online, u) < 0) // process, break if server disconnects (function returns -1)
                break;
        }
    }
}

int main(int argc, char* argv[])
{
    int i;

    name = argv[3]; // globalize the name of the client

    if(argc != 4) // not enough arguments
    {
        fprintf(stderr, "Usage: %s <server-ip> <server-port> <client-username>\n", argv[0]);
        return 1;
    }
    
    if(strcmp(name, "all") == 0) // name is 'all'
    {
        fprintf(stderr, "You cannot use 'all' as a username.\n");
        return 1;
    }

    for(i = 0 ; i < strlen(name) ; i++) // name contains ':' or ','
    {
        if(name[i] == ':' || name[i] == ',' || name[i] == '@')
        {
            fprintf(stderr, "You cannot use '@', ',' or ':' symbols in your username.\n");
            return 1;
        }
    }

    struct hostent* server = gethostbyname(argv[1]);
    
    if(!server)
    {
        fprintf(stderr, "ERROR: no such host: %s\n", argv[1]);
        return 1;
    }
  
    int portno = atoi(argv[2]);
    int sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    
    if(sockfd < 0)
        syserr("can't open socket");

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr = *((struct in_addr*)server->h_addr);
    serv_addr.sin_port = htons(portno);

    if(connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0)
        syserr("can't connect to server");

    char buffer[BUFFSIZ]; // buffer that handles general data
    char u[MAXUSR][MAXNAME]; // the client buffer that contains clients
    int online[MAXUSR]; // the online array that dictates who is online

    for(i = 0 ; i < MAXUSR ; i++) // initialize to 0
        online[i] = 0;

    strncpy(buffer, name, BUFFSIZ);
    
    if(send(sockfd, buffer, BUFFSIZ, 0) < 0) // send the name to the server
        syserr("can't send to server");

    int n = recv(sockfd, buffer, BUFFSIZ, 0); // receive response
    
    if(n < 0)
        syserr("can't receive from client");
    else
        buffer[n] = '\0';

    if(buffer[0] == 'f') // if 'fail' then the server is full
    {
        fprintf(stderr, "Sorry, only %d users at once are allowed to chat.\n", MAXUSR);
        return 1;
    }
    else if(buffer[0] == 'a') // if 'accept' then go to the next step
    {
        chat(sockfd, buffer, online, u);
    }
    else
    {
        fprintf(stderr, "You were rejected, due to duplicate username.\n"); // anything else means the username is taken
        return 1;
    }

    close(sockfd);
    return 0;
}
