#!/usr/bin/zsh
for f in ./**/*.csv; do
    python draw_lines.py $f `dirname $f`/`basename $f .csv`.png
done
