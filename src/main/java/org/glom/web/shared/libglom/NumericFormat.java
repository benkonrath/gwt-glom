package org.glom.web.shared.libglom;

import java.io.Serializable;

public class NumericFormat implements Serializable {

	private static final long serialVersionUID = -71135742094755989L;

	/**
	 * String to use as the currency symbol. When the symbol is shown in the UI, a space is appended to the string, and
	 * the result is prepended to the data from the database. Be aware that the string supplied by the Glom document
	 * might have no representation in the current user's locale.
	 */
	private String currencySymbol = "";

	/**
	 * Setting this to false would override the locale, if it used a 1000s separator.
	 */
	private boolean useThousandsSeparator = true;

	/**
	 * Whether to restrict numeric precision. If true, a fixed precision is set according to decimalPlaces. If false,
	 * the maximum precision is used. However, the chosen fixed precision might exceed the maximum precision.
	 */
	private boolean decimalPlacesRestricted = false;

	/**
	 * The number of decimal places to show, although it is only used if decimalPlacesRestricted is true.
	 */
	private int decimalPlaces = 2;

	/**
	 * Whether to use an alternative foreground color for negative values.
	 */
	private boolean useAltForegroundColorForNegatives = false;

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(final String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public boolean getUseThousandsSeparator() {
		return useThousandsSeparator;
	}

	public void setUseThousandsSeparator(final boolean useThousandsSeparator) {
		this.useThousandsSeparator = useThousandsSeparator;
	}

	public boolean getDecimalPlacesRestricted() {
		return decimalPlacesRestricted;
	}

	public void setDecimalPlacesRestricted(final boolean decimalPlacesRestricted) {
		this.decimalPlacesRestricted = decimalPlacesRestricted;
	}

	public static int getDefaultPrecision() {
		return 15; // As in libglom's numeric_format.cc
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(final int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public static String getAlternativeColorForNegatives() {
		return "red"; // As in libglom's numeric_format.cc
	}

	public static String getAlternativeColorForNegativesAsHTMLColor() {
		return "red"; // As in libglom's numeric_format.cc
	}

	public boolean getUseAltForegroundColorForNegatives() {
		return useAltForegroundColorForNegatives;
	}

	public void setUseAltForegroundColorForNegatives(final boolean useAltForegroundColorForNegatives) {
		this.useAltForegroundColorForNegatives = useAltForegroundColorForNegatives;
	}
}
