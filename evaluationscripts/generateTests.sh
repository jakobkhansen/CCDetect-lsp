dir=$(realpath "$1")
cd ~/Documents/Dev/LSP/CCDetect-lsp
gradle generateTest -Droot=$dir -DfileType=$2 -Dversions=$3 -Dchanges=$4 -DminSize=$5 -DmaxSize=$6
