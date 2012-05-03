package org.glom.web.shared.libglom.layout;



@SuppressWarnings("serial")
public class LayoutItemWithFormatting extends LayoutItem {
	private Formatting formatting;

	public Formatting getFormatting() {
		return formatting;
	}

	public void setFormatting(Formatting formatting) {
		this.formatting = formatting;
	}
	
	public Object clone() {
		LayoutItemWithFormatting result = (LayoutItemWithFormatting)super.clone();

		result.formatting = (Formatting)this.formatting.clone();

		return result;
	}
}
