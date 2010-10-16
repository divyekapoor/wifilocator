#!/usr/bin/env python
import re
import sys
import simplejson as json
from pprint import pprint
from subprocess import Popen,PIPE
from optparse import OptionParser

# Mark out the general pattern of the output as a regular expression
# Special matching characters used in the regular expression:
#   \d - Digit (0-9)
#   [...] - Character classes and consecutive character ranges
#   [^...] - Negated character class (match all characters except the ones mentioned
#   \s - Match any whitespace
# 
# Modifiers:
#   + - Match one or more characters
#   * - Match zero or more characters
#   \ - Escape the following character from its special meaning in the regular expression
#   {x} - Match the preceding character or group 'x' times
#
# Grouping characters:
#   (...) - Keep all characters matched within the parenthesis as a matched entity
#   (?:...) - Non grouping match (match the characters in the group as a single entity but don't save the result)
#   (?<name>...) - Named pattern match (the result can be retrieved with the name "name" from the groupdict() 
#
#
# Typical Cell Result is like:
#Cell 05 - Address: 00:0C:42:68:DE:D3
#                    Channel:13
#                    Frequency:2.472 GHz (Channel 13)
#                    Quality=32/70  Signal level=-78 dBm  
#                    Encryption key:off
#                    ESSID:"oasis_9997459688"
#                    Bit Rates:1 Mb/s; 2 Mb/s; 5.5 Mb/s; 11 Mb/s; 6 Mb/s
#                              9 Mb/s; 12 Mb/s; 18 Mb/s
#                    Bit Rates:24 Mb/s; 36 Mb/s; 48 Mb/s; 54 Mb/s
#                    Mode:Master
#                    Extra:tsf=0000001120b5f27a
#                    Extra: Last beacon: 7112ms ago
#                    IE: Unknown: 00106F617369735F39393937343539363838
#                    IE: Unknown: 010882848B960C121824
#                    IE: Unknown: 03010D
#                    IE: Unknown: 050400010000
#                    IE: Unknown: 2A0100
#                    IE: Unknown: 32043048606C
#                    IE: Unknown: DD2A000C42000000011E0000000000660604000030303043343236384445443300000000000000000502A809

#cellre = r"""Address: (?P<macaddr>(?:(?:[0-9a-fA-F]{2}):){5}(?:[0-9a-fA-F]{2}))
#                    Channel:(?P<channel>\d+)
#                    Frequency:(?P<freq>\d+|\d+\.\d*) GHz \(Channel (?:\d+)\)
#                    Quality=(?P<quality>\d+)/(?P<max_quality>\d+)  Signal level=(?P<signallevel>[+-]?\d+) dBm  
#                    Encryption key:.*
#                    ESSID:"(?P<essid>[^\n"]*)"""


def scan(interface):
    # Note: it takes quite a bit of time to create and verify this regex! :)
    cellre = r"""Address: (?P<macaddr>(?:(?:[0-9a-fA-F]{2}):){5}(?:[0-9a-fA-F]{2}))\s+Channel:(?P<channel>\d+)\s+Frequency:(?P<freq>\d+|\d+\.\d*) GHz \(Channel (?:\d+)\)\s+Quality=(?P<quality>\d+)/(?P<max_quality>\d+)  Signal level=(?P<signallevel>[+-]?\d+) dBm\s+Encryption key:.*\s+ESSID:"(?P<essid>[^\n"]*)"""

    # Print out the regular expression for debugging purposes
    # print cellre

    # Create a compiled expression NFA to parse the strings
    cell_parse_pattern = re.compile(cellre)

    # Completely read in the input as a huge string
    iwlist = Popen("./iwscan " + interface, shell=True, stdout=PIPE, stdin=None, stderr=PIPE)
    complete_input = iwlist.stdout.read()
    ret_code = iwlist.wait()

    # Make sure we scanned successfully
    if ret_code != 0:
        return (ret_code, [])

    # For all matches in the input string, return a match object iterator
    groups = re.finditer(cell_parse_pattern, complete_input)

    # Go through the groups and append the matched named groups to the cell_list
    cell_list = []
    for g in groups:
        cell_list.append(g.groupdict())

    return (ret_code, cell_list)


def main(interface, options):
    retcode, cells = scan(interface)
    if retcode != 0:
        sys.exit(retcode)

    # If easily parsable output is desired, transform the result to JSON and print it out
    if not options.quiet:
        if options.json:
            print json.dumps(cell_list)
        else:
            # Pretty print the result
            counter = 0
            for cell in cells:
                print "Cell %d" % counter
                counter += 1
                pprint(cell, indent=2)

    sys.exit(retcode)

# The following if statement will be executed only when calling this python file from the command line and
# not importing it as a module.
# Hence, ensure that all command line options are created and parsed only then
# Module importers can just create a python dict to provide options
if __name__ == "__main__":
    parser = OptionParser(usage="Usage: %s [interface] [options]" % sys.argv[0])
    parser.add_option("-Q", "--quiet", action="store_true", default=False, dest="quiet", help="Do not print cell scan results to stdout. May be useful to test the return error code")
    parser.add_option("-j", "--json", action="store_true", default=False, dest="json", help="Return the cell scan output in JSON for portability")

    (options,args) = parser.parse_args()
    if len(args) > 1:
        parser.error("Too many arguments for [interface]")
    
    interface = "wlan0"
    if len(args) == 1:
        interface = args[0]

    main(interface, options)

