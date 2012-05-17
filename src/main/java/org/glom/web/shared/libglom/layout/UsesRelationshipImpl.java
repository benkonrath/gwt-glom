package org.glom.web.shared.libglom.layout;

import org.glom.web.client.StringUtils;
import org.glom.web.shared.libglom.Relationship;

public class UsesRelationshipImpl implements UsesRelationship {
	private static final long serialVersionUID = -3778108396526473307L;
	private Relationship relationship;
	private Relationship relatedRelationship;

	@Override
	public void setRelationship(final Relationship relationship) {
		this.relationship = relationship;
	}

	/**
	 * @param get_related_relationship
	 */
	@Override
	public void setRelatedRelationship(final Relationship relationship) {
		this.relatedRelationship = relationship;
	}

	@Override
	public Relationship getRelationship() {
		return relationship;
	}

	@Override
	public Relationship getRelatedRelationship() {
		return relatedRelationship;
	}

	@Override
	public boolean getHasRelationshipName() {
		if (relationship == null) {
			return false;
		}

		if (StringUtils.isEmpty(relationship.getName())) {
			return false;
		}

		return true;
	}

	@Override
	public boolean getHasRelatedRelationshipName() {
		if (relatedRelationship == null) {
			return false;
		}

		if (StringUtils.isEmpty(relatedRelationship.getName())) {
			return false;
		}

		return true;
	}

	@Override
	public String getSqlJoinAliasName() {
		String result = "";

		if (getHasRelationshipName() && relationship.getHasFields()) // relationships that link to tables together
																		// via a field
		{
			// We use relationship_name.field_name instead of related_tableName.field_name,
			// because, in the JOIN below, will specify the relationship_name as an alias for the related table name
			result += ("relationship_" + relationship.getName());

			if (getHasRelatedRelationshipName() && relatedRelationship.getHasFields()) {
				result += ('_' + relatedRelationship.getName());
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	/*
	 * @Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result +
	 * ((relatedRelationship == null) ? 0 : relatedRelationship.hashCode()); result = prime * result + ((relationship ==
	 * null) ? 0 : relationship.hashCode()); return result; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof UsesRelationshipImpl)) {
			return false;
		}

		final UsesRelationshipImpl other = (UsesRelationshipImpl) obj;
		if (relationship == null) {
			if (other.relationship != null) {
				return false;
			}
		} else if (!relationshipEquals(relationship, other.relationship)) {
			return false;
		}

		if (relatedRelationship == null) {
			if (other.relatedRelationship != null) {
				return false;
			}
		} else if (!relationshipEquals(relatedRelationship, other.relatedRelationship)) {
			return false;
		}

		return true;
	}

	/**
	 * We use this utility function because Relationship.equals() fails in the the generated SWIG C++ code with a
	 * NullPointerException.
	 */
	private static boolean relationshipEquals(final Relationship a, final Relationship b) {
		if (a == null) {
			if (b == null) {
				return true;
			} else {
				return false;
			}
		}

		if (b == null) {
			return false;
		}

		return a.equals(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String getTableUsed(final String parentTableName) {
		String result = "";

		if (relatedRelationship != null) {
			result = relatedRelationship.getToTable();
		}

		if (StringUtils.isEmpty(result) && (relationship != null)) {
			result = relationship.getToTable();
		}

		if (StringUtils.isEmpty(result)) {
			result = parentTableName;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
	 */
	@Override
	public String getSqlTableOrJoinAliasName(final String parent_table) {
		if (getHasRelationshipName() || getHasRelatedRelationshipName()) {
			final String result = getSqlJoinAliasName();
			if (StringUtils.isEmpty(result)) {
				// Non-linked-fields relationship:
				return getTableUsed(parent_table);
			} else {
				return result;
			}
		} else {
			return parent_table;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getRelationshipNameUsed()
	 */
	@Override
	public String getRelationshipNameUsed() {
		if (relatedRelationship != null) {
			return relatedRelationship.getName();
		} else if (relationship != null) {
			return relationship.getName();
		} else {
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getTitleUsed(java.lang.String, java.lang.String)
	 */
	@Override
	public String getTitleUsed(final String parentTableTitle, final String locale) {
		if (relatedRelationship != null) {
			return relatedRelationship.getTitleOrName(locale);
		} else if (relationship != null) {
			return relationship.getTitleOrName(locale);
		} else {
			return parentTableTitle;
		}
	}
}
