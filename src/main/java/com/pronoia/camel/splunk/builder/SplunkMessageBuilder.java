package com.pronoia.camel.splunk.builder;

import com.pronoia.splunk.eventcollector.builder.EventBuilderSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.MessageHistory;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by bryan on 6/22/17.
 */
public class SplunkMessageBuilder {
    final static int CALLING_ROUTE_LOCATION = 0;
    final static int AUDIT_ROUTE_OFFSET = 1;

    String index;
    String source;
    String sourceType;

    @Handler
    public void builder(Exchange exchange) throws Exception {

        EventBuilderSupport<String> instance = new EventBuilderSupport<String>(){};

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d hh:mm:ss z yyyy");
        List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
        instance.setTimestamp(dateFormat.parse((exchange.getProperty("CamelCreatedTimestamp").toString())));
        instance.setFields(exchange.getProperties());
        instance.setFields(exchange.getIn().getHeaders());
        instance.setField("CallingRouteId", ((MessageHistory) list.get(CALLING_ROUTE_LOCATION)).getRouteId());
        instance.setField("TriggeredAuditRouteId", ((MessageHistory) list.get(list.size()-AUDIT_ROUTE_OFFSET)).getRouteId());
        instance.setEvent(exchange.getIn().getBody().toString());

        if(getIndex() != null && getIndex().length()>0) instance.setIndex(getIndex());
        if(getSource() != null && getSource().length()>0) instance.setSource(getSource());
        if(getSourceType() != null && getSourceType().length()>0) instance.setSourcetype(getSourceType());

        exchange.getIn().setBody(instance.build());
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
