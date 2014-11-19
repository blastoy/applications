all:	myftpclient myftpserver

tcpclient: myftpclient.c
	gcc -Wall $< -o $@

tcpserver: myftpserver.c
	gcc -Wall $< -o $@

clean:
	rm -f myftpclient myftpserver *.o *~ core

