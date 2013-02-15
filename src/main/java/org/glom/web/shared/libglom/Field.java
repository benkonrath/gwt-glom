package org.glom.web.shared.libglom;

import org.glom.web.shared.libglom.layout.Formatting;

public class Field extends Translatable {

	private static final long serialVersionUID = 5297785500678189743L;

	public enum GlomFieldType {
		TYPE_INVALID, TYPE_NUMERIC, TYPE_TEXT, TYPE_DATE, TYPE_TIME, TYPE_BOOLEAN, TYPE_IMAGE
	};

	private GlomFieldType glomFieldType; // TODO: = glom_field_type.TYPE_INVALID;
	private boolean primaryKey = false;

	// TODO: Add a setter, and remove final.
	/*
	 * Don't make this final, because that breaks GWT serialization. See
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1054
	 */
	private/* final */boolean uniqueKey = false;

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

	public enum SqlDialect {
		POSTGRESQL,
		MYSQL
	};

	/**
	 * @return
	 */
	public String getSqlType(SqlDialect sqlDialect) {
		// libglom uses libgda's map of Gda types and its API,
		// without hardcoding the actual SQL type names.
		
		if (sqlDialect == SqlDialect.POSTGRESQL) {
			// This is based on what libgda actually uses with PostgreSQL.
			switch (getGlomType()) {
			case TYPE_NUMERIC:
				return "numeric";
			case TYPE_TEXT:
				return "character varying";
			case TYPE_DATE:
				return "date";
			case TYPE_TIME:
				return "time with time zone";
			case TYPE_IMAGE:
				return "bytea";
			default:
				return "unknowntype";
			}
		} else { // MYSQL
			// This is based on what Glom actually uses with MySQL.
			switch (getGlomType()) {
			case TYPE_NUMERIC:
				return "double";
			case TYPE_TEXT:
				return "varchar(255)";
			case TYPE_DATE:
				return "date";
			case TYPE_TIME:
				return "time with time zone";
			case TYPE_IMAGE:
				return "blob";
			default:
				return "unknowntype";
			}
		}
	}
}
