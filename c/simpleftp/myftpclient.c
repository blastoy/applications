#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <dirent.h>

#define MAX_BUFF 256

char buffer[MAX_BUFF];

void syserr(char const *msg) 
{ 
  perror(msg); 
  exit(-1); 
}

void processcmd(char* servname, int servport, int sockfd)
{
  int n;

  if(send(sockfd, buffer, strlen(buffer), 0) < 0) // send user command to server
      syserr("Can't send to the server");

  if(strncmp("ls-remote", buffer, 10) == 0) // if the command sent was an ls-remote
  {
    unsigned long dirsiz, filsiz;

    if(recv(sockfd, &dirsiz, sizeof(uint32_t), 0) < 0) // receive size of the remote directory
      syserr("Can't receive from the server");

    dirsiz = ntohl(dirsiz);

    if(dirsiz == 0)
    {
      printf("Can't view remote directory!\n");
    }
    else
    {
      printf("Files at the server (%s):\n", servname); 

      unsigned long count = 0;

      for(;;)
      {
        if(recv(sockfd, &filsiz, sizeof(uint32_t), 0) < 0) // get the length of the directory entry name
          syserr("Can't receive from the server");

        filsiz = ntohl(filsiz);

        if(recv(sockfd, buffer, filsiz, 0) < 0)
          syserr("Can't receive from the server"); // print directory element
        else
          buffer[filsiz] = '\0';

        printf("%s\n", buffer);

        if(++count == dirsiz) // break once count has reached the directoy size
          break;
      }
    }
  }
  else if(strncmp("get ", buffer, 4) == 0) // if the command is get
  {
    FILE *fp;

    char *fn = strtok(buffer, " "); // get the file name inside the command
    fn = strtok(NULL, " ");   

    char *fns = (char *)malloc(strlen(fn) + 1); // store a copy of the file name
    strcpy(fns, fn); 

    if(recv(sockfd, buffer, sizeof(buffer), 0) < 0) // receive the file size from the server
      syserr("Can't receive from server");

    int fsiz = atoi(buffer);
    int count = 0;

    if(fsiz == -1) // possible case: file does not exist
    {
      printf("The file does not exist!\n");
    }
    else
    {
      fp = fopen(fns, "w");

      if(fp == NULL) 
        syserr("File cannot be opened");

      if(fsiz != 0) // possible case: file can be empty or non-empty
      {
        for(;;)
        {
          memset(buffer, 0, sizeof(buffer));

          int n = recv(sockfd, buffer, sizeof(buffer), 0);

          if(n == 0)
            break;
          else if(n < 0)
            syserr("Can't receive from the server");

          if(fwrite(buffer, sizeof(char), n, fp) < n) // write the contents of the file in the server
            syserr("Writing to the file failed");

          if((count += n) == fsiz) // once we write the size of the file, we are done
            break;
        }
      }

      fclose(fp);
      printf("Retrieve '%s' from remote server: successful\n", fns);    
    }
  }
  else if(strncmp("put ", buffer, 4) == 0) // if the command is put
  {
    if(recv(sockfd, buffer, sizeof(buffer), 0) < 0) // receive ready from server
      syserr("Can't receive from the server");

    char *fn = strtok(buffer, " "); // get the file name of the put
    fn = strtok(NULL, " ");

    char *fns = (char *)malloc(strlen(fn) + 1); // copy the name 
    strcpy(fns, fn);

    FILE *fp = fopen(fn, "r");

    int sz;

    if(fp == NULL) // calculate the size, -1 if it cannot be opened
      sz = -1;
    else
    {
      fseek(fp, 0L, SEEK_END);
      sz = ftell(fp);
      fseek(fp, 0L, SEEK_SET);
    }

    sprintf(buffer, "%d", sz);

    if(send(sockfd, buffer, sizeof(buffer), 0) < 0) // send file size
      syserr("Can't send to to the server");

    if(sz == -1)
    {
      printf("The file does not exist!\n");
    }
    else if(sz != 0)
    {
      for(;;)
      {
        memset(buffer, 0, sizeof(buffer));

        int fbs = fread(buffer, sizeof(char), sizeof(buffer), fp); // read the file contents

        if(fbs == 0)
          break;
        else if(fbs < 1)
          syserr("Could not read the file");

        if(send(sockfd, buffer, fbs, 0) < 0) // send the file contents to the server
          syserr("Can't send to the server");
      }

      printf("Upload '%s' to remote server: successful\n", fns); // file contents were send sucessfully
    } 
  }
  else
  {
    if(recv(sockfd, buffer, sizeof(buffer), 0) < 0) // if the command is random and unsupported
      syserr("Can't receive from the server");

    printf("%s\n", buffer);
  }
}

int main(int argc, char *argv[])
{
  if(argc != 3) 
  {
    fprintf(stderr, "Usage: %s <hostname> <port>\n", argv[0]);
    return 1;
  }

  struct hostent* server = gethostbyname(argv[1]);
  
  if(!server) 
  {
    fprintf(stderr, "ERROR: No such host: %s\n", argv[1]);
    return 2;
  }

  int portno = atoi(argv[2]);
  int sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
  
  if(sockfd < 0) 
    syserr("Can't open the socket");

  struct sockaddr_in serv_addr;
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr = *((struct in_addr*)server->h_addr);
  serv_addr.sin_port = htons(portno);

  if(connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0)
    syserr("Can't connect to the server");

  for(;;)
  {
    memset(buffer, 0, sizeof(buffer)); 

    printf("%s:%d> ", argv[1], portno);
    
    fgets(buffer, MAX_BUFF - 1, stdin); // receive a command from the user
    int n = strlen(buffer); 
   
    if(n > 0 && buffer[n - 1] == '\n') 
      buffer[n - 1] = '\0';

    if(strncmp("exit", buffer, 4) == 0) // if exit, close the client
    {
      send(sockfd, buffer, strlen(buffer), 0);
      break;
    }
    else if(strncmp("ls-local", buffer, 9) == 0) // if ls-local, print directory
    {
      DIR *dir;
      struct dirent *ent;

      if((dir = opendir(".")) == NULL) 
        printf("Could not view the directory.\n");
      else
      {
        printf("Files at the client:\n");

        while((ent = readdir(dir)) != NULL)
          printf("%s\n", ent->d_name);
      }

      closedir(dir);
      continue;
    }   

      processcmd(argv[1], portno, sockfd); // process more complex commands
  }

  printf("Connection to server %s:%d terminated. Bye now!\n", argv[1], portno);
  close(sockfd);
  return 0;
}