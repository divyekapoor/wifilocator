#!/usr/bin/env python
#
#          Copyright Divye Kapoor 2010
#
# This file is used to obtain in CSV form filtered sections of
# a JSON input file
#
# Input to this script: newline delimited sets of JSON objects
# Command Line Arguments: a . delimited path for every JSON field to be inserted into the CSV file
# Output: A CSV file with JSON values of the fields specified in the command line args
#

from termcolor import colored

import sys
import simplejson as json
import csv


def usage():
    print "Usage: ", sys.argv[0], " <field 1> [<field 2> <field 3> ...]"
    return -1

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
    field_paths = [arg.split('.') for arg in sys.argv[1:]]
    if not field_paths:
        return usage()
    
    for i in range(len(field_paths)):
        for j in range(len(field_paths[i])):
            if field_paths[i][j].isdigit():
                field_paths[i][j] = int(field_paths[i][j])

    writer = csv.writer(sys.stdout)
    writer.writerow([header for header in sys.argv[1:]])
    for line in sys.stdin:
        try:
            sample = json.loads(line)
            writer.writerow([process_field(sample, path) for path in field_paths])
        except ValueError as e:
            print colored(e, 'red')
            print 'Line: ', colored(line, 'yellow')
            raise


if __name__ == "__main__":
    main()

    
