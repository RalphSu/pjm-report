#!/bin/bash

echo $PJM_HOME
mvn clean install -DskipTests

nohup java -Xmx512m -jar /home/csp-ubuntu-47/workspace/pjm-report/report/target/report.jar >> report_generation.log &

