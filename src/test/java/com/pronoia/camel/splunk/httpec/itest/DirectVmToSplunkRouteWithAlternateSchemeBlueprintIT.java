package com.pronoia.camel.splunk.httpec.itest;

import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_AUTHORIZATION_TOKEN;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_HOST;
import static com.pronoia.camel.splunk.httpec.itest.SplunkConnectionInfo.SPLUNK_PORT;

import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

public class DirectVmToSplunkRouteWithAlternateSchemeBlueprintIT extends CamelBlueprintTestSupport {
  static final String ALTERNATE_SCHEME = "httpec";
  static final String ENDPOINT_NAME = "local-test";

  @EndpointInject(uri = "mock://" + ALTERNATE_SCHEME + ":splunk-event-collector")
  MockEndpoint target;

  @Override
  protected String getBlueprintDescriptor() {
    return "OSGI-INF/blueprint/directvm-to-splunk-with-alternate-scheme-test.xml";
  }

  @Override
  public String isMockEndpoints() {
    return ALTERNATE_SCHEME + '*';
  }

  @Override
  protected void doPreSetup() throws Exception {
    System.setProperty("karaf.name", "dummy-container");

    super.doPreSetup();
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
    final int totalMessages = 60;
    target.expectedMessageCount(totalMessages);

    for (int i=1; i<=totalMessages; ++i) {
      log.info("Sending event {}", i);
      String event = String.format("%tF Test Event %d from %s", System.currentTimeMillis(), i, this.getClass().getSimpleName());
      template.sendBody("direct-vm://" + ENDPOINT_NAME, event);
      Thread.sleep(100);
    }

    assertMockEndpointsSatisfied();
  }
}
