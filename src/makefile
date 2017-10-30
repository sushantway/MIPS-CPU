JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        simulator.java \
        Simul.java \
        Instr.java \
        Data.java \
        Config.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class