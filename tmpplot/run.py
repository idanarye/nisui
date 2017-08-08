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
# data.plot('memoryTime', avg, 'coverageTime', lambda dp: dp.memoryTime < 100000, group_by=['detectionRadius'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageTime', group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
# data.plot('memoryTime', avg, 'coveragePercentage', lambda dp: dp.memoryTime < 100000, group_by=['detectionRadius'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageTime', lambda dp: dp.memoryTime < 100000, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coveragePercentage', lambda dp: dp.memoryTime < 100000, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)

def coverageSpeed(obj):
    return obj['coverageTime'] / obj['coveragePercentage']
# data.plot('detectionRadius', avg, coverageSpeed, lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageTime', lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coveragePercentage', lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
# data.plot(('detectionRadius', 'memoryTime'), avg, 'coverageRedundancy', lambda dp: dp.memoryTime in {1000, 5000, 10000}, numberOfRobots=2) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageRedundancy', lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)
#  data.plot(('detectionRadius', 'memoryTime'), avg, 'coverageRedundancy', lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['numberOfRobots']) # detectionRadius=20)
# data.plot('detectionRadius', avg, 'coverageRedundancy', lambda dp: dp.memoryTime in {1000, 5000, 10000}, group_by=['memoryTime'], numberOfRobots=2) # detectionRadius=20)


# data.plot('numberOfRobots', avg, 'coverageTime', lambda dp: 1 < dp.numberOfRobots, group_by=['detectionRadius'])
data.plot(('detectionRadius', 'numberOfRobots'), avg, 'coverageTime', lambda dp: 1 < dp.numberOfRobots, group_by=['memoryTime', 'ignoreInMyOwnTurf'])
