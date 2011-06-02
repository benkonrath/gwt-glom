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

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
@SuppressWarnings("serial")
public class LayoutItemWithFormatting extends LayoutItem implements Serializable {

	private Formatting formatting;

	/**
	 * Get the field formatting used by this layout item, which may be either custom field formatting or the default
	 * field formatting.
	 */
	Formatting getFormatting() {
		return formatting;
	}

	/**
	 * Get the alignment for the formatting used (see getFormattingUsed()), choosing an appropriate alignment if it is
	 * set to HORIZONTAL_ALIGNMENT_AUTO. Note that this never returns HORIZONTAL_ALIGNMENT_AUTO.
	 */
	Formatting.HorizontalAlignment getFormattingHorizontalAlignment() {
		Formatting.HorizontalAlignment alignment = formatting.getHorizontalAlignment();

		if (alignment == Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO)
			alignment = Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_LEFT;

		return alignment;

	}

}
