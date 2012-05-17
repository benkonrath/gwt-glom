package org.glom.web.shared.libglom.layout;

public class LayoutItemWithFormatting extends LayoutItem {

	private static final long serialVersionUID = -3224795819809978669L;
	private Formatting formatting = new Formatting(); // Not null, so we have some default formatting.

	public Formatting getFormatting() {
		return formatting;
	}

	public void setFormatting(final Formatting formatting) {
		this.formatting = formatting;
	}
}
