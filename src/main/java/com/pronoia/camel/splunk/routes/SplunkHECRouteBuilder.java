package com.pronoia.camel.splunk.routes;

import com.pronoia.camel.splunk.builder.SplunkMessageBuilder;
import com.pronoia.splunk.eventcollector.client.SimpleEventCollectorClient;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by bryan on 6/22/17.
 */
public class SplunkHECRouteBuilder  extends RouteBuilder {

    SplunkMessageBuilder splunkMessageBuilder;

    @Override
    public void configure() throws Exception {
        //@formatter:off

        from( "direct-vm://{{camel.vm.name.in}}" ).routeId( "{{camel.route.id}}" )
                .to("bean:splunkMessageBuilder?method=builder")
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
                .setHeader("Authorization",constant("Splunk {{splunk.authorizationToken}}"))
                .to("http4://{{splunk.host}}:{{splunk.port}}/services/collector");
        //@formatter:on
    }

    public SplunkMessageBuilder getSplunkMessageBuilder() {
        return splunkMessageBuilder;
    }

    public void setSplunkMessageBuilder(SplunkMessageBuilder splunkMessageBuilder) {
        this.splunkMessageBuilder = splunkMessageBuilder;
    }

}
