#!/usr/bin/env python
#
#          Copyright Divye Kapoor 2010
#
# This file simply copies the input to the output while skipping n lines.
# n - user supplied parameter

import sys

def usage():
    print sys.argv[0]," <number of lines to skip> [ <offset> ] "
    sys.exit(0)

def main():
    if len(sys.argv) < 2:
        usage()

    skip = int(sys.argv[1]) # The number of lines to skip each turn
    offset = 0              # The first line that is actually going to be printed. Default = 0
    if len(sys.argv) >= 3:
        offset = int(sys.argv[2])

    count = 0
    for line in sys.stdin:
        if offset > 0: # Skip the first 'offset' lines
            offset -= 1
            continue

        if count == 0: # Print every 'skip' line
            print line,

        count = (count+1) % skip


if __name__ == "__main__":
    main()

    
