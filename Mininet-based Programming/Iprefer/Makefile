JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java --release 8

CLASSES = \
	Iprefer.java \
	IpreferClient.java \
	IpreferServer.java

default: classes
classes: $(CLASSES:.java=.class)
  
clean:
	$(RM) *.class
