package com.pronoia.camel.splunk.httpec.itest;

import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_AUTHORIZATION_TOKEN;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_HOST;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_PORT;

import com.pronoia.camel.splunk.httpec.VmToSplunkRouteBuilder;
import com.pronoia.camel.splunk.httpec.eventbuilder.DefaultCamelExchangeEventBuilder;
import com.pronoia.camel.splunk.httpec.processor.SplunkHttp4EventProcessor;

import org.apache.camel.EndpointInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class VmToSplunkRouteWithAlternateSchemeIT extends CamelTestSupport {
  static final String ALTERNATE_SCHEME = "httpec";
  static final String ENDPOINT_NAME = "local-test";

  @EndpointInject(uri = "mock://" + ALTERNATE_SCHEME + ":splunk-event-collector")
  MockEndpoint target;


  @Override
  public String isMockEndpoints() {
    return ALTERNATE_SCHEME + '*';
  }

  @Override
  protected JndiRegistry createRegistry() throws Exception {
    JndiRegistry registry = super.createRegistry();

    HttpComponent httpec = new HttpComponent();

    registry.bind(ALTERNATE_SCHEME, httpec);

    return registry;
  }

  @Override
  protected RoutesBuilder createRouteBuilder() throws Exception {
    VmToSplunkRouteBuilder builder = new VmToSplunkRouteBuilder();

    builder.setRouteId("test-alternate-scheme");

    builder.setTargetComponent(ALTERNATE_SCHEME);
    builder.setSourceEndpointName(ENDPOINT_NAME);

    builder.setValidateCertificates(false);

    SplunkHttp4EventProcessor eventProcessor = new SplunkHttp4EventProcessor();
    eventProcessor.setSplunkHost(SPLUNK_HOST);
    eventProcessor.setSplunkPort(SPLUNK_PORT);
    eventProcessor.setSplunkAuthorizationToken(SPLUNK_AUTHORIZATION_TOKEN);

    DefaultCamelExchangeEventBuilder eventBuilder = new DefaultCamelExchangeEventBuilder();
    eventBuilder.setIndex("fuse-dev");
    eventBuilder.includeSystemProperty("karaf.name");

    eventProcessor.setExchangeEventBuilder( eventBuilder);

    builder.setSplunkEventProcessor( eventProcessor );

    return builder;
  }

  /**
   * Description of test.
   *
   * @throws Exception in the event of a test error.
   */
  @Test
  public void testSingleMessage() throws Exception {
    target.expectedMessageCount(1);

    String event = String.format("%tF Test Event from %s", System.currentTimeMillis(), this.getClass().getSimpleName());
    template.sendBody("direct-vm://" + ENDPOINT_NAME, event);

    assertMockEndpointsSatisfied();
  }

  public void testMultipleEvents() throws Exception {
    final int numberOfLoops = 3;
    final int messagesPerLoop = 10;
    final long delayBetweenLoops = 5000;
    final long delayBetweenMessages = 100;

    target.expectedMessageCount(numberOfLoops * messagesPerLoop);

    for (int loop = 0; loop < numberOfLoops; ++loop) {
      for (int message = 1; message <= messagesPerLoop; ++message) {
        int messageNumber = (loop * messagesPerLoop) + message;
        log.info("Sending event {}", messageNumber);
        String event = String.format("%tF Test Event %d from %s", System.currentTimeMillis(), messageNumber, this.getClass().getSimpleName());
        template.sendBody("direct-vm://" + ENDPOINT_NAME, event);
        ++messageNumber;
        Thread.sleep(delayBetweenMessages);
      }
      Thread.sleep(delayBetweenLoops);
    }

    assertMockEndpointsSatisfied();
  }
}
