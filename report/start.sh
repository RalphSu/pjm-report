#!/bin/bash

echo $PJM_HOME
mvn clean install -DskipTests

nohup java -Xmx512m -jar target/report.jar >> report_generation.log &

