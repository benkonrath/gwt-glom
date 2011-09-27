/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.shared.layout;

import java.io.Serializable;

import org.glom.web.shared.GlomNumericFormat;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
@SuppressWarnings("serial")
public class Formatting implements Serializable {

	// @formatter:off
	public enum HorizontalAlignment {
		HORIZONTAL_ALIGNMENT_AUTO, // For instance, RIGHT for numeric fields.
		HORIZONTAL_ALIGNMENT_LEFT,
		HORIZONTAL_ALIGNMENT_RIGHT
	}
	// @formatter:on

	private HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
	private int multilineHeightLines = 1;
	private String textFormatColourForeground;
	private String textFormatColourBackground;
	private GlomNumericFormat glomNumericFormat;

	public void setHorizontalAlignment(HorizontalAlignment alignment) {
		horizontalAlignment = alignment;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * Get the number of lines of text that should be displayed.
	 * 
	 * @returns the number of lines of text
	 */
	public int getTextFormatMultilineHeightLines() {
		return multilineHeightLines;
	}

	/**
	 * Set the number of lines of text that should be displayed.
	 * 
	 * @param value
	 *            number of lines of text that should be displayed
	 * @returns the number of lines of text
	 */
	public void setTextFormatMultilineHeightLines(int value) {
		this.multilineHeightLines = value;
	}

	/**
	 * Get the foreground colour to use for text when displaying a field value.
	 * 
	 * This should be overridden by {@link GlomNumericFormat#setUseAltForegroundColourForNegatives(boolean)} if that is
	 * active.
	 * 
	 * @returns the text foreground colour in HTML colour format
	 */
	public String getTextFormatColourForeground() {
		return textFormatColourForeground;
	}

	/**
	 * Set the foreground colour to use for text when displaying a field value.
	 * 
	 * @param colour
	 *            the text foreground colour in HTML colour format
	 */
	public void setTextFormatColourForeground(String colour) {
		this.textFormatColourForeground = colour;
	}

	/**
	 * Get the background colour to use for text when displaying a field value.
	 * 
	 * @returns the text background colour in HTML colour format
	 */
	public String getTextFormatColourBackground() {
		return textFormatColourBackground;
	}

	/**
	 * Set the background colour to use for text when displaying a field value.
	 * 
	 * @param colour
	 *            a text background colour in HTML colour format
	 */
	public void setTextFormatColourBackground(String colour) {
		this.textFormatColourBackground = colour;
	}

	public GlomNumericFormat getGlomNumericFormat() {
		return glomNumericFormat;
	}

	public void setGlomNumericFormat(GlomNumericFormat glomNumericFormat) {
		this.glomNumericFormat = glomNumericFormat;
	}

}