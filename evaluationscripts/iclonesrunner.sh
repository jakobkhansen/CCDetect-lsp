dir=$(realpath "$1")

cd /home/jakob/Documents/Dev/LSP/iclones-0.2/
./iclones . -input $dir -informat directory -minblock 10 -minclone 100
