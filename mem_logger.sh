#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: logger_indefinite.sh log_name"
    exit 0
fi

logname=$1

mkdir -p $logname
finish_logging() {
	echo "[T] " $(date +"%T" )  "Finished all execution, sleeping 5s"
	sleep 5
	echo "Stopping logger.."
	pkill -9 sar
	echo "Deleting old files"
    rm $logname/mem.csv
    rm $logname/cpu.csv
    rm $logname/network.csv
    echo "Converting logfile to text.."
	##Convert sar output to csv files
	## sar -A -f $logname/log.file > $logname/log.txt
	sadf -d $logname/log.file -- -r > $logname/mem.csv
	sadf -d $logname/log.file -- -u > $logname/cpu.csv
	sadf -d $logname/log.file -- -n DEV > $logname/network.csv
}

echo "Starting logger.."
sar -u -o $logname/log.file 1 3600 > /dev/null 2>&1 &

echo "[T] " $(date +"%T" )  "Logging.."



echo "Press RETURN to finish logging"
read input_variable
finish_logging
