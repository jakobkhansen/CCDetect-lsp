#!/bin/bash

root=`dirname $1`
dir=`basename $1`
path=$root/$dir

cd /home/jakob/Documents/Dev/LSP/CCDetect-lsp/

java -DbcbPath=file://"$path" -cp app/build/libs/app-all.jar CCDetect.lsp.evaluation.BigCloneBenchEvaluation > /dev/null 2> /dev/null

cat evaluation.txt
