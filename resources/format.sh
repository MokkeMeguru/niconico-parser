#!bin/bash

for entry in $(pwd)/*.json
do
    tmp_file=$(mktemp)
    jq . $entry > ${tmp_file} && mv ${tmp_file} $entry
done
