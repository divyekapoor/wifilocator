#!/usr/bin/env python

import sys
import csv
from math import *

if len(sys.argv) < 3:
    print "Usage: ", sys.argv[0], " <truth file> <test file>"
    sys.exit(1)

truthf = csv.reader(open(sys.argv[1]))
truth = []

for row in truthf:
    truth.append([float(x) for x in row])

testf = csv.reader(open(sys.argv[2]))
test = []

for row in testf:
    test.append([float(x) for x in row])

mfactor = len(truth)/(1.0*len(test))
for index in range(len(test)):
    truth_index = int(mfactor*index)
    error = [test[index][0] - truth[truth_index][0], test[index][1] - truth[truth_index][1]]
    error.append(sqrt(error[0]*error[0] + error[1]*error[1]))
    print ",".join([str(x) for x in error])

