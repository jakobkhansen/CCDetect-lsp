#! /bin/bash

dir=$(realpath "$1")
mode="$2"
output=$(realpath "$3")
cd ~/Documents/Dev/LSP/CCDetect-lsp
gradle evaluateIncrementalPerformance -Droot=file://$dir -Dmode=$mode -DoutputFile=$output
