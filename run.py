#!/usr/bin/python3

import sys
import os
import json

exec_args = ['gradle', 'gradle', 'run', '-q']
args = sys.argv[1:]
if args:
    # print(args, flush=True)
    # print(list(map(json.dumps, args)), flush=True)
    # exec_args.append('-Pargs=%s' % ' '.join(map(json.dumps, args)))
    exec_args.append('-Pargs=%s' % ' '.join(args))

os.execlp(*exec_args)
