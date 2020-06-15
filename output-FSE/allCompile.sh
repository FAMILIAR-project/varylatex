#!/bin/sh
set -e

STATS=stats.csv
touch $STATS
rm $STATS
touch $STATS

headersFts=`cat headerftscsv.txt`
headers="idConfiguration,$headersFts,nbPages,sizePDF"

echo $headers >> $STATS

PDFLATEX="pdflatex -interaction=nonstopmode -synctex=1 -file-line-error"
FILTER="grep -v -e /usr/share/texlive"

texs=`ls VaryingVariability-FSE15*.tex`
for tex in $texs
do
    latexFileName="${tex%.*}"
    echo "Processing: " $latexFileName
    $PDFLATEX $latexFileName | $FILTER && printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' -
    bibtex $latexFileName && printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' -
    $PDFLATEX $latexFileName | $FILTER && printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' -
    $PDFLATEX $latexFileName | $FILTER
    # open -a Preview $latexFileName".pdf" # specific to MacOS
    # xdg-open $latexFileName".pdf" # for the demo ;)
    sleep 2
     # https://stackoverflow.com/questions/1672580/get-number-of-pages-in-a-pdf-using-a-cmd-batch-file
    # TODO: pdfinfo is actually very powerful and can be useful to dev an oracle
    nbPages=`pdfinfo $latexFileName.pdf | grep Pages | sed 's/[^0-9]*//'`
    # nbPages=`mdls -name kMDItemNumberOfPages -raw $latexFileName.pdf` # works only on MacOS!
    sizePDF=`du -k $latexFileName.pdf | cut -f1`
    idConfiguration="${latexFileName#*VaryingVariability-FSE15_}"
    configurationValues=`cat $latexFileName".csv"`
    echo $idConfiguration","$configurationValues","$nbPages","$sizePDF >> $STATS
done
