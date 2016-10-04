#!/usr/bin/env python

import SnapPea
import getopt
import sys

opts, args = getopt.getopt(sys.argv[1:], 'c:')
mflds = []
for o, v in opts:
  mflds.append(SnapPea.get_manifold(v))
for s in args:
  mflds.append(SnapPea.Triangulation(s))

if len(mflds)<2:
  mflds.append(SnapPea.Triangulation(''))

if len(mflds)!=2:
  raise Exception, "wrong number of arguments"

t1, t2 = mflds
res = t1.get_name()
if (t1.is_isometric(t2)):
  res += " is isometric to "
else:
  res += " is not isometric to "
res += t2.get_name()
print res

