# Mozart-Graal

An implementation of Oz on top of Truffle and Graal.

## Current Status

Early stage.

Many simple tests pass and the Panel is working:
![The Panel On Mozart-Graal](https://pbs.twimg.com/media/Cf_bHhQXIAAtp_X.png)

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
