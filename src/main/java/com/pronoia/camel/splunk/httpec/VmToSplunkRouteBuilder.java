package com.pronoia.camel.splunk.httpec;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.camel.LoggingLevel;

public class VmToSplunkRouteBuilder extends Http4ToSplunkRouteBuilderSupport {
  Integer size;
  Integer concurrentConsumers;

  int maximumRedeliveries = 5;
  long redeliveryDelay = 100;

  @Override
  public void configure() throws Exception {
    super.configure();

    StringBuilder sedaUriBuilder = new StringBuilder("seda://splunk-event-collector?waitForTaskToComplete=Never&purgeWhenStopping=true");
    if (hasSize()) {
      sedaUriBuilder.append("&size=")
          .append(size);
    }

    if (hasConcurrentConsumers()) {
      sedaUriBuilder.append("&concurrentConsumers=")
          .append(concurrentConsumers);
    }

    fromF(sedaUriBuilder.toString()).routeId(routeId + "-delivery")
        .onException(IOException.class, ConnectException.class)
            .handled(true)
            .maximumRedeliveries(maximumRedeliveries)
            .redeliveryDelay(redeliveryDelay)
            .logExhausted(false)
            .logExhaustedMessageBody(false)
            .log(getDeliveryFailureLoggingLevel(), getDeliveryFailureMessage())
            .log(getFailedBodyLoggingLevel(), getFailedBodyMessage())
            .end()
        .toF("%s://splunk-event-collector", getTargetComponent()).id("Send to Splunk")
    ;

    fromF("direct-vm://%s", getSourceEndpointName()).routeId(routeId + "-receiver")
        .onException(IllegalStateException.class)
          .onWhen(simple("${exception.message} == 'Queue full'"))
          .handled(true)
          .log(LoggingLevel.ERROR, "Failed to deliver event: ${body} - ${exception}")
          .end()
        .process(getSplunkEventProcessor())
        .toF("seda://splunk-event-collector", getTargetComponent()).id("Queue for deliver")
    ;

  }

  public boolean hasSize() {
    return size != null;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public boolean hasConcurrentConsumers() {
    return concurrentConsumers != null;
  }

  public Integer getConcurrentConsumers() {
    return concurrentConsumers;
  }

  public void setConcurrentConsumers(Integer concurrentConsumers) {
    this.concurrentConsumers = concurrentConsumers;
  }

  public int getMaximumRedeliveries() {
    return maximumRedeliveries;
  }

  public void setMaximumRedeliveries(int maximumRedeliveries) {
    this.maximumRedeliveries = maximumRedeliveries;
  }

  public long getRedeliveryDelay() {
    return redeliveryDelay;
  }

  public void setRedeliveryDelay(long redeliveryDelay) {
    this.redeliveryDelay = redeliveryDelay;
  }

}
