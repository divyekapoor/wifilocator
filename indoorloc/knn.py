#!/usr/bin/env python

import os,sys,math
import simplejson as json
from optparse import OptionParser
from pprint import pprint
from subprocess import Popen, PIPE

def perfile(callablefn):
    for filepath in os.listdir('.'):
        if ".txt" in filepath:
            f = open(filepath, "r")
            try:
                sampledata = json.loads(f.read())
                callablefn(filepath, sampledata)
            except Exception,e:
                # File failed... Ignore.
                # print "file: ", filepath, " failed"
                # print e
                pass
            f.close() 

class DistinctAP(object):
    def __init__(self):
        self.macaddrlist = {}

    def __call__(self, filepath, sampledata):
        for cell in sampledata:
            if cell['macaddr'] not in self.macaddrlist:
                self.macaddrlist[cell['macaddr']] = 1
            else:
                self.macaddrlist[cell['macaddr']] += 1

    def aplist(self):
        return self.macaddrlist.keys()
    

def vectorize(sampledata, ap_list):
    vector_rep = ['-90']*len(ap_list)
    for cell in sampledata:
        try:
            vector_rep[ap_list.index(cell['macaddr'])] = cell['signallevel']
        except:
            # Ignore if access points other than those available in the training db are supplied.
            pass


    return vector_rep


class SignalMatrix(object):
    def __init__(self):
        self.distinct_ap = DistinctAP()
        perfile(self.distinct_ap)

        self.rssi = {}

    def __call__(self, filepath, sampledata):
        ap_list = self.distinct_ap.aplist()
        self.rssi[filepath] = vectorize(sampledata, ap_list)

    def signal_matrix(self):
        return self.rssi


# Manhattan Distance
def mdist(u, v):
    assert len(u) == len(v)
    c = 0
    for x,y in zip(u,v):
        c += abs(int(x) - int(y))

    return c

# Euclidean Distance
def edist(u, v):
    assert len(u) == len(v)
    c = 0
    for x,y in zip(u,v):
        c += (int(x) - int(y))**2

    c = math.sqrt(c)
    return c


def locate(k, dist, options, args):
    curdir = os.path.abspath(os.path.curdir)
    os.chdir(os.path.abspath(os.path.dirname(__file__)))

    dap = DistinctAP()
    perfile(dap)
    # pprint(dap.aplist())

    sm = SignalMatrix()
    perfile(sm)
    # pprint(sm.signal_matrix(), width=200)
    
    if options.scan:
        cellscans = Popen('./cell_scan.py --json', shell=True, stdout=PIPE)
        cells = cellscans.stdout.read()
        cellscans.wait()
        cells = json.loads(cells)
    else:   
        cells = json.loads(sys.stdin.read())
    
    v = vectorize(cells, dap.aplist())

    if options.verbose:
        print "------------------- scanned vector --------------------"
        pprint(v, width=300)
        print "------------------- location matches ------------------"
    
    counter_list = []
    for f,d in sm.signal_matrix().items():
        counter_list += [(dist(d,v), f, d)]

    counter_list.sort()

    if options.verbose:
        pprint(counter_list[:k], width=300)

    votes = {}
    for i in counter_list[:k]:
        d,f,v = i
        f = f.replace("a.txt", "").replace("b.txt", "").replace("c.txt", "").replace("d.txt", "").replace("_", " ").strip()

        if f in votes:
            votes[f] += 1
        else:
            votes[f] = 1

    loc, vot = ("", 0)
    for l,v in votes.items():
        if v > vot:
            loc,vot = l,v
        
    os.chdir(curdir)

    return (loc,vot)
 

if __name__ == "__main__":
    parser = OptionParser(usage="Usage: %s [interface] [options]" % sys.argv[0])
    parser.add_option("-v", "--verbose", action="store_true", default=False, dest="verbose", help="Print verbose data to stdout.")
    parser.add_option("-j", "--json", action="store_true", default=False, dest="json", help="Return the location output in JSON for portability")
    parser.add_option("-k", "--neighbours", action="store", default=1, dest="k", type="int", help="The number of neighbours to use to determine the final indoor location")
    parser.add_option("-d", "--dist", action="store", default="mdist", type="choice", dest="dist", choices=["mdist", "edist"], help="The type of distance metric to use. mdist - Manhattan Distance, edist - Euclidean Distance")
    parser.add_option("-s", "--scan", action="store_true", default=False, dest="scan", help="Initiate a scan to figure out the Wifi access points")

    (options,args) = parser.parse_args()
    
    distfn = { 'mdist' : mdist, 'edist' : edist }

    loc,votes = locate(options.k, distfn[options.dist], options, args)

    if options.json:
        print json.dumps({ "location" : loc, "votes" : votes})
    else:
        print loc, votes
