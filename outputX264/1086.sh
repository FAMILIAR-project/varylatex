#!/bin/bash

numb='1086'
logfilename="$numb.log"
trailerlocation='../husky_cif.y4m'
TIMEFORMAT="USERTIME %U                                                                                            
SYSTEMTIME %S                                                                                                      
ELAPSEDTIME %R"; { time ../x264/x264 --no-asm --ref 5 --no-fast-pskip --rc-lookahead 60 --no-deblock --no-mbtree   -o sintel$numb.flv $trailerlocation ; } 2> $logfilename
# size of the video
size=`ls -lrt sintel$numb.flv | awk '{print $5}'`
# analyze log to extract relevant timing information
usertime=`grep "USERTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
systemtime=`grep "SYSTEMTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
elapsedtime=`grep "ELAPSEDTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
# clean
rm sintel$numb.flv


csvLine='1086,true,false,true,false,true,true,true,false,false,60,5'
csvLine="$csvLine,$size,$usertime,$systemtime,$elapsedtime"
echo $csvLine