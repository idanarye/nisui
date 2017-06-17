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

# data.plot('memoryTime', avg, 'coverageTime', lambda dp: dp.memoryTime < 100000, detectionRadius=100)
data.plot('memoryTime', avg, 'coverageTime', lambda dp: dp.memoryTime < 100000, group_by=['detectionRadius'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageTime', group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
