package org.glom.web.shared.libglom;

import org.glom.web.client.StringUtils;

public class Relationship extends Translatable {

	private static final long serialVersionUID = 851415917396362167L;
	private String fromTable = "";
	private String fromField = "";
	private String toTable = "";
	private String toField = "";

	/**
	 * @return
	 */
	public boolean getHasToTable() {
		return !StringUtils.isEmpty(toTable);
	}

	/**
	 * @return
	 */
	public String getToField() {
		return toField;
	}

	/**
	 * @return
	 */
	public boolean getHasFields() {
		return !StringUtils.isEmpty(toField) && !StringUtils.isEmpty(toTable) && !StringUtils.isEmpty(fromField)
				&& !StringUtils.isEmpty(fromTable);
	}

	/**
	 * @return
	 */
	public String getToTable() {
		return toTable;
	}

	/**
	 * @return
	 */
	public String getFromTable() {
		return fromTable;
	}

	/**
	 * @return
	 */
	public String getFromField() {
		return fromField;
	}

	/**
	 * @param
	 */
	public void setFromTable(final String name) {
		fromTable = name;
	}

	/**
	 * @param
	 */
	public void setFromField(final String name) {
		fromField = name;
	}

	/**
	 * @param
	 */
	public void setToTable(final String name) {
		toTable = name;
	}

	/**
	 * @param
	 */
	public void setToField(final String name) {
		toField = name;
	}

	public boolean equals(final Relationship b) {
		if (b == null) {
			return false;
		}

		if (!StringUtils.equals(this.getName(), b.getName())) {
			return false;
		}

		if (!StringUtils.equals(this.fromTable, b.fromTable)) {
			return false;
		}

		if (!StringUtils.equals(this.fromField, b.fromField)) {
			return false;
		}

		if (!StringUtils.equals(this.toTable, b.toTable)) {
			return false;
		}

		if (!StringUtils.equals(this.toField, b.toField)) {
			return false;
		}

		return true;
	}

}
