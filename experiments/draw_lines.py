#!/usr/bin/env python

import sys, itertools
from subprocess import call

if len(sys.argv) < 3:
    print sys.argv[0], "filename output"
    sys.exit(1)

print "Starting..."
f = open(sys.argv[1])

plist = []
points = ""
for line in f:
    a,b = line.strip().split(',')
    a = int(640*float(a))
    b = int(480*float(b))
    plist += [(a,b)]
    points += str(a) + "," + str(b) + " "

w = 1
rectpoints = ["rectangle " + str(a - w) + "," + str(b-w) + " " + str(a+w) + "," + str(b+w) for a,b in plist]
args = ["convert", "map.png", "-stroke", "black", "-fill", "none", "-draw", "polyline " + points, "-fill", "black"]

for x in rectpoints:
    args += ['-draw', x]

args += [sys.argv[2]]
print args
print call(args)  
