#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/time.h>

#define CHUNK 1024
#define WINSIZ 100
#define IPSIZE 40

struct packet // header to be sent to the server
{
  char ip[IPSIZE];
  uint16_t linesize;
  uint32_t filsiz;
  uint16_t portnum;
  uint32_t seqnum;
  uint16_t checksum;
  char data[CHUNK];
};

struct ack // ack packet to be received from the server
{
  uint32_t acknum;
  uint16_t checksum;
};

void syserr(const char* msg) { perror(msg); exit(-1); }

unsigned char checksum8(char *buf, int size) // checksum function by Jason Liu
{
  unsigned int sum = 0;
  int i;

  for(i = 0 ; i < size ; i++)
  {
    sum += buf[i] & 0xff;
    sum = (sum >> 8) + (sum & 0xff);
  }

  return ~sum;
}

int main(int argc, char* argv[])
{
  int sockfd, portno, n;
  struct hostent* server;
  struct sockaddr_in serv_addr;
  socklen_t addrlen;
  char ipadd[IPSIZE];

  if(argc != 4) // make sure four arguments are given
  {
    fprintf(stderr, "Usage: %s <hostname> <port> <file-name>\n", argv[0]);
    return 1;
  }
  
  server = gethostbyname(argv[1]);
  
  if(!server) // check if server exists
  {
    fprintf(stderr, "ERROR: no such host: %s\n", argv[1]);
    return 2;
  }

  portno = atoi(argv[2]);

  if(portno == 0) // make sure port number is valid
  {
    printf("Please enter a valid port number!\n");
    exit(-1);
  }

  struct in_addr **addr_list; 
  addr_list = (struct in_addr **)server->h_addr_list;
  
  int i;

  for(i = 0; addr_list[i] != NULL; i++) 
    strncpy(ipadd, inet_ntoa(*addr_list[i]), sizeof(ipadd)); 

  sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
  if(sockfd < 0) syserr("can't open socket");
  
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr = *((struct in_addr*)server->h_addr);
  serv_addr.sin_port = htons(portno);
  addrlen = sizeof(serv_addr);

  FILE *fp = fopen(argv[3], "rb");

  if(fp == NULL)
    syserr("Could not open the file for writing!"); // throw exception if file cannot be opened

  fseek(fp, 0L, SEEK_END);
  uint32_t fs = ftell(fp); // file size
  fseek(fp, 0L, SEEK_SET);

  struct packet queue[WINSIZ];
  struct ack a;

  struct timeval start;
  struct timeval end;

  long sn = 0;

  if(fs < 1)
  {
    printf("The file cannot be empty!\n");
    exit(-1);
  }

  gettimeofday(&start, NULL);

  for(i = 0 ; i < WINSIZ ; i++) // populate initial window size
  {
    bzero(queue[i].data, sizeof(queue[i].data));

    if((n = fread(queue[i].data, 1, 1024, fp)) < 1)
      break;
  
    queue[i].linesize = htons(n);
    strncpy(queue[i].ip, ipadd, strlen(ipadd));
    queue[i].portnum = htons(portno);
    queue[i].seqnum = htonl(sn++);
    queue[i].filsiz = htonl(fs);
    queue[i].checksum = htons(0);
    queue[i].checksum = htons(checksum8((char*)&queue[i], sizeof(queue[i])));
  }

  int s = 0;
  int t = i - 1;
  long lps = 0, fsTotal = 0, fsCount = 0;

  fd_set readfds, masterfds;
  struct timeval timeout;

  for(;;) // main client logic loop
  {
    LOOP:

    timeout.tv_sec = 0;
    timeout.tv_usec = 10;

    FD_ZERO(&masterfds);
    FD_SET(sockfd, &masterfds); 

    memcpy(&readfds, &masterfds, sizeof(fd_set));

    select(sockfd + 1, &readfds, NULL, NULL, &timeout);

    if(FD_ISSET(sockfd, &readfds)) // if the socket has information
    {
      
      n = recvfrom(sockfd, &a, sizeof(a), 0, (struct sockaddr*)&serv_addr, &addrlen); // read ack
      if(n < 0) syserr("can't receive from client");

      int packetchecksum = ntohs(a.checksum);
      a.checksum = 0;
      int ourchecksum = checksum8((char*)&a, sizeof(a));

      if(packetchecksum != ourchecksum)
        goto LOOP;
    }
    else // timeout occured: send window again
    {
      printf("RESENDING WINDOW!\n");

      i = s; 

      for(;;)
      {
        if(ntohl(queue[i].seqnum) != -1)
        {
          n = sendto(sockfd, (void*)&queue[i], sizeof(queue[i]), 0, (struct sockaddr*)&serv_addr, addrlen);
          if(n < 0) syserr("can't send to server");
          printf("->[%u]\n", ntohl(queue[i].seqnum));
        }

        i++;

        if(i == (t + 1))
          break;

        if(i == WINSIZ)
          i = 0;
      }

      goto LOOP;
    }

    if(ntohl(a.acknum) == -1) // ack -1 means the server is done
      goto END;

    if(ntohl(a.acknum) > lps)
    {
      long delta = ntohl(a.acknum) - lps; // how much the window should move (depends on ack)

      for(i = 0 ; i < delta ; i++)
      {
        t++; // pointer logic for circular queues
        s++;
        lps++;

        if(t == WINSIZ)
          t = 0;

        if(s == WINSIZ)
          s = 0;

        bzero(queue[t].data, sizeof(queue[t].data));

        if((n = fread(queue[t].data, 1, 1024, fp)) > 0) // send as you move
        {
          queue[t].linesize = htons(n);
          queue[t].seqnum = htonl(sn++);
          queue[t].filsiz = htonl(fs);
          queue[t].checksum = htons(0);
          queue[t].checksum = htons(checksum8((char*)&queue[t], sizeof(queue[t])));
          
          n = sendto(sockfd, (void*)&queue[t], sizeof(queue[t]), 0, (struct sockaddr*)&serv_addr, addrlen);
          if(n < 0) syserr("can't send to server");
          printf("->[%u]\n", ntohl(queue[t].seqnum));
        }
        else
          queue[t].seqnum = htonl(-1);
      }
    }
  }

  END:

  gettimeofday(&end, NULL);

  long seconds  = end.tv_sec  - start.tv_sec;
  long useconds = end.tv_usec - start.tv_usec;

  float elapsed = (((seconds) * 1000 + useconds/1000.0) + 0.5)/1000; // calculate throughput

  printf("The size is %u bytes.\nThe time is %f seconds.\n", fs, elapsed);
  printf("The throughput is therefore %d bits per second.\n", (int)((fs * 8)/(elapsed)));

  fclose(fp);
  close(sockfd);
  return 0;
}
