FROM benehdictus/cppstats_arm
# FROM jcvf/cppstats:latest
RUN apt-get update && apt-get install -y openjdk-11-jdk
RUN mkdir /home/execution/
WORKDIR /home/execution/
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENTRYPOINT java -Dfile.encoding="UTF-8" -Dsun.jnu.encoding="UTF-8" -Dlog4j.configurationFile=log4j2.xml -Dgit.maxfiles=30 -Dgit.maxdiff=1000 -jar ./ifdef-catcher-assembly-0.1.jar
