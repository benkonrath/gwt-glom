package org.glom.web.shared.libglom;

import org.glom.web.client.StringUtils;

@SuppressWarnings("serial")
public class Relationship extends Translatable {

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
		// TODO Auto-generated method stub
		return false;
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

	@Override
	public Relationship clone() {
		Relationship result = (Relationship) super.clone();

		result.fromTable = new String(this.fromTable);
		result.fromField = new String(this.fromField);
		result.toTable = new String(this.toTable);
		result.toField = new String(this.toField);

		return result;
	}

	/**
	 * @param
	 */
	public void setFromTable(String name) {
		fromTable = name;
	}

	/**
	 * @param
	 */
	public void setFromField(String name) {
		fromField = name;
	}

	/**
	 * @param
	 */
	public void setToTable(String name) {
		toTable = name;
	}

	/**
	 * @param
	 */
	public void setToField(String name) {
		toField = name;
	}

}
