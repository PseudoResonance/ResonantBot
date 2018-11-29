package com.github.pseudoresonance.resonantbot.data;

public class Column {

	private final String name;
	private final String type;
	private final String defaultValue;

	public Column(String name, String type, String defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public Column(String name, String type) {
		this.name = name;
		this.type = type;
		this.defaultValue = "NULL";
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Column) {
			Column col = (Column) obj;
			if (col.getName().equalsIgnoreCase(this.name) && col.getType().equalsIgnoreCase(this.type) && col.getDefaultValue().equals(this.defaultValue))
				return true;
		}
		return false;
	}

}
