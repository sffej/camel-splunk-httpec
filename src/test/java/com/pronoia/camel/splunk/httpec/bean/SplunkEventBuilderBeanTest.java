package com.pronoia.camel.splunk.httpec.bean;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.MessageHistory;
import org.apache.camel.impl.DefaultMessageHistory;
import org.apache.camel.model.RouteDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SplunkEventBuilderBean class.
 */
public class SplunkEventBuilderBeanTest {
  static final String BODY = "MSH|^~\\&|HAR|1000|JCAPS|CC|20170621091034|U0013110|ADT^A08|15291|D|2.3\n" +
      "EVN|A08|20170621091034||REG_UPDATE|U0013110^RAPOPORT^SVETLANA^^^^^^UCLA^^^^^RRMC\n" +
      "PID|1|4489163^^^MRN^MRN|4489163^^^MRN^MRN||DEEPA^KALI||19800929|F||I|345 W WASHINGTON AVE^^AGOURA HILLS^CA^91377^USA^P^^VENTURA|VENTURA|(661)123-1234^P^PH^^^661^1231234||ENGLISH|M||10004035||||N||||||||N\n" +
      "PD1|||WW DORIS STEIN EYE RS CTR^^6010\n" +
      "NK1|1|HUBBY^THREE|SPO|345 W washington ave^^AGOURA HILLS^CA^91377^USA|(661)123-1234^^^^^661^1231234|(661)232-1234^^^^^661^2321234|Emergency Contact 1";

  SplunkEventBuilderBean instance;
  Map<String, Object> headers;
  List<MessageHistory> messageHistoryList = new ArrayList();
  Date timestamp = new Date(1498061437000L);

  @Before
  public void setUp() throws Exception {
    System.setProperty("karaf.name","test-container");

    instance = new SplunkEventBuilderBean();
    instance.setHost("DummyHost");

    headers = new HashMap<>();
    headers.put("breadcrumbId", "ID-lstiesbap01-34111-1497883509001-11-559");
    headers.put("CamelMllpAutoAcknowledge", "true");
    headers.put("CamelMllpLocalAddress", "/10.12.171.14");
    headers.put("JMSCorrelationID", null);
    headers.put("JMSCorrelationIDAsBytes", null);
    headers.put("JMSDeliveryMode", "2");
    headers.put("JMSDestination", "queue://audit.in");
    headers.put("JMSExpiration", "0");
    headers.put("JMSMessageID", "ID:lstiesbap01-42602-1497883989994-11:1:1:1:67");
    headers.put("JMSPriority", "4");
    headers.put("JMSRedelivered", "false");
    headers.put("JMSReplyTo", null);
    headers.put("JMSTimestamp", "1498061437458");
    headers.put("JMSType", null);
    headers.put("JMSXGroupID", null);
    headers.put("JMSXUserID", null);
    headers.put("ucla_MSH-10", "15291");
    headers.put("ucla_MSH-11", "D");
    headers.put("ucla_MSH-12", "2.3");
    headers.put("ucla_MSH-3", "HAR");
    headers.put("ucla_MSH-4", "1000");
    headers.put("ucla_MSH-5", "JCAPS");
    headers.put("ucla_MSH-6", "CC");
    headers.put("ucla_MSH-7", "20170621091034");
    headers.put("ucla_MSH-8", "U0013110");
    headers.put("ucla_MSH-9", "ADT^A08");
    headers.put("ucla_MSH-9-1", "ADT");
    headers.put("ucla_MSH-9-2", "A08");
    headers.put("ucla_PID-18", "10004035");
    headers.put("ucla_PID-3-1", "4489163");
    headers.put("ucla_PV1-19", "60000102834");
    headers.put("ucla_PV1-2", "SPECIMEN");
    headers.put("ucla_PV1-3", "PATHLAB 145");
    headers.put("UCLA.AuditCode", "AuditCode");
    headers.put("UCLA.AuditSource", "AuditSource");
    headers.put("UCLA.AuditReason", "AuditReason");
    headers.put("UCLA.AuditText", "AuditText");

    messageHistoryList = new ArrayList();
    DefaultMessageHistory defaultMessageHistory = new DefaultMessageHistory("This is a routeid", new RouteDefinition("sampleURI"), new Date());
    DefaultMessageHistory defaultMessageHistory2 = new DefaultMessageHistory("This is another routeid", new RouteDefinition("sampleURI"), new Date());
    messageHistoryList.add(defaultMessageHistory);
    messageHistoryList.add(defaultMessageHistory2);

  }

  @Test
  public void testBuildEvent() throws Exception {
    // @formatter:off
    String EXPECTED =
        "{"
            + "\"host\":\"DummyHost\","
            + "\"time\":\"1498061437.000\",\""
            + "fields\":{"
            +   "\"container\":\"test-container\","
            +   "\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\","
            +   "\"ucla_MSH-11\":\"D\","
            +   "\"UCLA.AuditText\":\"AuditText\","
            +   "\"ucla_MSH-10\":\"15291\","
            +   "\"JMSRedelivered\":\"false\","
            +   "\"UCLA.AuditCode\":\"AuditCode\","
            +   "\"ucla_PID-3-1\":\"4489163\","
            +   "\"JMSDeliveryMode\":\"2\","
            +   "\"ucla_MSH-6\":\"CC\","
            +   "\"ucla_MSH-12\":\"2.3\","
            +   "\"ucla_MSH-5\":\"JCAPS\","
            +   "\"ucla_MSH-4\":\"1000\","
            +   "\"CamelMllpAutoAcknowledge\":\"true\","
            +   "\"ucla_MSH-3\":\"HAR\","
            +   "\"ucla_MSH-9\":\"ADT^A08\","
            +   "\"ucla_MSH-8\":\"U0013110\","
            +   "\"ucla_MSH-7\":\"20170621091034\","
            +   "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\","
            +   "\"JMSExpiration\":\"0\","
            +   "\"ucla_PV1-19\":\"60000102834\","
            +   "\"JMSTimestamp\":\"1498061437458\","
            +   "\"UCLA.AuditReason\":\"AuditReason\","
            +   "\"UCLA.AuditSource\":\"AuditSource\","
            +   "\"ucla_PID-18\":\"10004035\","
            +   "\"JMSPriority\":\"4\","
            +   "\"JMSDestination\":\"queue://audit.in\","
            +   "\"ucla_PV1-2\":\"SPECIMEN\","
            +   "\"ucla_PV1-3\":\"PATHLAB 145\","
            +   "\"CamelMllpLocalAddress\":\"/10.12.171.14\","
            +   "\"ucla_MSH-9-2\":\"A08\","
            +   "\"ucla_MSH-9-1\":\"ADT\","
            +   "\"CallingRouteId\":\"This is a routeid\","
            +   "\"TriggeredAuditRouteId\":\"This is another routeid\""
            + "},"
            + "\"event\":\"MSH|^~\\\\&|HAR|1000|JCAPS|CC|20170621091034|U0013110|ADT^A08|15291|D|2.3\\nEVN|A08|20170621091034||REG_UPDATE|U0013110^RAPOPORT^SVETLANA^^^^^^UCLA^^^^^RRMC\\nPID|1|4489163^^^MRN^MRN|4489163^^^MRN^MRN||DEEPA^KALI||19800929|F||I|345 W WASHINGTON AVE^^AGOURA HILLS^CA^91377^USA^P^^VENTURA|VENTURA|(661)123-1234^P^PH^^^661^1231234||ENGLISH|M||10004035||||N||||||||N\\nPD1|||WW DORIS STEIN EYE RS CTR^^6010\\nNK1|1|HUBBY^THREE|SPO|345 W washington ave^^AGOURA HILLS^CA^91377^USA|(661)123-1234^^^^^661^1231234|(661)232-1234^^^^^661^2321234|Emergency Contact 1\"" +
            "}";
    // @formatter:on

    final String OLD_EXPECTED = "{\"host\":\"DummyHost\",\"time\":\"1498061437.000\",\"fields\":{\"container\":\"test-container\",\"JMSMessageID\":\"ID:lstiesbap01-42602-1497883989994-11:1:1:1:67\"," +
        "\"ucla_MSH-11\":\"D\",\"UCLA.AuditText\":\"AuditText\",\"ucla_MSH-10\":\"15291\",\"JMSRedelivered\":\"false\",\"UCLA.AuditCode\":\"AuditCode\",\"ucla_PID-3-1\":\"4489163\"," +
        "\"JMSDeliveryMode\":\"2\",\"ucla_MSH-6\":\"CC\",\"ucla_MSH-12\":\"2.3\",\"ucla_MSH-5\":\"JCAPS\",\"ucla_MSH-4\":\"1000\",\"CallingRouteId\":\"THis is a routeid\"," +
        "\"CamelMllpAutoAcknowledge\":\"true\",\"ucla_MSH-3\":\"HAR\",\"ucla_MSH-9\":\"ADT^A08\",\"ucla_MSH-8\":\"U0013110\",\"ucla_MSH-7\":\"20170621091034\"," +
        "\"breadcrumbId\":\"ID-lstiesbap01-34111-1497883509001-11-559\",\"TriggeredAuditRouteId\":\"THis is another routeid\",\"JMSExpiration\":\"0\",\"ucla_PV1-19\":\"60000102834\"," +
        "\"JMSTimestamp\":\"1498061437458\",\"UCLA.AuditReason\":\"AuditReason\",\"UCLA.AuditSource\":\"AuditSource\",\"ucla_PID-18\":\"10004035\",\"JMSPriority\":\"4\"," +
        "\"JMSDestination\":\"queue:\\/\\/audit.in\",\"ucla_PV1-2\":\"SPECIMEN\",\"ucla_PV1-3\":\"PATHLAB 145\",\"CamelMllpLocalAddress\":\"\\/10.12.171.14\",\"ucla_MSH-9-2\":\"A08\"," +
        "\"ucla_MSH-9-1\":\"ADT\"},\"event\":\"MSH|^~\\\\&|HAR|1000|JCAPS|CC|20170621091034|U0013110|ADT^A08|15291|D|2.3\\nEVN|A08|20170621091034||REG_UPDATE|U0013110^RAPOPORT^SVETLANA^^^^^^UCLA^^^^^RRMC\\nPID|1|4489163^^^MRN^MRN|4489163^^^MRN^MRN||DEEPA^KALI||19800929|F||I|345 W WASHINGTON AVE^^AGOURA HILLS^CA^91377^USA^P^^VENTURA|VENTURA|(661)123-1234^P^PH^^^661^1231234||ENGLISH|M||10004035||||N||||||||N\\nPD1|||WW DORIS STEIN EYE RS CTR^^6010\\nNK1|1|HUBBY^THREE|SPO|345 W washington ave^^AGOURA HILLS^CA^91377^USA|(661)123-1234^^^^^661^1231234|(661)232-1234^^^^^661^2321234|Emergency Contact 1\"}";

    String eventBody = instance.buildEvent(timestamp, messageHistoryList, headers, BODY);

    assertEquals(EXPECTED, eventBody);
  }

  @Test
  public void testNullEvent() throws Exception {
    String eventBody = instance.buildEvent(timestamp, messageHistoryList, headers, BODY);
    assertFalse(eventBody.contains("JMSCorrelationID"));
  }

  @Test
  public void testOverrideBody() throws Exception {
    instance.setOverrideBody("breadcrumbId");
    String eventBody = instance.buildEvent(timestamp, messageHistoryList, headers, BODY);
    assertTrue(eventBody.contains("\"event\":\""+headers.get("breadcrumbId").toString()+"\""));
    assertFalse(eventBody.contains("\"breadcrumbId\":\""+headers.get("breadcrumbId").toString()+"\""));
  }

  @Test
  public void testContainerValue() throws Exception {
    String eventBody = instance.buildEvent(timestamp, messageHistoryList, headers, BODY);
    assertTrue(eventBody.contains("\"container\":\"test-container\""));
  }

  @Test
  public void testOverrideAudit() throws Exception {
    instance.setOverrideBody("audit");
    String EXPECTED = "UCLA.AuditCode:\\\"AuditCode\\\" UCLA.AuditSource:\\\"AuditSource\\\" UCLA.AuditReason:\\\"AuditReason\\\" UCLA.AuditText:\\\"AuditText\\\"";
    String eventBody = instance.buildEvent(timestamp, messageHistoryList, headers, BODY);
    assertTrue(eventBody.contains(EXPECTED));
  }
}