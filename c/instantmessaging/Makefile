all:	imclient imserver

imclient: imclient.c
	gcc -Wall $< -o $@

imserver: imserver.c
	gcc -Wall $< -o $@

clean:
	rm -f imclient imserver *.o *~ core

