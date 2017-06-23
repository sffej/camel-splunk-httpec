package com.pronoia.camel.splunk.httpec.bean;

import com.pronoia.splunk.eventcollector.builder.StringEventBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.apache.camel.MessageHistory;

public class SplunkEventBuilder {
  static final double MILLISECONDS_PER_SECOND = 1000.0;

  static final int CALLING_ROUTE_LOCATION = 0;
  static final int AUDIT_ROUTE_OFFSET = 1;

  String index;
  String source;
  String sourceType;

  @Handler
  public String buildEvent(@ExchangeProperty("CamelCreatedTimestamp") Date camelCreatedTimestamp,
                           @ExchangeProperty(Exchange.MESSAGE_HISTORY) List<MessageHistory> messageHistoryList,
                           @Headers Map<String, Object> messageHeaders,
                           @Body String body) throws Exception {

    StringEventBuilder instance = new StringEventBuilder();

    instance.setTimestamp(camelCreatedTimestamp.getTime() / MILLISECONDS_PER_SECOND);

    instance.setFields(messageHeaders);
    instance.setField("CallingRouteId", messageHistoryList.get(CALLING_ROUTE_LOCATION).getRouteId());
    instance.setField("TriggeredAuditRouteId", messageHistoryList.get(messageHistoryList.size() - AUDIT_ROUTE_OFFSET).getRouteId());

    instance.setEvent(body);

    if (hasIndex()) {
      instance.setIndex(getIndex());
    }
    if (hasSource()) {
      instance.setSource(getSource());
    }
    if (hasSourceType()) {
      instance.setSourcetype(getSourceType());
    }

    return instance.build();
  }

  public boolean hasIndex() {
    return index != null && !index.isEmpty();
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public boolean hasSource() {
    return source != null && !source.isEmpty();
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public boolean hasSourceType() {
    return sourceType != null && !sourceType.isEmpty();
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }
}
