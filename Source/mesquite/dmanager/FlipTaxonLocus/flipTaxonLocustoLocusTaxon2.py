#!/usr/bin/python
# coding: utf-8

#PYTHON 2

# To convert directory of files, each named by taxon
# and containing sequences named by locus,
# to directory of files, each named by locus
# and containing sequences named by taxon

#e.g., run via a shell script on the path; cd first into directory with files, then run shell script

# W.Maddison 2023; quick and dirty; please excuse crudeness.

import os
from os import *
from os.path import isfile, join
import glob


import io

fastaDir = os.getcwd()
fastaFiles = [ f for f in listdir(fastaDir) if isfile(join(fastaDir,f)) ]
outputDirectory = fastaDir + "/Flipped"
d = os.path.dirname(outputDirectory)
if not os.path.exists(outputDirectory):
	os.makedirs(outputDirectory)
else:
	# deleteAll files in directory
	files = glob.glob(outputDirectory + "/*")
	for f in files:
		os.remove(f)

for f in fastaFiles:
	if not f.startswith("."):
		taxonName = f[0:f.rfind('.')]
		#print "Taxon: ", taxonName
		lines = io.open(fastaDir + "/" + f, 'r', newline="\n")
		for line in lines:
			if line.startswith(">"):
				locusName = line[1:].strip()
				locusFilePath = outputDirectory + "/" + locusName + ".fas"
				if not os.path.exists(locusFilePath):
					locusFile = io.open(locusFilePath, 'w')
					#print "   locus: " + locusName
				else:
					locusFile = io.open(locusFilePath, 'a')
				locusFile.write(">".decode('utf-8'))
				locusFile.write(taxonName.decode('utf-8'))
				locusFile.write("\n".decode('utf-8'))
			else:
				locusFile.write(line.decode('utf-8'))
