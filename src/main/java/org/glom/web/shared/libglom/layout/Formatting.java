package org.glom.web.shared.libglom.layout;

import org.glom.web.shared.libglom.NumericFormat;

public class Formatting implements Cloneable {
	// @formatter:off
		public enum HorizontalAlignment {
			HORIZONTAL_ALIGNMENT_AUTO, // For instance, RIGHT for numeric fields.
			HORIZONTAL_ALIGNMENT_LEFT,
			HORIZONTAL_ALIGNMENT_RIGHT
		}
		// @formatter:on

		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
		private int multilineHeightLines = 1;
		private String textFormatColourForeground = "";
		private String textFormatColourBackground = "";
		private NumericFormat numericFormat = new NumericFormat();

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
		 * This should be overridden by {@link GlomNumericFormat#setUseAltForegroundColorForNegatives(boolean)} if that is
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

		public NumericFormat getNumericFormat() {
			return numericFormat;
		}

		public void setNumericFormat(NumericFormat numericFormat) {
			this.numericFormat = numericFormat;
		}
		
		public Object clone() {
			Formatting result = null;
			try {
				result = (Formatting)super.clone();
			} catch (CloneNotSupportedException e) {
				System.err.println("Formatting.clone() failed: " + e.getMessage());
				 return null;
			}

			result.numericFormat = (NumericFormat)this.numericFormat.clone();
			result.textFormatColourBackground = new String(this.textFormatColourBackground);
			result.textFormatColourForeground = new String(this.textFormatColourForeground);

			return result;
		}
}
