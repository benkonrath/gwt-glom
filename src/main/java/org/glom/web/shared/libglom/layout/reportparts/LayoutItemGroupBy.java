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
	public boolean get_has_field_group_by() {
		if(fieldGroupBy == null) {
			return false;
		}
		
		return !StringUtils.isEmpty(fieldGroupBy.get_name());
	}

	/**
	 * @return
	 */
	public LayoutItemField get_field_group_by() {
		return fieldGroupBy;
	}

	/**
	 * @param fieldGroupBy
	 */
	public void set_field_group_by(final LayoutItemField fieldGroupBy) {
		this.fieldGroupBy = fieldGroupBy;
	}

	/**
	 * @return
	 */
	public LayoutGroup get_secondary_fields() {
		return secondaryFields;
	}
	
	/**
	 * @param secondaryFields
	 */
	public void set_secondary_fields(final LayoutGroup secondaryFields) {
		this.secondaryFields = secondaryFields;
	}
	
	public Object clone() {
		LayoutItemGroupBy result = (LayoutItemGroupBy)super.clone();

		result.fieldGroupBy = (LayoutItemField)this.fieldGroupBy.clone();
		result.secondaryFields = (LayoutGroup)this.secondaryFields.clone();

		return result;
	}


}
