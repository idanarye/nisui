#!/usr/bin/python

import dataload

data = dataload.ExperimentsData('results.txt')
# data = dataload.ExperimentsData('results.txt.mark-ahead-1')


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
# data.plot(('detectionRadius', 'numberOfRobots'), avg, 'coverageTime', lambda dp: 1 < dp.numberOfRobots, group_by=['memoryTime', 'ignoreInMyOwnTurf'])
# data.plot(('numberOfRobots'), avg, 'coveragePercentage', lambda dp: 1 < dp.numberOfRobots and dp.ignoreInMyOwnTurf and dp.detectionRadius == 20 and dp.ignoreInMyOwnTurf, group_by=['memoryTime'])
for nr in sorted({p[0].numberOfRobots for p in data._data}):
    if nr == 1.0:
        continue
    # data.plot(('numberOfRobots'), avg, 'coveragePercentage',
              # lambda dp: 1 < dp.numberOfRobots,
              # group_by=['marginsOverMarkerForDetection'],
              # filename='detectionRadius-%s.png' % dt,
              # detectionRadius=dt)
    data.plot(('detectionRadius'), avg, 'coveragePercentage',
    # data.plot(('detectionRadius'), avg, coverageSpeed,
              lambda dp: 1 < dp.numberOfRobots,
              group_by=['keepInteria'],
              filename='numberOfRobots-%s.png' % nr,
              numberOfRobots=nr)
# data.plot(('detectionRadius'), avg, 'coverageTime', lambda dp: 1 < dp.numberOfRobots, numberOfRobots=30, group_by=['keepInteria'])
