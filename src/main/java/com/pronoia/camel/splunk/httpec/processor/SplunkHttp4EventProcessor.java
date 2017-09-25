package com.pronoia.camel.splunk.httpec.processor;

import com.pronoia.camel.splunk.httpec.eventbuilder.CamelExchangeEventBuilder;
import com.pronoia.camel.splunk.httpec.eventbuilder.DefaultCamelExchangeEventBuilder;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.http4.HttpMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplunkHttp4EventProcessor implements Processor {
  public static final String SPLUNK_AUTHORIZATION_HEADER = "Authorization";

  Logger log = LoggerFactory.getLogger(this.getClass());

  String splunkHost = "127.0.0.1";
  Integer splunkPort = 8088;
  String splunkAuthorizationToken;

  CamelExchangeEventBuilder splunkEventBuilder = new DefaultCamelExchangeEventBuilder();

  String cachedAuthorizationHeader;

  public boolean hasSplunkHost() {
    return splunkHost != null && !splunkHost.isEmpty();
  }

  public String getSplunkHost() {
    return splunkHost;
  }

  public void setSplunkHost(String splunkHost) {
    this.splunkHost = splunkHost;
  }

  public boolean hasSplunkPort() {
    return splunkPort != null;
  }

  public Integer getSplunkPort() {
    return splunkPort;
  }

  public void setSplunkPort(Integer splunkPort) {
    this.splunkPort = splunkPort;
  }

  public boolean hasSplunkAuthorizationToken() {
    return splunkAuthorizationToken != null && !splunkAuthorizationToken.isEmpty();
  }

  public String getSplunkAuthorizationToken() {
    return splunkAuthorizationToken;
  }

  public void setSplunkAuthorizationToken(String splunkAuthorizationToken) {
    this.splunkAuthorizationToken = splunkAuthorizationToken;
  }

  public boolean hasSplunkEventBuilder() {
    return splunkEventBuilder != null;
  }

  public CamelExchangeEventBuilder getSplunkEventBuilder() {
    return splunkEventBuilder;
  }

  public void setSplunkEventBuilder(CamelExchangeEventBuilder splunkEventBuilder) {
    this.splunkEventBuilder = splunkEventBuilder;
  }

  public void verifyConfiguration() {
    if (!hasSplunkHost()) {
      throw new IllegalStateException("Splunk Host must be specified");
    }

    if (!hasSplunkPort()) {
      throw new IllegalStateException("Splunk Port must be specified");
    }

    if (!hasSplunkAuthorizationToken()) {
      throw new IllegalStateException("Splunk Authorization Token must be specified");
    }

    if (!hasSplunkEventBuilder()) {
      splunkEventBuilder = new DefaultCamelExchangeEventBuilder();
      log.warn("Splunk EventBuilder<Exchange> not specified using {} as default", splunkEventBuilder.getClass().getName());
    }

    this.cachedAuthorizationHeader = String.format("Splunk %s", getSplunkAuthorizationToken());
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    if (cachedAuthorizationHeader == null) {
      verifyConfiguration();
    }

    // Build the new body before adding all the CamelHttp headers
    String eventBody = splunkEventBuilder.eventBody(exchange).build();

    Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

    message.setBody(eventBody);
    message.setHeader(Exchange.HTTP_URI,  String.format("https4://%s:%d", splunkHost, splunkPort));
    message.setHeader(Exchange.HTTP_PATH, "services/collector");
    message.setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);

    // Splunk HTTP Event Collector Headers
    message.setHeader(SPLUNK_AUTHORIZATION_HEADER, cachedAuthorizationHeader);
  }

}
