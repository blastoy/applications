all:	udpsender udpreceiver

udpclient: udpsender.c
	gcc -Wall $< -o $@

udpserver: udpreceiver.c
	gcc -Wall $< -o $@

clean:
	rm -f udpsender udpreceiver *.o *~ core

