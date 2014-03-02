#!/bin/bash

source ~/.bashrc
env|grep PJM_HOME
nohup java -Xmx512m -jar ~/report.jar >> ~/report_generation.log &

