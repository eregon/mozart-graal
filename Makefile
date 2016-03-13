.PHONY: default build bootcompiler compile install_deps test

MOZART2 = ../mozart2
BOOTCOMPILER_JAR = $(MOZART2)/bootcompiler/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar
BOOTCOMPILER_ECLIPSE = $(MOZART2)/bootcompiler/.project

default: build

$(MOZART2):
	cd .. && git clone https://github.com/eregon/mozart2.git
	cd ../mozart2 && git checkout mozart-graal

$(BOOTCOMPILER_JAR): $(MOZART2)
	cd ../mozart2/bootcompiler && ./sbt assembly

$(BOOTCOMPILER_ECLIPSE): $(MOZART2)
	cd ../mozart2/bootcompiler && ./sbt eclipse eclipse-with-source

bootcompiler: $(BOOTCOMPILER_JAR) $(BOOTCOMPILER_ECLIPSE)

target:
	mvn package

install_deps: target

bin:
	mkdir bin

bin/org/mozartoz/truffle/Main.class: bin
	./oz compile

compile: bin/org/mozartoz/truffle/Main.class

build: $(MOZART2) bootcompiler install_deps compile

test:
	./oz
