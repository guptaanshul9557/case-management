package org.egov.lm.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * status of the Case
 */
public enum Status {

	ACTIVE ("ACTIVE"),

	INACTIVE ("INACTIVE"),
	
	REGISTERED("REGISTERED"),
	
	ADVOCATE_ALLOCATED("ADVOCATE_ALLOCATED"),

	INWORKFLOW ("INWORKFLOW"),
	
	CANCELLED ("CANCELLED"),
	
	REJECTED ("REJECTED"),
	
	INITIATED ("INITIATED"),
	
	SETTLED ("SETTLED"),
	
	CASESCHEDULED ("CASESCHEDULED");

	private String value;

  Status(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static Status fromValue(String text) {
    for (Status b : Status.values()) {
      if (String.valueOf(b.value).equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}
