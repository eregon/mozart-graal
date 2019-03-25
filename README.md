# Mozart-Graal

[![Build Status](https://travis-ci.org/eregon/mozart-graal.svg?branch=master)](https://travis-ci.org/eregon/mozart-graal)

An implementation of the [Oz programming language](https://en.wikipedia.org/wiki/Oz_(programming_language)) on top of Truffle and Graal.

The bootcompiler and Oz libraries are imported from [Mozart 2](https://github.com/mozart/mozart2).

## Interesting Points

* A [master thesis](https://www.info.ucl.ac.be/~pvr/MemoireMaximeIstasse.pdf) was made with this project to optimize performance. It details many static and dynamic optimizations.

* Tail calls are optimized and compiled as loops, see `TailCallCatcherNode`.
  Self-recursion is optimized further in `SelfTailCallCatcherNode`.

* There is a Truffle AST serializer in `OzSerializer` using [Kryo](https://github.com/EsotericSoftware/kryo) able to serialize Oz code.

* The project uses the Coroutine patch for HotSpot to be able to create many lightweight threads.

* The parser, typer, and some optimizations are written in Scala (`bootcompiler/`)
  and the AST produced by Scala is then translated to a Truffle AST in `Translator`.

## Current Status

Early stage but the Panel and Browser are working.

![The Panel On Mozart-Graal](https://pbs.twimg.com/media/Cf_bHhQXIAAtp_X.png)

## Dependencies

* Java 8
* Maven 3
* C/C++ toolchain (`build-essential`) for building Graal
* Python 2.7 (for `mx`)
* Ruby >= 2.2.0 (for the launcher)

## Build instructions

```bash
mkdir mozart-dev
cd mozart-dev
git clone https://github.com/eregon/mozart-graal.git
cd mozart-graal
rake
```

It takes around 5 min to build everything.

Run with
```bash
./oz
```

## Graal

Graal is not built by default to save time.
To build it, run
```bash
rake build:graal
```

On recent Linux, you need to do part of the build manually, see [Building JVMCI](vm/jvmci/README.md).

## IDE: IntelliJ IDEA

IntelliJ IDEA configuration are checked in the repository and should work out of the box.
Import `mozart-dev` as a project.
