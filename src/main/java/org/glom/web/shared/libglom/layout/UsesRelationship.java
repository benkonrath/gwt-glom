/*
 * Copyright (C) 2012 Openismus GmbH
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

package org.glom.web.shared.libglom.layout;

import java.io.Serializable;

import org.glom.web.shared.libglom.Relationship;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public interface UsesRelationship extends Serializable {

	public abstract void setRelationship(final Relationship relationship);

	public abstract Relationship getRelationship();

	/**
	 * @return
	 */
	public abstract boolean getHasRelationshipName();

	/**
	 * @param get_related_relationship
	 */
	public abstract void setRelatedRelationship(final Relationship relationship);

	public abstract Relationship getRelatedRelationship();

	public abstract boolean getHasRelatedRelationshipName();

	public abstract String getSqlJoinAliasName();

	public abstract String getSqlTableOrJoinAliasName(String tableName);

	/**
	 * @param string
	 * @return
	 */
	public abstract String getTableUsed(String string);

	public abstract String getRelationshipNameUsed();

	public abstract String getTitleUsed(String parentTableTitle, String locale);
}