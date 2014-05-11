#!/bin/bash


port='8080'
`curl http://127.0.0.1:$port > /dev/null`
ret=`echo $?`
echo $ret
if [ $ret -eq "0" ]; then
	echo 'server is ok exit'
	exit
fi

pid=`ps aux | grep ruby | grep -v grep | awk '{print $2}'`
echo $pid

if [ -z "$VAR" ]; then 
	echo "restart server"
	`kill -9 $pid`
fi
`cd ~/workspace/pjm2/; ruby script/server -e production -p 8080 -d;`
