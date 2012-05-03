package org.glom.web.shared.libglom;

import org.glom.web.shared.libglom.layout.Formatting;

@SuppressWarnings("serial")
public class Field extends Translatable {
	
	public enum GlomFieldType {
		TYPE_INVALID,
		TYPE_NUMERIC,
		TYPE_TEXT,
		TYPE_DATE,
		TYPE_TIME,
		TYPE_BOOLEAN,
		TYPE_IMAGE
	};
	
	private GlomFieldType glom_field_type; //TODO: = glom_field_type.TYPE_INVALID;
	private boolean primaryKey = false;
	private boolean uniqueKey = false;
	Formatting formatting = new Formatting(); //TODO: Start will null?

	/**
	 * @return the formatting
	 */
	public Formatting getFormatting() {
		return formatting;
	}

	/**
	 * @param formatting the formatting to set
	 */
	public void setFormatting(Formatting formatting) {
		this.formatting = formatting;
	}

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
	public GlomFieldType get_glom_type() {
		return glom_field_type;
	}
	
	void set_glom_field_type(GlomFieldType fieldType) {
		this.glom_field_type = fieldType;
	}
	
	public Object clone() {
		final Field result = (Field)super.clone();
		result.glom_field_type = this.glom_field_type;
		result.primaryKey = this.primaryKey;
		result.uniqueKey = this.uniqueKey;
		result.formatting = (Formatting)this.clone();
		return result;
	}

	/**
	 * @return
	 */
	public boolean getUniqueKey() {
		return uniqueKey;
	}
	
}
