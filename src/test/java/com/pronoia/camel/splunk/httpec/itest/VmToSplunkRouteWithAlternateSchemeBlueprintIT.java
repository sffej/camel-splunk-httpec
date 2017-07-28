package com.pronoia.camel.splunk.httpec.itest;

import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_AUTHORIZATION_TOKEN;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_HOST;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_PORT;

import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

public class VmToSplunkRouteWithAlternateSchemeBlueprintIT extends CamelBlueprintTestSupport {
  static final String ALTERNATE_SCHEME = "httpec";
  static final String ENDPOINT_NAME = "local-test";

  @EndpointInject(uri = "mock://" + ALTERNATE_SCHEME + ":splunk-event-collector")
  MockEndpoint target;

  @Override
  protected String getBlueprintDescriptor() {
    return "OSGI-INF/blueprint/vm-to-splunk-with-alternate-scheme-test.xml";
  }

  @Override
  public String isMockEndpoints() {
    return ALTERNATE_SCHEME + '*';
  }

  @Override
  protected String setConfigAdminInitialConfiguration(Properties props) {
    props.setProperty("route-id", "test-alternate-scheme");
    props.setProperty("target-component", ALTERNATE_SCHEME);
    props.setProperty("endpoint-name", ENDPOINT_NAME);

    props.setProperty("splunk-host", SPLUNK_HOST);
    props.setProperty("splunk-port", Integer.toString(SPLUNK_PORT));
    props.setProperty("splunk-authorization-token", SPLUNK_AUTHORIZATION_TOKEN);

    props.setProperty("splunk-index", "fuse-dev");

    props.setProperty("validate-certificates", "false");

    return this.getClass().getSimpleName();
  }

  /**
   * Description of test.
   *
   * @throws Exception in the event of a test error.
   */
  @Test
  public void testSingleEvent() throws Exception {
    target.expectedMessageCount(1);

    String event = String.format("%tF Test Event from %s", System.currentTimeMillis(), this.getClass().getSimpleName());
    template.sendBody("direct-vm://" + ENDPOINT_NAME, event);


    assertMockEndpointsSatisfied();
  }


  /**
   * Description of test.
   *
   * @throws Exception in the event of a test error.
   */
  @Test
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
