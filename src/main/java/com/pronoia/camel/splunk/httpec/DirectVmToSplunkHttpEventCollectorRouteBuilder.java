package com.pronoia.camel.splunk.httpec;

import com.pronoia.camel.splunk.httpec.bean.SplunkEventBuilder;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by bryan on 6/22/17.
 */
public class DirectVmToSplunkHttpEventCollectorRouteBuilder extends RouteBuilder {

  SplunkEventBuilder splunkEventBuilder;

  @Override
  public void configure() throws Exception {
    //@formatter:off

        from( "direct-vm://{{camel.vm.name.in}}" ).routeId( "{{camel.route.id}}" )
                .to("bean:splunkEventBuilder?method=buildEvent")
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
                .setHeader("Authorization",constant("Splunk {{splunk.authorizationToken}}"))
                .to("http4://{{splunk.host}}:{{splunk.port}}/services/collector");
        //@formatter:on
  }

  public SplunkEventBuilder getSplunkEventBuilder() {
    return splunkEventBuilder;
  }

  public void setSplunkEventBuilder(SplunkEventBuilder splunkEventBuilder) {
    this.splunkEventBuilder = splunkEventBuilder;
  }

}
