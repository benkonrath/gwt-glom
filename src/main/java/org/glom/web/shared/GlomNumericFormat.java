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

package org.glom.web.shared;

import java.io.Serializable;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
@SuppressWarnings("serial")
public class GlomNumericFormat implements Serializable {

	/**
	 * String to use as the currency symbol. When the symbol is shown in the UI, a space is appended to the string, and
	 * the result is prepended to the data from the database. Be aware that the string supplied by the Glom document
	 * might have no representation in the current user's locale.
	 */
	private String currencyCode = "";

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
	 * Whether to use an alternative foreground colour for negative values.
	 */
	private boolean useAltForegroundColourForNegatives = false;

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public boolean getUseThousandsSeparator() {
		return useThousandsSeparator;
	}

	public void setUseThousandsSeparator(boolean useThousandsSeparator) {
		this.useThousandsSeparator = useThousandsSeparator;
	}

	public boolean getDecimalPlacesRestricted() {
		return decimalPlacesRestricted;
	}

	public void setDecimalPlacesRestricted(boolean decimalPlacesRestricted) {
		this.decimalPlacesRestricted = decimalPlacesRestricted;
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public boolean getUseAltForegroundColourForNegatives() {
		return useAltForegroundColourForNegatives;
	}

	public void setUseAltForegroundColourForNegatives(boolean useAltForegroundColourForNegatives) {
		this.useAltForegroundColourForNegatives = useAltForegroundColourForNegatives;
	}

}
