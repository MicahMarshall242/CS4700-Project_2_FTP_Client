#!/bin/bash
JFLAGS = -g #-classpath $(CLASSPATH)
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# Include all your .java  file names here
CLASSES = \
		Main.java \
		FTPConnection.java \
		SocketWrapper.java \



default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

