FROM ubuntu:16.04

RUN apt-get update && apt-get install -y \
    git \
    libconfuse0 \
    libftdi1 \
    maven \
    software-properties-common \
    supervisor \
    wget

# Tellstick
RUN echo 'deb http://download.telldus.com/debian/ stable main' >> /etc/apt/sources.list && \
    wget -q http://download.telldus.se/debian/telldus-public.key -O- | apt-key add - && \
    apt-get update && apt-get install -y telldus-core

# Install Java
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Build Tellstick Hazelcast Cluster
RUN rm -rf /tellstick-hazelcast-cluster
RUN git clone https://github.com/Eitraz/tellstick-hazelcast-cluster.git && \
    cd tellstick-hazelcast-cluster && \
    mvn clean package -DskipTests && \
    cp target/tellstick-hazelcast-cluster-*.jar /tellstick-hazelcast-cluster.jar && \
    rm -rf /tellstick-hazelcast-cluster

RUN rm -rf ~/.m2

RUN rm /etc/tellstick.conf
VOLUME /etc/tellstick.conf

EXPOSE 5701

# Install and configure Supervisor
#RUN apt-get update && apt-get install -y supervisor
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
CMD ["/usr/bin/supervisord"]

#WORKDIR /
#CMD ["java", "-jar", "tellstick-hazelcast-cluster.jar"]
