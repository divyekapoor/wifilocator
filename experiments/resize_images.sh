#!/usr/bin/zsh
for f in ./**/*.png; do
    convert $f -filter Lagrange -resize 1280x960 `dirname $f`/`basename $f .png`.resized.png
done
