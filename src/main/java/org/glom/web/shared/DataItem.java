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
import java.util.Date;

/**
 * This Data Transfer Object (DTO) is used to send a data item between the client and the server.
 * 
 */
@SuppressWarnings("serial")
public class DataItem implements Serializable {

	private String text;
	private boolean bool;
	private double number;
	private Date date;
	private byte[] image;

	// TODO: Time

	public DataItem() {
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public boolean getBoolean() {
		return bool;
	}

	public void setBoolean(final boolean bool) {
		this.bool = bool;
	}

	public double getNumber() {
		return number;
	}

	public void setNumber(final double number) {
		this.number = number;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(final byte[] image) {
		this.image = image;
	}

}
