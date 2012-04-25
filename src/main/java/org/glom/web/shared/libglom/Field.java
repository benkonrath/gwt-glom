package org.glom.web.shared.libglom;

public class Field extends Translatable {
	
	public enum glom_field_type {
		TYPE_INVALID,
		TYPE_NUMERIC,
		TYPE_TEXT,
		TYPE_DATE,
		TYPE_TIME,
		TYPE_BOOLEAN,
		TYPE_IMAGE
	};
	
	private glom_field_type glom_field_type; //TODO: = glom_field_type.TYPE_INVALID;
	private boolean primaryKey = false;

	/**
	 * @return
	 */
	public boolean get_primary_key() {
		return primaryKey;
	};
	
	public void set_primary_key(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return
	 */
	public glom_field_type get_glom_type() {
		return glom_field_type;
	}
	
	void set_glom_field_type(glom_field_type fieldType) {
		this.glom_field_type = fieldType;
	}
	
	
}
