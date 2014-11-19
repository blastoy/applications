#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <dirent.h>

#define DEFAULT_PORT 5555
#define MAX_BUFF 256

char buffer[MAX_BUFF];

void syserr(const char *msg) 
{ 
  perror(msg); 
  exit(-1); 
}

unsigned long getdirsiz() // returns directory size
{
  DIR *dir;
  struct dirent *ent;

  unsigned long count = 0;

  if((dir = opendir(".")) == NULL) 
    return 0;
  else
    while((ent = readdir(dir)) != NULL)
      count++;

  closedir(dir);

  return count;
}

void handleclient(int newsockfd)
{
  int n;
  printf("New connection established!\n");

  for(;;)
  {
    memset(buffer, 0, sizeof(buffer));  

    if(recv(newsockfd, buffer, sizeof(buffer), 0) < 0) 
      syserr("Can't receive from the client"); 
    
    if(strncmp("exit", buffer, 4) == 0)
      break;

    else if(strncmp("ls-remote", buffer, 10) == 0) // if command is ls-remote
    {
      DIR *dir;
      struct dirent *ent;

      unsigned long dirsiz = getdirsiz(); // calculate the directory of the remote server
      
      if(dirsiz == 0) // if 0, return 0
      {
        dirsiz = htonl(dirsiz);

        if(send(newsockfd, &dirsiz, sizeof(uint32_t), 0) < 0)
          syserr("Can't send to the client");
      }
      else
      {
        dirsiz = htonl(dirsiz);

        if(send(newsockfd, &dirsiz, sizeof(uint32_t), 0) < 0)
          syserr("Can't send to the client");

        dir = opendir(".");
        
        while((ent = readdir(dir)) != NULL) // for every directory entry
        {
          memset(buffer, 0, sizeof(buffer)); 

          sprintf(buffer, "%s", ent->d_name);
          unsigned long filsiz = htonl(strlen(buffer));

          if(send(newsockfd, &filsiz, sizeof(uint32_t), 0) < 0) // send the entry name length
            syserr("Can't send to the client");       

          if(send(newsockfd, buffer, strlen(buffer), 0) < 0) // send the entry
            syserr("Can't send to the client");
        }

        closedir(dir); 
      }
    }
    else if(strncmp("get ", buffer, 4) == 0) // if command is get
    {
      char *fn = strtok(buffer, " ");
      fn = strtok(NULL, " ");

      FILE *fp = fopen(fn, "r"); // open a file for reading

      int sz;

      if(fp == NULL) // return size, -1 if does not exist
        sz = -1;
      else
      {
        fseek(fp, 0L, SEEK_END);
        sz = ftell(fp);
        fseek(fp, 0L, SEEK_SET);
      }

      sprintf(buffer, "%d", sz);

      if(send(newsockfd, buffer, sizeof(buffer), 0) < 0)
        syserr("Can't send to the client");

      if(sz > 0) // case that the file is not empty
      {
        for(;;)
        {
          memset(buffer, 0, sizeof(buffer));

          int fbs = fread(buffer, sizeof(char), sizeof(buffer), fp);

          if(fbs == 0)
            break;
          else if(fbs < 1)
            syserr("Could not read the file");

          if(send(newsockfd, buffer, fbs, 0) < 0) // send buffered content of the file
            syserr("Can't send to the client");
        }
      }    
    }
    else if(strncmp("put ", buffer, 4) == 0) // if command is put
    {
      FILE *fp;

      if(send(newsockfd, buffer, sizeof(buffer), 0) < 0)
        syserr("Can't send to the client");

      char *fn = strtok(buffer, " "); // get the file name from the command
      fn = strtok(NULL, " "); 

      char *fns = (char *)malloc(strlen(fn) + 1); // save a copy
      strcpy(fns, fn);    

      if(recv(newsockfd, buffer, sizeof(buffer), 0) < 0) 
        syserr("Can't receive from the server");

      int fsiz = atoi(buffer);
      int count = 0;

      if(fsiz != -1) // calculate size of the file, -1 if invalid
      {
        fp = fopen(fns, "w");

        if(fp == NULL) 
          syserr("File cannot be opened");

        if(fsiz != 0)
        {
          for(;;)
          {
            memset(buffer, 0, sizeof(buffer));

            int n = recv(newsockfd, buffer, sizeof(buffer), 0);

            if(n == 0)
              break;
            if(n < 0)
              syserr("Can't receive from the server");
            
            if(fwrite(buffer, sizeof(char), n, fp) < n)
              syserr("Writing to the file failed");

            if((count += n) == fsiz)
              break;
          }
        }
            
        fclose(fp);
      }
    }
    else // command not supported
    {
      strcpy(buffer, "This command is not supported yet.");
      
      if(send(newsockfd, buffer, sizeof(buffer), 0) < 0)  
        syserr("Can't send to the client");
    }
  }

  printf("Connection closed.\n"); // child is done with the connection
  close(newsockfd);
}

int main(int argc, char *argv[])
{
  int portno = DEFAULT_PORT;

  if(argc == 2) 
    portno = atoi(argv[1]);

  if(portno == 0)
    syserr("Could not bind to the given port");

  int sockfd = socket(AF_INET, SOCK_STREAM, 0); 
  
  if(sockfd < 0) 
    syserr("Can't open the socket"); 
  
  struct sockaddr_in serv_addr, clt_addr;
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(portno);

  if(bind(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0) 
    syserr("Can't bind");

  printf("Server is running...\n");

  listen(sockfd, 5);

  pid_t pid; 

  for(;;)
  {
    socklen_t addrlen = sizeof(clt_addr); 
    int newsockfd = accept(sockfd, (struct sockaddr*)&clt_addr, &addrlen);
    
    if(newsockfd < 0) 
      syserr("Can't accept"); 

    pid = fork(); // fork a new child to process the client

    if(pid == 0)
    {
      handleclient(newsockfd); // handle the commmand given by the client
      break;
    }
  }

  if(pid != 0) // client cannot close the sockfd
    close(sockfd); 

  return 0;
}
