#!/usr/bin/env python

import os
import simplejson as json
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
                print "file: ", filepath, " failed"
                print e
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
    vector_rep = [0]*len(ap_list)
    for cell in sampledata:
        vector_rep[ap_list.index(cell['macaddr'])] = cell['signallevel']

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


def dist(u, v):
    assert len(u) == len(v)
    c = 0
    for x,y in zip(u,v):
        c += abs(int(x) - int(y))

    return c

if __name__ == "__main__":
    k = 10
    dap = DistinctAP()
    perfile(dap)
    # pprint(dap.aplist())

    sm = SignalMatrix()
    perfile(sm)
    # pprint(sm.signal_matrix(), width=200)

    cellscans = Popen('./cell_scan.py --json', shell=True, stdout=PIPE)
    cells = cellscans.stdout.read()
    cellscans.wait()

    cells = json.loads(cells)
    v = vectorize(cells, dap.aplist())
    print "------------------- scanned vector --------------------"
    pprint(v, width=200)
    print "------------------- location matches ------------------"
    
    counter_list = []
    for f,d in sm.signal_matrix().items():
        counter_list += [(dist(d,v), f, d)]

    counter_list.sort()
    pprint(counter_list[:k], width=200)

