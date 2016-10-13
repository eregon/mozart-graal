# Mozart-Graal

[![Build Status](https://travis-ci.org/eregon/mozart-graal.svg?branch=master)](https://travis-ci.org/eregon/mozart-graal)

An implementation of Oz on top of Truffle and Graal.

## Current Status

Early stage.

Many simple tests pass and the Panel is working:
![The Panel On Mozart-Graal](https://pbs.twimg.com/media/Cf_bHhQXIAAtp_X.png)

## Dependencies

* Java SE Development Kit 8u92 from [here](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jdk-8u92-oth-JPR)
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
When asked for which Java, pick Java 1.8.0_92.

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
