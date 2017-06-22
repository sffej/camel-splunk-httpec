package com.pronoia.camel.splunk.routes;

import com.pronoia.camel.splunk.builder.SplunkMessageBuilder;
import com.pronoia.splunk.eventcollector.client.SimpleEventCollectorClient;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by bryan on 6/22/17.
 */
public class SplunkHECRouteBuilder  extends RouteBuilder {

    SplunkMessageBuilder splunkMessageBuilder;
    SimpleEventCollectorClient simpleEventCollectorClient;

    @Override
    public void configure() throws Exception {
        //@formatter:off

        from( "direct-vm://{{camel.vm.name.in}}" ).routeId( "{{camel.route.id}}" )
                .to("bean:splunkMessageBuilder?method=builder")
                .to("bean:simpleEventCollector?method=sendEvent");
        //@formatter:on
    }

    public SplunkMessageBuilder getSplunkMessageBuilder() {
        return splunkMessageBuilder;
    }

    public void setSplunkMessageBuilder(SplunkMessageBuilder splunkMessageBuilder) {
        this.splunkMessageBuilder = splunkMessageBuilder;
    }

    public SimpleEventCollectorClient getSimpleEventCollectorClient() {
        return simpleEventCollectorClient;
    }

    public void setSimpleEventCollectorClient(SimpleEventCollectorClient simpleEventCollectorClient) {
        this.simpleEventCollectorClient = simpleEventCollectorClient;
    }
}
