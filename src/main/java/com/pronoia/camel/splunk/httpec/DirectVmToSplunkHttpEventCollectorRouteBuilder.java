package com.pronoia.camel.splunk.httpec;

import com.pronoia.camel.splunk.httpec.bean.SplunkEventBuilderBean;
import com.pronoia.camel.splunk.httpec.http.InsecureX509TrustManager;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by bryan on 6/22/17.
 */
public class DirectVmToSplunkHttpEventCollectorRouteBuilder extends RouteBuilder {

  SplunkEventBuilderBean splunkEventBuilderBean;
  static final long CONNECTION_TIME_TO_LIVE_MILLIS = 500;
  static final int RETRY_COUNT = 3;
  String endpoint = "";
  String routeId = "";
  String authToken = "";
  String host = "";
  Integer port;

  boolean disableVerifier = true;
  boolean useHTTPS = true;
  boolean loadConfigureHttps4 = false;

  public void configureHttps4() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
    X509ExtendedTrustManager extendedTrustManager = new InsecureX509TrustManager();
    trustManagersParameters.setTrustManager(extendedTrustManager);

    SSLContextParameters sslContextParameters = new SSLContextParameters();
    sslContextParameters.setTrustManagers(trustManagersParameters);

    HttpComponent insecurehttps = getContext().getComponent("https4", HttpComponent.class);
    insecurehttps.setSslContextParameters(sslContextParameters);
    insecurehttps.setConnectionTimeToLive(CONNECTION_TIME_TO_LIVE_MILLIS);
    HttpClientConfigurer httpClientConfigurer = new HttpClientConfigurer() {
      @Override
      public void configureHttpClient(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT,true));
      }
    };
    insecurehttps.setHttpClientConfigurer(httpClientConfigurer);
  }

  @Override
  public void configure() throws Exception {

    if( isLoadConfigureHttps4()) configureHttps4();

    //@formatter:off
    fromF( "direct-vm://%s", endpoint ).routeId( routeId )
            .bean(splunkEventBuilderBean).id("Build Splunk Event")
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST)).id("Set header to POST")
            .setHeader("Authorization",constant("Splunk "+authToken)).id("Set header Auth Token")
            .doTry()
              .toF("%s://%s:%d/services/collector%s",getHttpsURI(),host,port, getVerifierURI()).id("Send to Splunk")
            .doCatch(IOException.class)
              .to("log:com.pronoia.camel.splunk.httpec?level=ERROR&showAll=true")
            .endDoTry();
    ;
    //@formatter:on
  }

  public SplunkEventBuilderBean getSplunkEventBuilderBean() {
    return splunkEventBuilderBean;
  }

  public void setSplunkEventBuilderBean(SplunkEventBuilderBean splunkEventBuilderBean) {
    this.splunkEventBuilderBean = splunkEventBuilderBean;
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
    return useHTTPS?"https4":"http4";
  }

  public boolean isLoadConfigureHttps4() {
    return loadConfigureHttps4;
  }

  public void setLoadConfigureHttps4(boolean loadConfigureHttps4) {
    this.loadConfigureHttps4 = loadConfigureHttps4;
  }
}
