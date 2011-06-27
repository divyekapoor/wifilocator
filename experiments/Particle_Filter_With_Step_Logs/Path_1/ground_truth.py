#!/usr/bin/env python

import sys

if len(sys.argv) < 2:
    print "Usage: ", sys.argv[0], " <num_steps>"
    sys.exit(1)

start = (0.70625,0.16479166666666667)
end = (0.20304687,0.1765625)

num_steps = int(sys.argv[1])

delta = ((end[0] - start[0])/num_steps, (end[1] - start[1])/num_steps)

for i in range(num_steps):
    print str(start[0] + i*delta[0]) + "," + str(start[1] + i*delta[1])


