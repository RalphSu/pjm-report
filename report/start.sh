#!/bin/bash

echo $PJM_HOME
mvn clean install -DskipTests
#/home/csp-ubuntu-47/workspace/pjm-report/report/
nohup java -Xmx512m -jar target/report.jar >> report_generation.log &

