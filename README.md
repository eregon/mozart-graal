# Mozart-Graal

An implementation of Oz on top of Truffle and Graal.

## Current Status

Very early stage.
The Base and Init functors can be loaded, and some simple tests pass.

## Dependencies

* Java 8
* C/C++ toolchain (`build-essential`) for building Graal
* Python 2.7 (for `mx`)
* Ruby >= 2.0.0 (for the launcher)

## Build instructions

```bash
mkdir mozart-dev
cd mozart-dev
git clone https://github.com/eregon/mozart-graal.git
cd mozart-graal
make
```

Run with
```bash
./oz
```
