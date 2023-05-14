#! /bin/bash

output=$(realpath "$1")

# WorldWind
./ccdetectrunner.sh ./WorldWind/DEL10 incremental $output/WorldWind_DEL10.ccdetect
./ccdetectrunner.sh ./WorldWind/DEL100 incremental $output/WorldWind_DEL100.ccdetect
./ccdetectrunner.sh ./WorldWind/INS10 incremental $output/WorldWind_INS10.ccdetect
./ccdetectrunner.sh ./WorldWind/INS100 incremental $output/WorldWind_INS100.ccdetect

./ccdetectrunner.sh ./WorldWind/DEL10 saca $output/WorldWind_DEL10.saca
./ccdetectrunner.sh ./WorldWind/DEL100 saca $output/WorldWind_DEL100.saca
./ccdetectrunner.sh ./WorldWind/INS10 saca $output/WorldWind_INS10.saca
./ccdetectrunner.sh ./WorldWind/INS100 saca $output/WorldWind_INS100.saca

./iclonesrunner.sh WorldWind/DEL10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/WorldWind_DEL10.iclones
./iclonesrunner.sh WorldWind/DEL100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/WorldWind_DEL100.iclones
./iclonesrunner.sh WorldWind/INS10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/WorldWind_INS10.iclones
./iclonesrunner.sh WorldWind/INS100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/WorldWind_INS100.iclones

# graal 
./ccdetectrunner.sh ./graal/DEL10 incremental $output/graal_DEL10.ccdetect
./ccdetectrunner.sh ./graal/DEL100 incremental $output/graal_DEL100.ccdetect
./ccdetectrunner.sh ./graal/INS10 incremental $output/graal_INS10.ccdetect
./ccdetectrunner.sh ./graal/INS100 incremental $output/graal_INS100.ccdetect

./ccdetectrunner.sh ./graal/DEL10 saca $output/graal_DEL10.saca
./ccdetectrunner.sh ./graal/DEL100 saca $output/graal_DEL100.saca
./ccdetectrunner.sh ./graal/INS10 saca $output/graal_INS10.saca
./ccdetectrunner.sh ./graal/INS100 saca $output/graal_INS100.saca

./iclonesrunner.sh graal/DEL10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/graal_DEL10.iclones
./iclonesrunner.sh graal/DEL100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/graal_DEL100.iclones
./iclonesrunner.sh graal/INS10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/graal_INS10.iclones
./iclonesrunner.sh graal/INS100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/graal_INS100.iclones

# flink 
./ccdetectrunner.sh ./flink/DEL10 incremental $output/flink_DEL10.ccdetect
./ccdetectrunner.sh ./flink/DEL100 incremental $output/flink_DEL100.ccdetect
./ccdetectrunner.sh ./flink/INS10 incremental $output/flink_INS10.ccdetect
./ccdetectrunner.sh ./flink/INS100 incremental $output/flink_INS100.ccdetect

./ccdetectrunner.sh ./flink/DEL10 saca $output/flink_DEL10.saca
./ccdetectrunner.sh ./flink/DEL100 saca $output/flink_DEL100.saca
./ccdetectrunner.sh ./flink/INS10 saca $output/flink_INS10.saca
./ccdetectrunner.sh ./flink/INS100 saca $output/flink_INS100.saca

./iclonesrunner.sh flink/DEL10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/flink_DEL10.iclones
./iclonesrunner.sh flink/DEL100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/flink_DEL100.iclones
./iclonesrunner.sh flink/INS10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/flink_INS10.iclones
./iclonesrunner.sh flink/INS100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/flink_INS100.iclones

# neo4j
./ccdetectrunner.sh ./neo4j/DEL10 incremental $output/neo4j_DEL10.ccdetect
./ccdetectrunner.sh ./neo4j/DEL100 incremental $output/neo4j_DEL100.ccdetect
./ccdetectrunner.sh ./neo4j/INS10 incremental $output/neo4j_INS10.ccdetect
./ccdetectrunner.sh ./neo4j/INS100 incremental $output/neo4j_INS100.ccdetect

./ccdetectrunner.sh ./neo4j/DEL10 saca $output/neo4j_DEL10.saca
./ccdetectrunner.sh ./neo4j/DEL100 saca $output/neo4j_DEL100.saca
./ccdetectrunner.sh ./neo4j/INS10 saca $output/neo4j_INS10.saca
./ccdetectrunner.sh ./neo4j/INS100 saca $output/neo4j_INS100.saca

./iclonesrunner.sh neo4j/DEL10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/neo4j_DEL10.iclones
./iclonesrunner.sh neo4j/DEL100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/neo4j_DEL100.iclones
./iclonesrunner.sh neo4j/INS10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/neo4j_INS10.iclones
./iclonesrunner.sh neo4j/INS100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/neo4j_INS100.iclones

# #elasticsearch
./ccdetectrunner.sh ./elasticsearch/DEL10 incremental $output/elasticsearch_DEL10.ccdetect
./ccdetectrunner.sh ./elasticsearch/DEL100 incremental $output/elasticsearch_DEL100.ccdetect
./ccdetectrunner.sh ./elasticsearch/INS10 incremental $output/elasticsearch_INS10.ccdetect
./ccdetectrunner.sh ./elasticsearch/INS100 incremental $output/elasticsearch_INS100.ccdetect

./ccdetectrunner.sh ./elasticsearch/DEL10 saca $output/elasticsearch_DEL10.saca
./ccdetectrunner.sh ./elasticsearch/DEL100 saca $output/elasticsearch_DEL100.saca
./ccdetectrunner.sh ./elasticsearch/INS10 saca $output/elasticsearch_INS10.saca
./ccdetectrunner.sh ./elasticsearch/INS100 saca $output/elasticsearch_INS100.saca

./iclonesrunner.sh elasticsearch/DEL10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/elasticsearch_DEL10.iclones
./iclonesrunner.sh elasticsearch/DEL100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/elasticsearch_DEL100.iclones
./iclonesrunner.sh elasticsearch/INS10 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/elasticsearch_INS10.iclones
./iclonesrunner.sh elasticsearch/INS100 | grep -E "(Version:)" | sed 's/^ *//' | cut -c 2- | awk '{print $3}' | paste -s -d, - > $output/elasticsearch_INS100.iclones

# intellij
./ccdetectrunner.sh ./intellij-community/DEL10 incremental $output/intellij_DEL10.ccdetect
./ccdetectrunner.sh ./intellij-community/DEL100 incremental $output/intellij_DEL100.ccdetect
./ccdetectrunner.sh ./intellij-community/INS10 incremental $output/intellij_INS10.ccdetect
./ccdetectrunner.sh ./intellij-community/INS100 incremental $output/intellij_INS100.ccdetect

./ccdetectrunner.sh ./intellij-community/DEL10 saca $output/intellij_DEL10.saca
./ccdetectrunner.sh ./intellij-community/DEL100 saca $output/intellij_DEL100.saca
./ccdetectrunner.sh ./intellij-community/INS10 saca $output/intellij_INS10.saca
./ccdetectrunner.sh ./intellij-community/INS100 saca $output/intellij_INS100.saca
