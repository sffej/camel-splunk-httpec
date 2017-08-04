package com.pronoia.camel.splunk.httpec;

public class DirectVmToSplunkRouteBuilder extends Http4ToSplunkRouteBuilderSupport {
  @Override
  public void configure() throws Exception {
    super.configure();

    // @formatter:off
    fromF("direct-vm://%s", getSourceEndpointName()).routeId(routeId)
        .process(getSplunkEventProcessor())
        .doTry()
          .toF("%s://splunk-event-collector", getTargetComponent()).id("Send to Splunk")
        .doCatch(Exception.class)
          .log(getDeliveryFailureLoggingLevel(), getDeliveryFailureMessage()).id("Log Delivery Failure")
          .log(getFailedBodyLoggingLevel(), getFailedBodyMessage()).id("Log Failed Body")
        .endDoTry();
    // @formatter:on
  }

}
