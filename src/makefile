# Location of trees.
#SOURCE_DIR := /Users/wzx/Development/DistributedSystems/src/A1
#OUTPUT_DIR := /Users/wzx/Development/RMI/assignment1
SOURCE_DIR := .
OUTPUT_DIR := ./classes
# Java tools
JFLAGS := -sourcepath $(SOURCE_DIR)\
               -d $(OUTPUT_DIR)\
               -source 1.8
JAVADOC     := javadoc
JDFLAGS     := -sourcepath $(SOURCE_DIR)        \
               -d $(OUTPUT_DIR)                 \
               -link http://java.sun.com/products/jdk/1.8/docs/api            
 # Unix tools
 AWK         := awk
 FIND        := find
 MKDIR       := mkdir -p
 RM          := rm -rf
 SHELL       := /bin/bash
 
.PHONY: all
all: compile

hello:
	@echo $(all_javas)

# all_javas - Temp file for holding source file list
all_javas := $(OUTPUT_DIR)/all.javas
# make-directories - Ensure output directory exists.
make-directories := $(shell $(MKDIR) $(OUTPUT_DIR))

# compile - Compile the source
.PHONY: compile
compile: $(all_javas)
	javac $(JFLAGS) @$<

# all_javas - Gather source file list
.INTERMEDIATE: $(all_javas)
$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@

# javadoc - Generate the Java doc from sources
.PHONY: javadoc
javadoc: $(all_javas)
	$(JAVADOC) $(JDFLAGS) @$<

.PHONY: clean
clean:
	$(RM) $(OUTPUT_DIR)

register:
	cd $(OUTPUT_DIR);
	rmiregistry 

#moveTo:
#	cd $(OUTPUT_DIR);

startServer:
	cd $(OUTPUT_DIR);
	java -classpath $(OUTPUT_DIR) -Djava.rmi.server.codebase=file:$(OUTPUT_DIR)/ A1.CalculatorServer &

startClient:
	cd $(OUTPUT_DIR);
	java -classpath $(OUTPUT_DIR) A1.CalculatorClient

