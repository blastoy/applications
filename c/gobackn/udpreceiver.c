#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/time.h>

#define CHUNK 1024
#define WINSIZ 100
#define IPSIZE 40

struct packet // the packet header received from the client
{
  char ip[IPSIZE];
  uint16_t linesize;
  uint32_t filsiz;
  uint16_t portnum;
  uint32_t seqnum;
  uint16_t checksum;
  char data[CHUNK];
};

struct ack // ack header to send to the client
{
  uint32_t acknum;
  uint16_t checksum;
};

void syserr(const char* msg) { perror(msg); exit(-1); }

unsigned char checksum8(char *buf, int size) // checksum implementation by Jason Liu
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

int main(int argc, char *argv[])
{
  int sockfd, portno, n;
  struct sockaddr_in serv_addr, clt_addr; 
  socklen_t addrlen;
  char clientIP[IPSIZE];
  int clientPort;

  if(argc != 3) 
  { 
    fprintf(stderr,"Usage: %s <port> <file-name>\n", argv[0]);
    return 1;
  } 
  
  portno = atoi(argv[1]);
  
  if(portno == 0) // make sure port number is valid
  {
    printf("Please enter a valid port number!\n");
    exit(-1);
  }

  sockfd = socket(AF_INET, SOCK_DGRAM, 0); 
  if(sockfd < 0) syserr("can't open socket"); 
  
  memset(&serv_addr, 0, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(portno);
  addrlen = sizeof(clt_addr);

  if(bind(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0) 
    syserr("can't bind");
  
  FILE *fp = fopen(argv[2], "wb");

  if(fp == NULL)
    syserr("Could not open the file for writing!"); // throw exception if file cannot be opened

  struct packet p;
  struct ack a;
  
  long fsTotal = 0, fsCount = 0, pkn = 0;
  int once = 1, once2 = 1;

  struct timeval start;
  struct timeval end;

  for(;;)
  {
    bzero(p.data, sizeof(p.data));

    n = recvfrom(sockfd, &p, sizeof(p), 0, (struct sockaddr*)&clt_addr, &addrlen); // get a seqno
    if(n < 0) syserr("can't receive from client"); 

    if(once == 1)
    {
      gettimeofday(&start, NULL); // start timer
      once = 0;
    }

    int packetchecksum = ntohs(p.checksum);
    p.checksum = 0;
    int ourchecksum = checksum8((char*)&p, sizeof(p));

    if(packetchecksum == ourchecksum)
    {
      if(htonl(p.seqnum) == pkn) // if everything is okay and seqno is in order
    	{
	      pkn++;

        if(once2 == 1)
        {
          fsTotal = ntohl(p.filsiz);
          strncpy(clientIP, p.ip, strlen(p.ip));
          clientPort = ntohs(p.portnum);
          once2 = 0;
        }

        fwrite(p.data, 1, ntohs(p.linesize), fp); // write to the file
        fsCount += ntohs(p.linesize);

        if(fsCount >= fsTotal)
          break;
      }
    }

    a.acknum = htonl(pkn);
    a.checksum = 0;
    a.checksum = htons(checksum8((char*)&a, sizeof(a)));

    printf("<-[%u]\t", ntohl(a.acknum));

    n = sendto(sockfd, (void *)&a, sizeof(a), 0, (struct sockaddr*)&clt_addr, addrlen);
    if(n < 0) syserr("can't send to server");

    printf("%lu\n", fsTotal - fsCount);
  }
      
  gettimeofday(&end, NULL);
  fclose(fp);
  printf("Done writing the file! Making sure client is done...\n");
    
  for(;;) // make sure the client is finished
  {
    a.acknum = htonl(-1);
    a.checksum = 0;
    a.checksum = htons(checksum8((char*)&a, sizeof(a)));

    n = sendto(sockfd, (void *)&a, sizeof(a), 0, (struct sockaddr*)&clt_addr, addrlen);
    if(n < 0) syserr("can't send to server");

    fd_set readfds, masterfds;
    struct timeval timeout;
      
    timeout.tv_sec = 60;
    timeout.tv_usec = 0;
          
    FD_ZERO(&masterfds);
    FD_SET(sockfd, &masterfds);
          
    memcpy(&readfds, &masterfds, sizeof(fd_set));
          
    select(sockfd + 1, &readfds, NULL, NULL, &timeout);
          
    if(FD_ISSET(sockfd, &readfds))
    {
      n = recvfrom(sockfd, &a, sizeof(a), 0, (struct sockaddr*)&serv_addr, &addrlen);
      if(n < 0) syserr("can't receive from client");
    }
    else
      break;
  }

  close(sockfd);

  long seconds  = end.tv_sec  - start.tv_sec;
  long useconds = end.tv_usec - start.tv_usec;

  float elapsed = (((seconds) * 1000 + useconds/1000.0) + 0.5)/1000; // calculate throughput

  clientIP[IPSIZE - 1] = '\0';
  printf("The IP address of the client was %s and it's port was %d.\n", clientIP, clientPort);
  printf("The size is %lu bytes.\nThe time is %f seconds.\n", fsTotal, elapsed);
  printf("The throughput is therefore %d bits per second.\n", (int)((fsTotal * 8)/(elapsed)));

  return 0;
}
