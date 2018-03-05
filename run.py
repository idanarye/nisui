#!/usr/bin/python3

import sys
import os
import shlex

exec_args = ['gradle', 'gradle', 'run', '-q']
args = sys.argv[1:]
if args:
    exec_args.append('-Pargs=%s' % ' '.join(map(shlex.quote, args)))

os.execlp(*exec_args)
