package com.pronoia.camel.splunk.httpec.itest;

import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_AUTHORIZATION_TOKEN;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_HOST;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_PORT;

import com.pronoia.camel.splunk.httpec.DirectVmToSplunkRouteBuilder;
import com.pronoia.camel.splunk.httpec.eventbuilder.DefaultCamelExchangeEventBuilder;
import com.pronoia.camel.splunk.httpec.processor.SplunkHttp4EventProcessor;

import org.apache.camel.EndpointInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class DirectVmToSplunkRouteIT extends CamelTestSupport {
  static final String TRIGGER_ENDPOINIT = "event-source";

  static final String SCHEME = "https4";
  static final String ENDPOINT = "local-test";

  @EndpointInject(uri = "mock://" + SCHEME + ":splunk-event-collector")
  MockEndpoint target;

  @Override
  public String isMockEndpoints() {
    return SCHEME + '*';
  }

  RoutesBuilder createDirectVmRouteBuilder() throws Exception {
    DirectVmToSplunkRouteBuilder builder = new DirectVmToSplunkRouteBuilder();

    builder.setRouteId("test-route-2");

    builder.setTargetComponent(SCHEME);
    builder.setSourceEndpointName(ENDPOINT);
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

  RoutesBuilder createFeedingRouteBuilder() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct://" + TRIGGER_ENDPOINIT)
            .to("direct-vm://" + ENDPOINT)
        ;
      }
    };
  }

  @Override
  protected RoutesBuilder[] createRouteBuilders() throws Exception {
    return new RoutesBuilder[]{ createFeedingRouteBuilder(), createDirectVmRouteBuilder()};
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
    template.sendBody("direct://" + TRIGGER_ENDPOINIT, event);

    assertMockEndpointsSatisfied();
  }

}
