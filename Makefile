.PHONY: default build bootcompiler install_deps graal compile test

MOZART2 = ../mozart2
BOOTCOMPILER_JAR = $(MOZART2)/bootcompiler/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar
BOOTCOMPILER_ECLIPSE = $(MOZART2)/bootcompiler/.project
MX = ../mx/mx
GRAAL = ../graal-coro
MAIN_CLASS = bin/org/mozartoz/truffle/Main.class

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

$(MX):
	cd .. && git clone https://github.com/graalvm/mx.git

$(GRAAL): $(MX)
	cd .. && git clone -b coro https://github.com/eregon/graal-core.git graal-coro
	cd ../graal-coro && $(MX) --vm server build

graal: $(GRAAL)

bin:
	mkdir bin

$(MAIN_CLASS): bin
	./oz compile

compile: $(MAIN_CLASS)

build: $(MOZART2) bootcompiler install_deps graal compile

test:
	./oz
