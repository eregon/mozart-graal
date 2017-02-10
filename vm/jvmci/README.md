To build JVMCI on Linux, you most likely need an older compiler.
The easiest way to do this is via Docker.

First, download the `Java SE Development Kit 8u92` from [the archive](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jdk-8u92-oth-JPR).

Move the downloaded file `jdk-8u92-linux-x64.tar.gz` to this directory next to the `Dockerfile`.

Start Docker if it is not already started:
```
sudo service docker start
```

Build the image:
```
docker build -t mozart-jvmci .
```

Start the image:
```
docker run --rm -it mozart-jvmci
```

And then inside run:
```
git clone https://github.com/eregon/jvmci.git
cd jvmci
mx build
```

Then, in *another* terminal, copy the built jdk:
```
docker cp `docker ps -q -f ancestor=mozart-jvmci -l`:/home/mozart/jvmci/jdk1.8.0_92/product .
# Make sure it works
./product/bin/java -version
# Move it to the right location
mkdir -p ../../../jvmci/jdk1.8.0_92
mv product ../../../jvmci/jdk1.8.0_92
```

Exit the docker container:
```
exit
```
