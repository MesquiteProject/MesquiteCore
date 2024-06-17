#PhyIN: Trimming alignments by phylogenetic incompatibility among neighbouring sites

# Wayne Maddison
# This is translated from Java, so please forgive my accent

version = "0.8, May 2024"

#============================= setting up parameters ===================
inFileName = "infile.fas"
outFileName = "outfile.fas"
blockLength = 12 # parameter b
propConflict = 0.4 # parameter p, proportion of neighbouring sites in conflict to trigger block selection
neighbourDistance = 1 # parameter d
gapsAsExtraState = False # parameter e, whether non-terminal gaps are treated as an extra state

import argparse
parser = argparse.ArgumentParser("phyin")
parser.add_argument("-input", help="File to be read. Must be DNA/RNA data and in FASTA format, aligned.", default = "infile.fas")
parser.add_argument("-output", help="File to be written.", default = "outfile.fas")
parser.add_argument("-b", help="Length of blocks over which conflict proportions are assessed. Integer.", type=int, default = 12)
parser.add_argument("-p", help="Proportion of neighbouring sites in conflict to trigger block selection. Double.", type=float, default = 0.4)
parser.add_argument("-d", help="Distance surveyed for conflict among neighbours. Integer.", type=int, default = 1)
parser.add_argument("-e", help="Treat gaps as an extra state.", action="store_true", default = False)
parser.add_argument("-v", help="Verbose.", action="store_true", default = False)
args = parser.parse_args()
inFileName = args.input
outFileName = args.output
blockLength = args.b
propConflict = args.p
gapsAsExtraState = args.e
neighbourDistance = args.d
verbose = args.v

numStates = 4 
if (gapsAsExtraState):
	numStates = 5 

print("PhyIN (", version, ") trimming with parameters: ")
print("   Length of blocks over which conflict proportions are assessed: ", args.b)
print("   Proportion of neighbouring sites in conflict to trigger block selection: ", args.p)
print("   Distance surveyed for conflict among neighbours: ", args.d)
print("   Treat gaps as an extra state: ", args.e)
print("NOTE: PhyIN ignores ambiguity codes, and considers only A, a, C, c, G, g, T, t, U, u, and possibly gaps (-).\n")
#=============================


#============================= Reading the data ===================
#=============================
# Read FASTA file
fastaFile = open(inFileName,'r')

# these are the lists that will hold the names and sequences.
names = [] # taxon names
sequences = [] # sequences

currentTaxon = 0
print("Reading file: ", inFileName)
for line in fastaFile: 
	line = line.strip() # strips end-of-line character
	if len(line)>0: #might not be needed, but in case there are blank lines, ignore them
		if line[0] == '>':  #The next taxon!
			names.append(line[1:]) #add this taxon name to the list of taxon names
			sequences.append("") #append an empty string to the list of sequences
			currentTaxon = len(sequences)-1 #remember what taxon number we're now on
			print("Reading " + line[1:])
		else: 
			sequences[currentTaxon] += line #not a taxon, therefore sequence, therefore concatenate to the current taxon's sequence
fastaFile.close()


# Checking this is an alignment, i.e. all sequences are the same length
if len(sequences) == 0:
	print("ERROR: No sequences read.")
	exit(42)

numChars = len(sequences[0])
for x in sequences:
	if (len(x) != numChars):
		print("ERROR: This appears not to be an alignment; not all taxa have the same sequence length.")
		exit(43)
# perhaps put in here a check that it is DNA data? This is important because of assumption of 4 states + gap		
		
numTaxa = len(names)
print("\nNumber of sequences (taxa): ", numTaxa);
print("Number of sites: ", numChars);

print("Processing...");
# convert sequences as text to nucleotides as integer codes for quicker access later
nucleotides =  [[0 for i in range(numChars)] for j in range(numTaxa)] # integer array i,k for compatibility test
def getEffectiveState(s):#need to translate character in sequence to internal representation of states
	if s in ["A", "a"]:  #use dictionary or whatever
		return 0
	elif s in ["C", "c"]:
		return 1
	elif s in ["G", "g"]:
		return 2
	elif s in ["T", "t", "U", "u"]:
		return 3
	elif gapsAsExtraState and s == "-":
		return 4
	else: # NOTE: this ignores all ambiguous bases
		return -1

counts = [0 for i in range(numStates)] # to check that this is credibly DNA data
gaps = 0
for i in range(numTaxa): #Translating sequences a text characters to 0, 1, 2, 3 code
	for k in range(numChars):
		state = getEffectiveState(sequences[i][k])
		if (state >= 0):
			counts[state] += 1
		if (sequences[i][k] == "-"):
			gaps += 1
		nucleotides[i][k] = state
totalACGT = 0
for s in range(numStates):
	totalACGT += counts[s]
if (totalACGT*1.0/(numChars*numTaxa-gaps) < 0.2):
	print("\n\nWARNING: PhyIN is built currently only for DNA data. Is this DNA data?\n")

print("Finding conflict (incompatibilities) among neighbouring sites.")

#============================= Finding conflict (incompatibilities) among sites ===================
#=============================
# Mark sites with conflict with neighbours

hasConflict = [False for k in range(numChars)] # boolean array (numChars) of whether in conflict
taxonSequenceStart = [-1 for i in range(numTaxa)] # first non-gap site in taxon i
taxonSequenceEnd = [-1 for i in range(numTaxa)]  # last non-gap site in taxon i
statePairs = [[False for i in range(numStates)] for j in range(numStates)] # integer array i,j for compatibility test

# determine first and last nucleotides of each sequence, since terminal gaps are forgiven
def getFirstInSequence(i):
	for k in range(numChars):
		if (sequences[i][k] != "-"):
			return k
	return -1
def getLastInSequence(i):
	for k in range(numChars):
		if (sequences[i][numChars-k-1] != "-"):
			return numChars-k-1
	return -1
for i in range(numTaxa):
	taxonSequenceStart[i] = getFirstInSequence(i)
	taxonSequenceEnd[i] = getLastInSequence(i)

#.............................
#the function to decide if two sites are incompatible
def areIncompatible (k1, k2):
	#resetting record of state pairs
	for x in range(numStates):
		for y in range(numStates):
			statePairs[x][y] = False
			
	#harvest all patterns between the two columns
	for i in range(numTaxa):
		#Look only at taxa for which k1 and k2 are within their sequence (i.e. not in terminal gap region)
		if (taxonSequenceStart[i] >= 0 and k2 >= taxonSequenceStart[i] and k2 <= taxonSequenceEnd[i] and k2 >= taxonSequenceStart[i] and k2 <= taxonSequenceEnd[i]):
			state1 = nucleotides[i][k1] #state will be returned as 0, 1, 2, 3, 4, -1 (respectively: A, C, G, T, - (if extra state), other)
			state2 = nucleotides[i][k2]
			if (state1 >=0 and state1<numStates and state2 >=0 and state2<numStates):
				statePairs[state1][state2] = True

	#Test of compatibility: Look for cycles in state to state occupancy graph (M. Steel)
	# If there is a cycle, it will be left behind after trimming terminal (single linked) edges from the graph
	#.............................
	def anyOtherConnectForward(s1, s2): # Is s1 paired only with s2, or with others as well?
		for m in range(numStates):
			if (s2 != m and statePairs[s1][m]):
				return True
		return False
	def anyOtherConnectBackward (s1, s2): # Is s2 paired only with s1, or with others as well?
		for m in range(numStates):
			if (s1 != m and statePairs[m][s2]):
				return True
		return False		
	def trimSingleConnects(): #if a state has only one pairing, trim it because it is a singleton
		#turn a statePairs[i][k] to false if it is the only pairing among statePairs[i]
		for s1 in range(numStates):
			for s2 in range(numStates):
				if (statePairs[s1][s2]): #the pairing is present
					if (not anyOtherConnectForward(s1, s2)): 
						statePairs[s1][s2] = False #but it is the only one for s1, and so it is a singleton and should be trimmed
						return True
					if (not anyOtherConnectBackward(s1, s2)):
						statePairs[s1][s2] = False #but it is the only one for s2, and so it is a singleton and should be trimmed
						return True			
		return False	
	#.............................
	# Trim single links iteratively until there are not more
	stopper = 1000 #just in case something goes wrong!
	#trim state pair graph iteratively for single connections
	while trimSingleConnects() and stopper > 0:
		stopper -= 1
		if (stopper == 1):
			print("ALERT SOMETHING WENT WRONG WITH THE PhyIN calculations (trimSingleConnects too many iterations)")
			exit(44)
	#.............................
	#If anything remains, there was a cycle, hence incompatibility
	for s1 in range(numStates):
		for s2 in range(numStates):
			if (statePairs[s1][s2]>0):
				return True

	return False

#.............................
# Mark sites in conflict. Currently looks only to immediate neighbour. 
for k in range(numChars):
	for d in range(1, neighbourDistance+1):
		if (areIncompatible(k, k+d)):
				hasConflict[k] = True
				if (k+d<numChars):
					hasConflict[k+d] = True #if k is in conflict with k+d, reverse must be true as well
	if (verbose and ((k +1) % 100 == 0)):
		print(" ", k, " sites assessed.")


#============================= Finding regions with too much conflict ===================
#=============================
# Scan for blocks with too much conflict. Selects only from conflicting to conflicting, even if that is less than blockLength

toDelete = [False for k in range(numChars)]

#.............................
# Look to see if k is start of bad block. 
def selectBlockByProportion(k):
	if (not hasConflict[k]):
		return
	count = 0
	lastHit = numChars-1
	for b in range(blockLength):
		if (k+b>= numChars):
			break
		if (hasConflict[k+b]):
			count+=1
			lastHit = k+b

	if (1.0*count/blockLength >= propConflict):
		for k2 in range (k, lastHit+1):
			toDelete[k2]= True

#Check all sites to see if they are the start of a bad block
for k in range(numChars):
	selectBlockByProportion(k)

deleted = 0
for k in range(numChars):
	if (toDelete[k]):
		deleted += 1

print("Incompatibilities assessed. Total sites originally:", numChars, " Deleted:", deleted, "Retained:", (numChars-deleted))
print("Writing trimmed sequences to file " + outFileName);

#============================= Writing sequences without high conflict regions (trimmed) ===================
#Sites to delete have been found.  Now to write the output file without them
outputFile = open(outFileName,'w')

for i in range(numTaxa):
	outputFile.write(">")
	outputFile.write(names[i])
	outputFile.write("\n")
	lineLength = 0;
	for k in range(numChars):
		if (not toDelete[k]):
			outputFile.write(sequences[i][k])
			lineLength += 1
			if (lineLength % 50 == 0):
				outputFile.write("\n");
		
			
	outputFile.write("\n") 
	

outputFile.close() 
