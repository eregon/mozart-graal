FROM buildpack-deps:jessie
RUN apt-get update && apt-get install -y --no-install-recommends \
            libasound2-dev \
            libcups2-dev \
            libxtst-dev \
            unzip \
            zip \
        && rm -rf /var/lib/apt/lists/*

RUN useradd -m mozart
USER mozart
WORKDIR /home/mozart
RUN git clone https://github.com/graalvm/mx.git /home/mozart/mx
ENV PATH /home/mozart/mx:$PATH
ADD jdk-8u92-linux-x64.tar.gz .
ENV JAVA_HOME /home/mozart/jdk1.8.0_92
