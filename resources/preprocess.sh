#!/bin/bash

find . -type f -name "*.csv" -print0 | xargs -t -L 2 -P 4 -0 sed -i 's/\\"/""/g'
# find . -type f -name "*.csv" -print0 | xargs -t -L 2 -P 4 -0 sed -i 's/\\""/\\"/g'
# find . -type f -name "*.csv" -print0 | xargs -t -L 2 -P 4 -0 sed -i 's/\\\\"/\\\\""/g'
