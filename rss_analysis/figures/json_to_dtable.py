#!/usr/bin/env python
#
#          Copyright Divye Kapoor 2010
#
# This file is used to obtain in CSV form the point representation of 
# each signal sample.
#
# The sample is being represented by an n dimensional vector, each
# dimension corresponds to a particular MAC Address (AP)
#
# Input to this script: newline delimited sets of JSON objects
# Command Line Arguments: filename with ordered list of APs
# Output: A CSV file with [time, signal strength for AP1, signal strength for AP2, ...] for APs defined in the file specified
#

from termcolor import colored

import sys
import simplejson as json
import csv


def usage():
    print "Usage: ", sys.argv[0], " <filename> "
    sys.exit(0)

def process_field(sample, field):
    """Walk the JSON path down the list of attributes supplied on the command line and get the value of the field. 
    Sample fields are: 
       scaninfo.0.macaddr
       starttime
    """
    item = sample
    for subfield in field:
        if not item:
            return ""
        if type(subfield) is int and subfield >= len(item):
            return ""
        item = item[subfield]
    return item
 

def main():
    for line in sys.stdin:
        try:
            sample = json.loads(line)
            output = sample.copy()

            output["scaninfo"] = {}
            for cell in sample["scaninfo"]:
                output["scaninfo"][cell["macaddr"]] = cell

            print json.dumps(output)
        except ValueError as e:
            print colored(e, 'red')
            print 'Line: ', colored(line, 'yellow')
            raise


if __name__ == "__main__":
    main()

    
