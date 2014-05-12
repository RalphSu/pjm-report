#!/bin/bash


port='8080'
`curl --max-time 3 http://127.0.0.1:$port > /dev/null`
ret=`echo $?`
echo "$(date) curl response $ret"
if [ $ret -eq "0" ]; then
	echo "$(date) server is ok exit"
	exit
fi

pid=`ps aux | grep ruby | grep -v grep | awk '{print $2}'`
echo " old server pid is $pid"

if [ ! -z "$pid" ]; then 
	echo "$(date) kill old server"
	`kill -9 $pid`
fi
echo "$(date) start new server"
`cd ~/workspace/pjm2/; ruby script/server -e production -p 8080 -d > /dev/null;`
