#!/usr/bin/env python

import SnapPea
import getopt
import sys

opts, args = getopt.getopt(sys.argv[1:], 'c:t')
t = None
triangFlag = False

for o, v in opts:
  if o=='-c':
    t = SnapPea.get_manifold(v)
  elif o=='-t':
    triangFlag = True

for s in args:
  t = SnapPea.Triangulation(s)

if t is None:
  t = SnapPea.Triangulation('')

if triangFlag:
  t.save('')
else:
  print t
  print 'fundamental group:'
  print t.fundamental_group(1, 1, 1)

