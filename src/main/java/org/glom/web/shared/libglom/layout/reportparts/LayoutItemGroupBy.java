package org.glom.web.shared.libglom.layout.reportparts;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItemField;

@SuppressWarnings("serial")
public class LayoutItemGroupBy extends LayoutGroup {

	private LayoutItemField fieldGroupBy = null;
	private LayoutGroup secondaryFields = null;

	/**
	 * @return
	 */
	public boolean getHasFieldGroupBy() {
		if (fieldGroupBy == null) {
			return false;
		}

		return !StringUtils.isEmpty(fieldGroupBy.getName());
	}

	/**
	 * @return
	 */
	public LayoutItemField getFieldGroupBy() {
		return fieldGroupBy;
	}

	/**
	 * @param fieldGroupBy
	 */
	public void setFieldGroupBy(final LayoutItemField fieldGroupBy) {
		this.fieldGroupBy = fieldGroupBy;
	}

	/**
	 * @return
	 */
	public LayoutGroup getSecondaryFields() {
		return secondaryFields;
	}

	/**
	 * @param secondaryFields
	 */
	public void setSecondaryFields(final LayoutGroup secondaryFields) {
		this.secondaryFields = secondaryFields;
	}

	@Override
	public Object clone() {
		LayoutItemGroupBy result = (LayoutItemGroupBy) super.clone();

		result.fieldGroupBy = (LayoutItemField) this.fieldGroupBy.clone();
		result.secondaryFields = (LayoutGroup) this.secondaryFields.clone();

		return result;
	}

}
