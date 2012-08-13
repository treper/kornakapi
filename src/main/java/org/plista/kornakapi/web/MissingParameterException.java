package org.plista.kornakapi.web;

public class MissingParameterException extends RuntimeException {

  public MissingParameterException(String message) {
    super(message);
  }
}
