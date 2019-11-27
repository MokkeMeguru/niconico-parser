#!/bin/sh
DB="$(pwd)/headers.db"
IMPORT_DIR="$(pwd)"

IMPORT_FILES="$(pwd)/*.csv"

sqlite3 headers.db < create-table.sql
for file_path in $IMPORT_FILES; do
    file_name=$(basename $file_path)
    table_name="article_header"
    sqlite3 -separator , $DB ".import ${file_path} ${table_name}"
done
