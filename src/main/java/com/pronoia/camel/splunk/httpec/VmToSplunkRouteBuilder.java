package com.pronoia.camel.splunk.httpec;

import java.io.IOException;
import java.net.ConnectException;
import java.util.StringJoiner;

import org.apache.camel.LoggingLevel;

public class VmToSplunkRouteBuilder extends Http4ToSplunkRouteBuilderSupport {
  static final String INTERNAL_DELIVERY_QUEUE_NAME = "splunk-delivery-queue";
  static final String DEFAULT_SEDA_OPTIONS = "waitForTaskToComplete=Never&purgeWhenStopping=true";

  Integer size;
  Integer concurrentConsumers;

  int maximumRedeliveries = 5;
  long redeliveryDelay = 100;

  @Override
  public void configure() throws Exception {
    super.configure();

    StringBuilder sedaUriBuilder = new StringBuilder("seda://");
    sedaUriBuilder.append(INTERNAL_DELIVERY_QUEUE_NAME);

    StringJoiner optionsJoiner = new StringJoiner("&", "?", "");
    optionsJoiner.setEmptyValue("");

    if (hasDefaultSedaOptions()) {
      optionsJoiner.add(DEFAULT_SEDA_OPTIONS);
    }

    if (hasSize()) {
      optionsJoiner.add("size=" + size);
    }

    if (hasConcurrentConsumers()) {
      optionsJoiner.add("concurrentConsumers=" + concurrentConsumers);
    }

    sedaUriBuilder.append(optionsJoiner.toString());

    // @formatter:off
    fromF(sedaUriBuilder.toString()).routeId(routeId + " delivery")
        .onException(IOException.class, ConnectException.class)
          .handled(true)
          .maximumRedeliveries(maximumRedeliveries)
          .redeliveryDelay(redeliveryDelay)
          .logExhausted(false)
          .logExhaustedMessageBody(false)
            .log(getDeliveryFailureLoggingLevel(), getDeliveryFailureMessage()).id("Log Delivery Failure")
            .log(getFailedBodyLoggingLevel(), getFailedBodyMessage()).id("Log Failed Body")
          .end()
        .toF("%s://splunk-event-collector", getTargetComponent()).id("Send to Splunk");
    // @formatter:on

    // @formatter:off
    fromF("direct-vm://%s", getSourceEndpointName()).routeId(routeId + " receiver")
        .onException(IllegalStateException.class).id(IllegalStateException.class.getSimpleName() + " handler")
          .onWhen(simple("${exception.message} == 'Queue full'"))
          .handled(true)
            .log(LoggingLevel.ERROR, "Failed to deliver event: ${body} - ${exception}").id("Log 'Queue full' delivery failure")
          .end()
        .process(getSplunkEventProcessor()).id("Prepare Splunk JSON payload")
        .toF("seda://%s", INTERNAL_DELIVERY_QUEUE_NAME).id("Queue for delivery");
    // @formatter:on

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

  boolean hasDefaultSedaOptions() {
    return DEFAULT_SEDA_OPTIONS != null && !DEFAULT_SEDA_OPTIONS.isEmpty();
  }

}
