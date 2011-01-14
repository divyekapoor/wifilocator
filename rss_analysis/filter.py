#!/usr/bin/env python
#
#          Copyright Divye Kapoor 2010
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
    writer.writerow([path[-1] for path in field_paths])
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

    
