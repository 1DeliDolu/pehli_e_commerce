FROM apache/kafka:4.2.0

ARG JMX_EXPORTER_VERSION=1.0.1

USER root

RUN mkdir -p /opt/jmx-exporter

ADD https://repo.maven.apache.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${JMX_EXPORTER_VERSION}.jar /opt/jmx-exporter/jmx_prometheus_javaagent.jar

COPY docker/kafka-jmx/kafka-jmx-config.yml /opt/jmx-exporter/kafka-jmx-config.yml
