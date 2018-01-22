#!/usr/bin/python3

import sys
import os
import shlex

args = sys.argv[1:]
args = ' '.join(map(shlex.quote, args))
os.execlp('gradle', 'gradle', 'run', '-q', '-Pargs=%s' % args)
