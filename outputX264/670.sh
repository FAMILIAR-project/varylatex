#!/bin/bash

numb='670'
logfilename="/srv/local/macher/bench3/output/$numb.log"
trailerlocation='/srv/local/macher/bench3/forest_jester-dv.mov'

TIMEFORMAT="USERTIME %U                                                                                            
SYSTEMTIME %S                                                                                                      
ELAPSEDTIME %R"; { time ../x264/x264 --ref 5 --no-fast-pskip --no-deblock --no-8x8dct --rc-lookahead 40  --no-cabac --no-mixed-refs  -o /srv/local/macher/bench3/tempvids/sintel$numb.264 $trailerlocation ; } 2> $logfilename
# size of the video
size=`ls -lrt /srv/local/macher/bench3/tempvids/sintel$numb.264 | awk '{print $5}'`
# analyze log to extract relevant timing information
usertime=`grep "USERTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
systemtime=`grep "SYSTEMTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
elapsedtime=`grep "ELAPSEDTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
# clean
rm /srv/local/macher/bench3/tempvids/sintel$numb.264


csvLine='670,true,true,false,true,true,true,false,true,false,40,5'
csvLine="$csvLine,$size,$usertime,$systemtime,$elapsedtime"
echo $csvLine