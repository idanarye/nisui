#!/usr/bin/python

import dataload

data = dataload.ExperimentsData('results.txt')


def avg(inp):
    itr = iter(inp)
    total = next(itr)
    count = 1
    while True:
        try:
            total += next(itr)
        except StopIteration:
            return total / count
        count += 1

data.plot('memoryTime', avg, 'coverageTime', detectionRadius=100)
