package org.glom.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GlomField implements Serializable {
	private String text;
	private String fgColour;
	private String bgColour;

	public GlomField() {
	}

	public GlomField(String text, String fgColour, String bgColour) {
		this.text = text;
		this.fgColour = fgColour;
		this.bgColour = bgColour;
	}

	// @formatter:off
	public String getText() { return text;}
	public void setText(String text) { this.text = text; }
	public String getFGColour() { return fgColour; }
	public void setFGColour(String fgColour) { this.fgColour = fgColour; }
	public String getBGColour() { return bgColour; }
	public void setBGColour(String bgColour) { this.bgColour = bgColour; }
	// @formatter:on
}