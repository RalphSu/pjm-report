#!/bin/bash

mvn clean install -DskipTests

# set cron tab
prefix=`pwd`
sh=$prefix/start.sh
jar=$prefix/target/report.jar
echo $sh
echo $jar

# ln sh
eof=`rm ~/start.sh`
ln -s $sh ~/start.sh
# ln jar
eof=`rm ~/report.jar`
ln -s $jar ~/report.jar

sh=~/start.sh
sh=${sh//\//\\/}
echo $sh

sed 's/exec/'$sh'/g' cron.tab.template > cron.tab
crontab cron.tab
