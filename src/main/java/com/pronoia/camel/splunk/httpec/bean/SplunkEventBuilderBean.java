package com.pronoia.camel.splunk.httpec.bean;

import com.pronoia.splunk.eventcollector.eventbuilder.StringEventBuilder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.apache.camel.MessageHistory;

public class SplunkEventBuilderBean {
  static final double MILLISECONDS_PER_SECOND = 1000.0;

  static final int CALLING_ROUTE_LOCATION = 0;
  static final int AUDIT_ROUTE_OFFSET = 1;

  StringEventBuilder stringEventBuilder;

  String overrideBody;

  public SplunkEventBuilderBean() {
    this.stringEventBuilder = new StringEventBuilder();
    this.stringEventBuilder.includeSystemProperty("karaf.name", "container");
  }

  @Handler
  public String buildEvent(@ExchangeProperty("CamelCreatedTimestamp") Date camelCreatedTimestamp,
                           @ExchangeProperty(Exchange.MESSAGE_HISTORY) List<MessageHistory> messageHistoryList,
                           @Headers Map<String, Object> messageHeaders,
                           @Body String body) throws Exception {

    stringEventBuilder.setTimestamp(camelCreatedTimestamp.getTime() / MILLISECONDS_PER_SECOND);

    for (Map.Entry<String, Object> entry : messageHeaders.entrySet()) {
      if (entry.getValue() instanceof byte[]) {
        continue;
      }
      if (entry.getValue() == null) {
        continue;
      }
      if (getOverrideBody() != null && getOverrideBody().equals(entry.getKey().toString())) {
        continue;
      }
      stringEventBuilder.setField(entry.getKey(), entry.getValue().toString());
    }

    stringEventBuilder.setField("CallingRouteId", messageHistoryList.get(CALLING_ROUTE_LOCATION).getRouteId());
    stringEventBuilder.setField("TriggeredAuditRouteId", messageHistoryList.get(messageHistoryList.size() - AUDIT_ROUTE_OFFSET).getRouteId());

    if (overrideBody == null) {
      stringEventBuilder.setEventBody(body);
    } else if (overrideBody.equals("audit")) {
      String auditEvent = "UCLA.AuditCode:\"" + messageHeaders.get("UCLA.AuditCode").toString() + "\""
          + " UCLA.AuditSource:\"" + messageHeaders.get("UCLA.AuditSource").toString() + "\""
          + " UCLA.AuditReason:\"" + messageHeaders.get("UCLA.AuditReason").toString() + "\""
          + " UCLA.AuditText:\"" + messageHeaders.get("UCLA.AuditText").toString() + "\"";
      stringEventBuilder.setEventBody(auditEvent);
    } else {
      stringEventBuilder.setEventBody(messageHeaders.get(overrideBody).toString());
    }

    return stringEventBuilder.build();
  }

  public boolean hasHost() {
    return stringEventBuilder.hasDefaultHost();
  }

  public String getHost() {
    return stringEventBuilder.getDefaultHost();
  }

  public void setHost(String host) {
    stringEventBuilder.setDefaultHost(host);
  }

  public boolean hasIndex() {
    return stringEventBuilder.hasDefaultIndex();
  }

  public String getIndex() {
    return stringEventBuilder.getDefaultIndex();
  }

  public void setIndex(String index) {
    stringEventBuilder.setDefaultIndex(index);
  }

  public boolean hasSource() {
    return stringEventBuilder.hasDefaultSource();
  }

  public String getSource() {
    return stringEventBuilder.getDefaultSource();
  }

  public void setSource(String source) {
    stringEventBuilder.setDefaultSource(source);
  }

  public boolean hasSourcetype() {
    return stringEventBuilder.hasDefaultSourcetype();
  }

  public String getSourcetype() {
    return stringEventBuilder.getDefaultSourcetype();
  }

  public void setSourcetype(String sourcetype) {
    stringEventBuilder.setDefaultSourcetype(sourcetype);
  }

  public String getOverrideBody() {
    return overrideBody;
  }

  public void setOverrideBody(String overrideBody) {
    this.overrideBody = overrideBody;
  }
}
