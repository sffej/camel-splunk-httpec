package com.pronoia.camel.splunk.httpec.eventbuilder;

import com.pronoia.splunk.eventcollector.EventBuilder;
import com.pronoia.splunk.eventcollector.eventbuilder.JacksonEventBuilderSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

public class DefaultCamelExchangeEventBuilder extends JacksonEventBuilderSupport<Exchange> implements CamelExchangeEventBuilder {
  boolean includeExchangeProperties = false;
  boolean includeMessageHeaders = true;
  boolean includeBody = true;

  boolean propertiesAsFields = true;
  boolean headersAsFields = true;

  Pattern excludePropertyPattern;
  Pattern includePropertyPattern;
  Pattern excludeHeaderPattern;
  Pattern includeHeaderPattern;

  public DefaultCamelExchangeEventBuilder() {
    if (!hasHost()) {
      setHost();
    }
  }

  @Override
  protected void serializeFields(Map<String, Object> eventObject) {
    if (hasEvent()) {
      Exchange exchange = getEvent();

      Date timestamp = exchange.getProperty(Exchange.CREATED_TIMESTAMP, Date.class);

      if (timestamp != null) {
        setTimestamp(timestamp);
      } else {
        setTimestamp();
      }

      if (includeExchangeProperties && propertiesAsFields) {
        Map<String, Object> exchangeProperties = exchange.getProperties();
        if (exchangeProperties != null && !exchangeProperties.isEmpty()) {
          for (Map.Entry<String, Object> property : exchangeProperties.entrySet()) {
            String propertyName = property.getKey();
            Object propertyValue = property.getValue();
            if (propertyValue != null && qualifyExchangePropertyValue(propertyName, propertyValue)) {
              addField(propertyName, propertyValue.toString());
            }
          }
        }
      }

      if (includeMessageHeaders && headersAsFields) {
        Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

        Map<String, Object> messageHeaders = message.getHeaders();
        if (messageHeaders != null && !messageHeaders.isEmpty()) {
          for (Map.Entry<String, Object> header : messageHeaders.entrySet()) {
            String headerName = header.getKey();
            Object headerValue = header.getValue();
            if (headerValue != null && qualifyMessageHeaderValue(headerName, headerValue)) {
              addField(headerName, headerValue.toString());
            }
          }
        }
      }
    } else {
      setTimestamp();
    }

    super.serializeFields(eventObject);
  }

  @Override
  protected void serializeBody(Map<String, Object> eventObject) {
    if (hasEvent()) {
      Exchange exchange = getEvent();
      Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

      if (propertiesAsFields && headersAsFields) {
        if (includeBody) {
          eventObject.put("event", message.getBody());
        } else {
          eventObject.put("event", "[Camel Message Body was excluded]");
        }
      } else {
        Map<String, Object> camelEvent = new HashMap<>();

        if (includeExchangeProperties && !propertiesAsFields) {
          Map<String, Object> exchangeProperties = exchange.getProperties();
          if (exchangeProperties != null && !exchangeProperties.isEmpty()) {
            Map<String, Object> propertiesForEvent = new HashMap<>();

            for (Map.Entry<String, Object> property : exchangeProperties.entrySet()) {
              String propertyName = property.getKey();
              Object propertyValue = property.getValue();
              if (qualifyExchangePropertyValue(propertyName, propertyValue)) {
                propertiesForEvent.put(propertyName, propertyValue.toString());
              }
            }

            camelEvent.put("exchangeProperties", propertiesForEvent);
          }
        }

        if (includeMessageHeaders && !headersAsFields) {
          Map<String, Object> messageHeaders = message.getHeaders();
          if (messageHeaders != null && !messageHeaders.isEmpty()) {
            Map<String, Object> headersForEvent = new HashMap<>();

            for (Map.Entry<String, Object> header : messageHeaders.entrySet()) {
              String headerName = header.getKey();
              Object headerValue = header.getValue();
              if (qualifyMessageHeaderValue(headerName, headerValue)) {
                headersForEvent.put(headerName, headerValue.toString());
              }
            }

            camelEvent.put("messageHeaders", headersForEvent);
          }
        }

        if (includeBody) {
          camelEvent.put("messageBody", message.getBody());
        }

        eventObject.put("event", camelEvent);
      }
    }
  }

  @Override
  public EventBuilder<Exchange> duplicate() {
    DefaultCamelExchangeEventBuilder answer = new DefaultCamelExchangeEventBuilder();

    answer.copyConfiguration(this);

    return answer;
  }

  public boolean isIncludeExchangeProperties() {
    return includeExchangeProperties;
  }

  public void setIncludeExchangeProperties(boolean includeExchangeProperties) {
    this.includeExchangeProperties = includeExchangeProperties;
  }

  public boolean isIncludeMessageHeaders() {
    return includeMessageHeaders;
  }

  public void setIncludeMessageHeaders(boolean includeMessageHeaders) {
    this.includeMessageHeaders = includeMessageHeaders;
  }

  public boolean isIncludeBody() {
    return includeBody;
  }

  public void setIncludeBody(boolean includeBody) {
    this.includeBody = includeBody;
  }

  public Pattern getExcludePropertyPattern() {
    return excludePropertyPattern;
  }

  public void setExcludePropertyPattern(Pattern excludePropertyPattern) {
    this.excludePropertyPattern = excludePropertyPattern;
  }

  public void setExcludePropertyPattern(String excludePropertyPatternString) {
    this.excludePropertyPattern = Pattern.compile(excludePropertyPatternString);
  }

  public Pattern getIncludePropertyPattern() {
    return includePropertyPattern;
  }

  public void setIncludePropertyPattern(Pattern includePropertyPattern) {
    this.includePropertyPattern = includePropertyPattern;
  }

  public void setIncludePropertyPattern(String includePropertyPatternString) {
    this.includePropertyPattern = Pattern.compile(includePropertyPatternString);
  }

  public Pattern getExcludeHeaderPattern() {
    return excludeHeaderPattern;
  }

  public void setExcludeHeaderPattern(Pattern excludeHeaderPattern) {
    this.excludeHeaderPattern = excludeHeaderPattern;
  }

  public void setExcludeHeaderPattern(String excludeHeaderPatternString) {
    this.excludeHeaderPattern = Pattern.compile(excludeHeaderPatternString);
  }

  public Pattern getIncludeHeaderPattern() {
    return includeHeaderPattern;
  }

  public void setIncludeHeaderPattern(Pattern includeHeaderPattern) {
    this.includeHeaderPattern = includeHeaderPattern;
  }

  public void setIncludeHeaderPattern(String includeHeaderPatternString) {
    this.includeHeaderPattern = Pattern.compile(includeHeaderPatternString);
  }

  boolean qualifyExchangePropertyValue(String propertyName, Object propertyValue) {
    boolean answer = false;

    if (propertyValue != null) {
      String propertyValueString = propertyValue.toString();
      if (!propertyValueString.isEmpty()) {
        if (excludePropertyPattern != null && excludePropertyPattern.matcher(propertyName).matches()) {
          log.debug("Excluding exchange property {}='{}' because value matched exclusion pattern {}", propertyName, propertyValue, excludePropertyPattern.toString());
        } else if (includePropertyPattern != null) {
          if (includePropertyPattern.matcher(propertyName).matches()) {
            answer = true;
          } else {
            log.debug("Not including exchange property {}='{}' because value did not match inclusion pattern {}", propertyName, propertyValue, includePropertyPattern.toString());
          }
        } else {
          answer = true;
        }
      }
    }

    return answer;
  }

  boolean qualifyMessageHeaderValue(String headerName, Object headerValue) {
    boolean answer = false;

    if (headerValue != null) {
      String headerValueString = headerValue.toString();
      if (!headerValueString.isEmpty()) {
        if (excludeHeaderPattern != null && excludeHeaderPattern.matcher(headerName).matches()) {
          log.debug("Excluding message header {}='{}' because value matched exclusion pattern {}", headerName, headerValue, excludeHeaderPattern.toString());
        } else if (includeHeaderPattern != null) {
          if (includeHeaderPattern.matcher(headerName).matches()) {
            answer = true;
          } else {
            log.debug("Not including message header {}='{}' because value did not match inclusion pattern {}", headerName, headerValue, includeHeaderPattern.toString());
          }
        } else {
          answer = true;
        }
      }
    }

    return answer;
  }


}
