package com.pronoia.camel.splunk.httpec.internal;

import org.apache.camel.LoggingLevel;

public class LoggingLevelHelper {
  public static LoggingLevel parse(String loggingLevelString) {
    switch (loggingLevelString) {
      case "off":
        return LoggingLevel.OFF;
      case "OFF":
        return LoggingLevel.OFF;
      case "error":
        return LoggingLevel.ERROR;
      case "ERROR":
        return LoggingLevel.ERROR;
      case "warn":
        return LoggingLevel.WARN;
      case "WARN":
        return LoggingLevel.WARN;
      case "warning":
        return LoggingLevel.WARN;
      case "WARNING":
        return LoggingLevel.WARN;
      case "info":
        return LoggingLevel.INFO;
      case "INFO":
        return LoggingLevel.INFO;
      case "debug":
        return LoggingLevel.DEBUG;
      case "DEBUG":
        return LoggingLevel.DEBUG;
      case "trace":
        return LoggingLevel.TRACE;
      case "TRACE":
        return LoggingLevel.TRACE;
      default:
        return null;
    }
  }

  public static String stringValue(LoggingLevel loggingLevel) {
    switch (loggingLevel) {
      case OFF:
        return "OFF";
      case ERROR:
        return "ERROR";
      case WARN:
        return "WARN";
      case INFO:
        return "INFO";
      case DEBUG:
        return "DEBUG";
      case TRACE:
        return "TRACE";
    }

    return "UNKNOWN";
  }
}
