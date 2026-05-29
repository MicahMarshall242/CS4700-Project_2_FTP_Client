#!/bin/bash

# Showing classpath including current directory (.) and jar files
# ":" is the separator between the items included in classpath on linux
# ";" is the separator on windows
ifeq ($(OS), Windows_NT) # Windows environment
	CLASSPATH =.;gson-2.10.1.jar;picocli-4.7.6.jar
else # Assuming Linux/macOS environment
	CLASSPATH =.:gson-2.10.1.jar:picocli-4.7.6.jar
endif
JFLAGS = -g -classpath $(CLASSPATH)
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# Include all your .java  file names here
CLASSES = \
		Main.java \
		FtpConnection.Java



default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

