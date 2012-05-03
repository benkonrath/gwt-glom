package org.glom.web.shared.libglom;

import org.glom.web.shared.libglom.layout.LayoutGroup;

@SuppressWarnings("serial")
public class Report extends Translatable {

	private LayoutGroup layoutGroup = new LayoutGroup();

	/**
	 * @return
	 */
	public LayoutGroup get_layout_group() {
		return layoutGroup;
	}
	
	public Object clone() {
		Report result = (Report)super.clone();

		result.layoutGroup = (LayoutGroup)this.layoutGroup.clone(); 

		return result;
	}

	/**
	 * @param listLayoutGroups
	 */
	public void set_layout_group(final LayoutGroup layoutGroup) {
		this.layoutGroup = layoutGroup;
	}
}
