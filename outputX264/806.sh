#!/bin/bash

numb='806'
logfilename="$numb.log"
trailerlocation='../husky_cif.y4m'
TIMEFORMAT="USERTIME %U                                                                                            
SYSTEMTIME %S                                                                                                      
ELAPSEDTIME %R"; { time ../x264/x264 --no-asm --ref 1 --no-fast-pskip --no-8x8dct --rc-lookahead 60  --no-mixed-refs  -o sintel$numb.flv $trailerlocation ; } 2> $logfilename
# size of the video
size=`ls -lrt sintel$numb.flv | awk '{print $5}'`
# analyze log to extract relevant timing information
usertime=`grep "USERTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
systemtime=`grep "SYSTEMTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
elapsedtime=`grep "ELAPSEDTIME" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`
# clean
rm sintel$numb.flv


csvLine='806,true,true,true,false,false,true,false,true,false,60,1'
csvLine="$csvLine,$size,$usertime,$systemtime,$elapsedtime"
echo $csvLine