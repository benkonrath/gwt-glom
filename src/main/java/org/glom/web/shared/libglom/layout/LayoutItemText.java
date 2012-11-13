package org.glom.web.shared.libglom.layout;

public class LayoutItemText extends LayoutItemWithFormatting {
	private static final long serialVersionUID = -4628381213105506497L;
	/*
	 * Don't make this final, because that breaks GWT serialization. See
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1054
	 */
	private/* final */StaticText text = new StaticText();
	
	/**
	 * @param text
	 */
	public void setText(final StaticText text) {
		this.text = text;
	}
	
	public StaticText getText() {
		return text;
	}

}
