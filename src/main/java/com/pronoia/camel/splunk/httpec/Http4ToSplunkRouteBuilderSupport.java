package com.pronoia.camel.splunk.httpec;

import com.pronoia.camel.splunk.httpec.internal.LoggingLevelHelper;
import com.pronoia.splunk.eventcollector.EventBuilder;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.http.conn.ssl.X509HostnameVerifier;


public abstract class Http4ToSplunkRouteBuilderSupport extends RouteBuilder {
  static final long CONNECTION_TIME_TO_LIVE_MILLIS = 500;

  String routeId;
  String sourceEndpointName;
  String targetComponent = "splunk-httpec";

  Processor splunkEventProcessor;

  boolean validateCertificates = true;

  LoggingLevel deliveryFailureLoggingLevel = LoggingLevel.WARN;
  LoggingLevel failedBodyLoggingLevel = LoggingLevel.ERROR;

  String deliveryFailureMessage = "Failed to send event to Splunk because of ${exception.class.name}";
  String failedBodyMessage = "Failed event: ${body}";

  @Override
  public void configure() throws Exception {
    if (!validateCertificates) {
      disableCertificateValidation();
    }
  }

  public boolean hasRouteId() {
    return routeId != null && !routeId.isEmpty();
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public boolean hasSourceEndpointName() {
    return sourceEndpointName != null && !sourceEndpointName.isEmpty();
  }

  public String getSourceEndpointName() {
    return sourceEndpointName;
  }

  public void setSourceEndpointName(String souceEndpointName) {
    this.sourceEndpointName = souceEndpointName;
  }

  public boolean hasTargetComponent() {
    return targetComponent != null && !targetComponent.isEmpty();
  }

  public String getTargetComponent() {
    return targetComponent;
  }

  public void setTargetComponent(String targetComponent) {
    this.targetComponent = targetComponent;
  }

  public boolean hasSplunkEventProcessor() {
    return splunkEventProcessor != null;
  }

  public Processor getSplunkEventProcessor() {
    return splunkEventProcessor;
  }

  public void setSplunkEventProcessor(Processor splunkEventProcessor) {
    this.splunkEventProcessor = splunkEventProcessor;
  }

  public boolean isValidateCertificates() {
    return validateCertificates;
  }

  public void setValidateCertificates(boolean validateCertificates) {
    this.validateCertificates = validateCertificates;
  }

  protected void disableCertificateValidation() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
    trustManagersParameters.setTrustManager(new NoopX509TrustManager());

    SSLContextParameters sslContextParameters = new SSLContextParameters();
    sslContextParameters.setTrustManagers(trustManagersParameters);

    HttpComponent component = getContext().getComponent(getTargetComponent(), HttpComponent.class);
    component.setSslContextParameters(sslContextParameters);
    component.setConnectionTimeToLive(CONNECTION_TIME_TO_LIVE_MILLIS);
    component.setX509HostnameVerifier(new NoopX509HostnameVerifier());
  }

  public LoggingLevel getDeliveryFailureLoggingLevel() {
    return deliveryFailureLoggingLevel;
  }

  public void setDeliveryFailureLoggingLevel(LoggingLevel deliveryFailureLoggingLevel) {
    this.deliveryFailureLoggingLevel = deliveryFailureLoggingLevel;
  }

  public void setDeliveryFailureLoggingLevel(String deliveryFailureLoggingLevelString) {
    LoggingLevel newLoggingLevel = LoggingLevelHelper.parse(deliveryFailureLoggingLevelString);
    if (newLoggingLevel != null) {
      this.deliveryFailureLoggingLevel = newLoggingLevel;
    } else {
      this.deliveryFailureLoggingLevel = LoggingLevel.WARN;
      log.warn("Unknown LoggingLevel requested for 'deliveryFailureLoggingLevel' {} - using default {}", deliveryFailureLoggingLevelString, LoggingLevelHelper.stringValue(deliveryFailureLoggingLevel));
    }
  }

  public LoggingLevel getFailedBodyLoggingLevel() {
    return failedBodyLoggingLevel;
  }

  public void setFailedBodyLoggingLevel(LoggingLevel failedBodyLoggingLevel) {
    this.failedBodyLoggingLevel = failedBodyLoggingLevel;
  }

  public void setFailedBodyLoggingLevel(String failedBodyLoggingLevelString) {
    LoggingLevel newLoggingLevel = LoggingLevelHelper.parse(failedBodyLoggingLevelString);
    if (newLoggingLevel != null) {
      this.failedBodyLoggingLevel = newLoggingLevel;
    } else {
      this.failedBodyLoggingLevel = LoggingLevel.ERROR;
      log.warn("Unknown LoggingLevel requested for 'failedBodyLoggingLevel' {} - using default {}", failedBodyLoggingLevelString, LoggingLevelHelper.stringValue(failedBodyLoggingLevel));
    }
  }

  public String getDeliveryFailureMessage() {
    return deliveryFailureMessage;
  }

  public void setDeliveryFailureMessage(String deliveryFailureMessage) {
    this.deliveryFailureMessage = deliveryFailureMessage;
  }

  public String getFailedBodyMessage() {
    return failedBodyMessage;
  }

  public void setFailedBodyMessage(String failedBodyMessage) {
    this.failedBodyMessage = failedBodyMessage;
  }

  class NoopX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  class NoopX509HostnameVerifier implements X509HostnameVerifier {
    @Override
    public void verify(String s, SSLSocket sslSocket) throws IOException {}

    @Override
    public void verify(String s, X509Certificate x509Certificate) throws SSLException {}

    @Override
    public void verify(String s, String[] strings, String[] strings1) throws SSLException {}

    @Override
    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  }
}
