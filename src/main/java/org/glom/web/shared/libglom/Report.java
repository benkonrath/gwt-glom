package org.glom.web.shared.libglom;

import org.glom.web.shared.libglom.layout.LayoutGroup;

public class Report extends Translatable {

	private static final long serialVersionUID = 4175542775362775834L;
	private LayoutGroup layoutGroup = new LayoutGroup();

	/**
	 * @return
	 */
	public LayoutGroup getLayoutGroup() {
		return layoutGroup;
	}

	/**
	 * @param listLayoutGroups
	 */
	public void setLayoutGroup(final LayoutGroup layoutGroup) {
		this.layoutGroup = layoutGroup;
	}
}
