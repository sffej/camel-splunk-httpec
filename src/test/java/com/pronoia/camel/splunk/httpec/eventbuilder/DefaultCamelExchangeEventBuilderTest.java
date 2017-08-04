package com.pronoia.camel.splunk.httpec.eventbuilder;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.MessageHistory;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessageHistory;
import org.apache.camel.model.RouteDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DefaultCamelExchangeEventBuilder class.
 */
public class DefaultCamelExchangeEventBuilderTest {
  DefaultCamelExchangeEventBuilder instance;
  Date timestamp = new Date(1498061437000L);

  Exchange exchange;

  @Before
  public void setUp() throws Exception {
    System.setProperty("karaf.name", "test-container");

    List<MessageHistory> messageHistoryList = new LinkedList<>();
    messageHistoryList.add(new DefaultMessageHistory("routeid-one", new RouteDefinition("dummy://uri-one"), new Date()));
    messageHistoryList.add(new DefaultMessageHistory("routeid-two", new RouteDefinition("dummy://uri-two"), new Date()));

    exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.setProperty(Exchange.MESSAGE_HISTORY, messageHistoryList);
    exchange.setProperty(Exchange.CREATED_TIMESTAMP, timestamp);
    exchange.setProperty("AuditCode", "TestAuditCode");
    exchange.setProperty("AuditSource", "TestAuditSource");
    exchange.setProperty("AuditReason", "TestAuditReason");
    exchange.setProperty("AuditText", "TestAuditText");

    Message message = exchange.getIn();
    message.setHeader("breadcrumbId", "ID-lstiesbap01-34111-1497883509001-11-559");
    message.setHeader("CamelMllpAutoAcknowledge", "true");
    message.setHeader("CamelMllpLocalAddress", "/10.12.171.14");
    message.setHeader("JMSCorrelationID", null);
    message.setHeader("JMSCorrelationIDAsBytes", null);
    message.setHeader("JMSDeliveryMode", 2);
    message.setHeader("JMSDestination", "queue://audit.in");
    message.setHeader("JMSExpiration", 0);
    message.setHeader("JMSMessageID", "ID:lstiesbap01-42602-1497883989994-11:1:1:1:67");
    message.setHeader("JMSPriority", "4");
    message.setHeader("JMSRedelivered", "false");
    message.setHeader("JMSReplyTo", null);
    message.setHeader("JMSTimestamp", 1498061437458L);
    message.setHeader("JMSType", null);
    message.setHeader("JMSXGroupID", null);
    message.setHeader("JMSXUserID", null);

    message.setBody("Dummy Body");

    instance = new DefaultCamelExchangeEventBuilder();
    instance.setHost("DummyHost");

    Map<String, String> systemProperties = new HashMap<>();

    systemProperties.put("karaf.name", "container");

    instance.addIncludedSystemProperties(systemProperties);
  }


  @Test
  public void testBuildDefaults() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testNullEvent() throws Exception {
    String eventBody = instance.build();

    assertTrue(eventBody.contains("\"container\":\"test-container\""));
    assertFalse(eventBody.contains("JMSCorrelationID"));
  }

  @Test
  public void testExcludeBody() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\""
            + "},"
            + "\"event\":\"[Camel Message Body was excluded]\"" +
            "}";
    // @formatter:on

    instance.setIncludeBody(false);

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testExcludeMessageHeaders() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setIncludeMessageHeaders(false);

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testIncludeExchangeProperties() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"AuditCode\":\"TestAuditCode\","
            +   "\"container\":\"test-container\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"CamelMessageHistory\":\"[DefaultMessageHistory[routeId=routeid-one, node=null], DefaultMessageHistory[routeId=routeid-two, node=null]]\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"AuditReason\":\"TestAuditReason\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelCreatedTimestamp\":\"Wed Jun 21 10:10:37 MDT 2017\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"AuditSource\":\"TestAuditSource\","
            +   "\"AuditText\":\"TestAuditText\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setIncludeExchangeProperties(true);

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testIncludeHeaderPatter() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"JMSDeliveryMode\":\"2\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setIncludeHeaderPattern("JMS.*");

    assertEquals(EXPECTED, instance.event(exchange).build());
  }
  @Test
  public void testExcludeHeaderPatter() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setExcludeHeaderPattern("JMS.*");

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testIncludePropertyPattern() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"CamelMessageHistory\":\"[DefaultMessageHistory[routeId=routeid-one, node=null], DefaultMessageHistory[routeId=routeid-two, node=null]]\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelCreatedTimestamp\":\"Wed Jun 21 10:10:37 MDT 2017\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setIncludeExchangeProperties(true);
    instance.setIncludePropertyPattern("Camel.*");

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testExcludePropertyPattern() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"AuditCode\":\"TestAuditCode\","
            +   "\"container\":\"test-container\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"AuditReason\":\"TestAuditReason\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"AuditSource\":\"TestAuditSource\","
            +   "\"AuditText\":\"TestAuditText\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\""
            + "},"
            + "\"event\":\"Dummy Body\"" +
            "}";
    // @formatter:on

    instance.setIncludeExchangeProperties(true);
    instance.setExcludePropertyPattern("Camel.*");

    assertEquals(EXPECTED, instance.event(exchange).build());
  }

  @Test
  public void testHeadersInEvent() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\""
            + "},"
            + "\"event\":{"
            +   "\"messageHeaders\":{"
            +     "\"JMSPriority\":\"4\","
            +     "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +     "\"JMSDestination\":\"queue://audit.in\","
            +     "\"JMSExpiration\":\"0\","
            +     "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +     "\"JMSRedelivered\":\"false\","
            +     "\"JMSTimestamp\":\"1498061437458\","
            +     "\"JMSDeliveryMode\":\"2\","
            +     "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +     "\"CamelMllpAutoAcknowledge\":\"true\""
            +   "},"
            +   "\"messageBody\":\"Dummy Body\""
            + "}"
            + "}";
    // @formatter:on

    // instance.setIncludeBody(false);
    instance.headersAsFields = false;

    assertEquals(EXPECTED, instance.event(exchange).build());
  }


  @Test
  public void testPropertiesInEvent() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\""
            + "},"
            + "\"event\":{"
            +   "\"messageBody\":\"Dummy Body\","
            +   "\"exchangeProperties\":{"
            +     "\"AuditCode\":\"TestAuditCode\","
            +     "\"AuditSource\":\"TestAuditSource\","
            +     "\"CamelMessageHistory\":\"[DefaultMessageHistory[routeId=routeid-one, node=null], DefaultMessageHistory[routeId=routeid-two, node=null]]\","
            +     "\"AuditText\":\"TestAuditText\","
            +     "\"AuditReason\":\"TestAuditReason\","
            +     "\"CamelCreatedTimestamp\":\"Wed Jun 21 10:10:37 MDT 2017\""
            +   "}"
            + "}"
            + "}";
    // @formatter:on

    instance.setIncludeExchangeProperties(true);
    instance.propertiesAsFields = false;

    assertEquals(EXPECTED, instance.event(exchange).build());
  }
}