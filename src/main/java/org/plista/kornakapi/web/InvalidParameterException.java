package org.plista.kornakapi.web;

public class InvalidParameterException extends RuntimeException {

  public InvalidParameterException(String message, Throwable cause) {
    super(message, cause);
  }
}
