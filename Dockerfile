FROM openjdk:8
ENV JAVA_OPTS="-Xmx4096m -Xms2048m"
ADD target/routingengine.jar routingengine.jar
RUN wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -
RUN apt-get install apt-transport-https
RUN echo "deb https://artifacts.elastic.co/packages/8.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-8.x.list
RUN apt-get update && apt-get install filebeat
RUN update-rc.d filebeat defaults 95 10
COPY src/main/resources/filebeat.yml /etc/filebeat/filebeat.yml
CMD filebeat -c /etc/filebeat/filebeat.yml & java -jar /routingengine.jar