#!/bin/sh

numb='738'
logfilename="$numb.log"
trailerlocation='/Users/macher1/Documents/SANDBOX/x264Expe/sintel_trailer_2k_480p24.y4m'
(gtime -f "USERTIME %U\nSYSTEMTIME %S\nELAPSEDTIME %e\nMEMORYTIME %K" x264 --no-asm --ref 5 --no-8x8dct --rc-lookahead 20  --no-weightb  -o sintel$numb.flv $trailerlocation) 2> $logfilename
# size of the video
size=`du -k sintel$numb.flv | cut -f1`
# clean
rm sintel$numb.flv

# analyze log to extract relevant timing information
usertime=`grep "USERTIME" $logfilename | sed 's/[^.0-9]*//g'`
systemtime=`grep "SYSTEMTIME" $logfilename | sed 's/[^.0-9]*//g'`
elapsedtime=`grep "ELAPSEDTIME" $logfilename | sed 's/[^.0-9]*//g'`
memorytime=`grep "MEMORYTIME" $logfilename | sed 's/[^.0-9]*//g'`

csvLine='738,true,true,true,false,false,false,false,false,true,20,5'
csvLine="$csvLine,$size,$usertime,$systemtime,$elapsedtime,$memorytime"
echo $csvLine