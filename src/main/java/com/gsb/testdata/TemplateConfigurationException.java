package com.gsb.testdata;

/**
 * Thrown while a template is being defined (builder stage), e.g. incoherent
 * constraints, unknown field names or fixed values violating a constraint.
 */
public class TemplateConfigurationException extends TestDataException {

  public TemplateConfigurationException(String message) {
    super(message);
  }
}
