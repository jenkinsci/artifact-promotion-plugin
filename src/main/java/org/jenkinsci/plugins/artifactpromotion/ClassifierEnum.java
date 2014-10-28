package org.jenkinsci.plugins.artifactpromotion;

public enum ClassifierEnum {
	SOURCES("-sources"),
	JAVADOC("-javadoc");

	private String value;
	
	public String getValue() {
		return this.value;
	}
	
	ClassifierEnum(String enumValue) {
		this.value = enumValue;
	}
}
