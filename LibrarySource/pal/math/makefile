### VARIABLES ###

JIKESOPTS := +P
GCJOPTS := 

# Always check dependencies
JIKESOPTS += +M

SRC := $(wildcard *.java)
CLS := $(patsubst %.java, %.class, $(SRC))
OBJ := $(patsubst %.class, %.o, $(wildcard *.class))


### RULES ###

# Compile Java sources into class files
%.class: %.java
	jikes $(JIKESOPTS) $<

# Alternative to using jikes: gcj -C
	
# Compile class files into native code
%.o: %.class
	gcj -c $(GCJOPTS) $<

	
### TARGETS ###

.PHONY: classes native clean

classes: $(CLS)

ifneq ($(OBJ),)
native: $(OBJ)
	(ar -rv package.a *.o; ranlib package.a)
else
native: $(OBJ)
endif
	
clean:
	rm -f *.class *.o *.u *.a *~

