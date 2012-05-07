package org.glom.web.shared.libglom;

import org.glom.web.shared.libglom.layout.Formatting;

public class Field extends Translatable {

	private static final long serialVersionUID = 5297785500678189743L;

	public enum GlomFieldType {
		TYPE_INVALID, TYPE_NUMERIC, TYPE_TEXT, TYPE_DATE, TYPE_TIME, TYPE_BOOLEAN, TYPE_IMAGE
	};

	private GlomFieldType glomFieldType; // TODO: = glom_field_type.TYPE_INVALID;
	private boolean primaryKey = false;
	private boolean uniqueKey = false;
	Formatting formatting = new Formatting(); // Not null, so we have some default formatting.

	/**
	 * @return the formatting
	 */
	public Formatting getFormatting() {
		return formatting;
	}

	/**
	 * @param formatting
	 *            the formatting to set
	 */
	public void setFormatting(final Formatting formatting) {
		this.formatting = formatting;
	}

	/**
	 * @return
	 */
	public boolean getPrimaryKey() {
		return primaryKey;
	};

	public void setPrimaryKey(final boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return
	 */
	public GlomFieldType getGlomType() {
		return glomFieldType;
	}

	public void setGlomFieldType(final GlomFieldType fieldType) {
		this.glomFieldType = fieldType;
	}

	/**
	 * @return
	 */
	public boolean getUniqueKey() {
		return uniqueKey;
	}

}
