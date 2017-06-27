package com.pronoia.camel.splunk.httpec;

import com.pronoia.camel.splunk.httpec.bean.SplunkEventBuilder;

import com.pronoia.camel.splunk.httpec.http.InsecureX509TrustManager;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;


import javax.net.ssl.X509ExtendedTrustManager;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by bryan on 6/22/17.
 */
public class DirectVmToSplunkHttpEventCollectorRouteBuilder extends RouteBuilder {

  SplunkEventBuilder splunkEventBuilder;
  String endpoint = "";
  String routeId = "";
  String authToken = "";
  String host = "";
  Integer port;

  boolean disableVerifier = true;
  boolean useHTTPS = true;

  private void configureHttps4() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
    X509ExtendedTrustManager extendedTrustManager = new InsecureX509TrustManager();
    trustManagersParameters.setTrustManager(extendedTrustManager);

    SSLContextParameters sslContextParameters = new SSLContextParameters();
    sslContextParameters.setTrustManagers(trustManagersParameters);

    HttpComponent httpComponent = getContext().getComponent("https4", HttpComponent.class);
    httpComponent.setSslContextParameters(sslContextParameters);
  }

  @Override
  public void configure() throws Exception {

    configureHttps4();

    //@formatter:off

    fromF( "direct-vm://%s", endpoint ).routeId( routeId )
            .bean(splunkEventBuilder).id("Build Splunk Event")
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST)).id("Set header to POST")
            .setHeader("Authorization",constant("Splunk "+authToken)).id("Set header Auth Token")
            .doTry()
              .toF("%s4://%s:%d/services/collector%s",getHttpsURI(),host,port, getVerifierURI()).id("Send to Splunk")
            .doCatch(Exception.class)
              .to("log:com.pronoia.camel.splunk.httpec?level=ERROR&showAll=true&multiline=true").id("Log Exception")
              .rollback().id("Rollback data")
            .endDoTry();

    //@formatter:on
  }

  public SplunkEventBuilder getSplunkEventBuilder() {
    return splunkEventBuilder;
  }

  public void setSplunkEventBuilder(SplunkEventBuilder splunkEventBuilder) {
    this.splunkEventBuilder = splunkEventBuilder;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public boolean isDisableVerifier() {
    return disableVerifier;
  }

  public void setDisableVerifier(boolean disableVerifier) {
    this.disableVerifier = disableVerifier;
  }

  protected String getVerifierURI() {
    return disableVerifier?"?x509HostnameVerifier=allowAllHostnameVerifier":"";
  }

  public boolean isUseHTTPS() {
    return useHTTPS;
  }

  public void setUseHTTPS(boolean useHTTPS) {
    this.useHTTPS = useHTTPS;
  }

  protected String getHttpsURI() {
    return useHTTPS?"https":"http";
  }
}
