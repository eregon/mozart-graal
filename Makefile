.PHONY: default build bootcompiler

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

build: $(MOZART2) bootcompiler
	mvn package
