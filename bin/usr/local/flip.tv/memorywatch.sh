#!/bin/sh

OLDTAIL=`cat /var/log/flip.tv/taillog.pid`

if [ -e /var/log/flip.tv/taillog ]; then
	mv /var/log/flip.tv/taillog /var/log/flip.tv/taillog.1
fi

tail -n 0 -F $1 > /var/log/flip.tv/taillog &
echo $! > /var/log/flip.tv/taillog.pid

if [ ! -z $OLDTAIL ]; then
	ps -ef|grep "[^]] $OLDTAIL.*tail" >/dev/null
	if [ $? == 0 ]; then
		kill $OLDTAIL
	fi
fi

if [ -e /var/log/flip.tv/taillog.1 ]; then
	grep OutOfMemoryError /var/log/flip.tv/taillog.1 > /var/log/flip.tv/outofmemory.log

	ERRORS=`cat /var/log/flip.tv/outofmemory.log | wc -l`

	if [ $ERRORS -gt 0 ]; then
		cat /var/log/flip.tv/outofmemory.log | mail -s "OutOfMemory on `uname -n`" errors@appeligo.com
	fi
fi
