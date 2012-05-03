package org.glom.web.shared.libglom.layout;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.libglom.Relationship;

public class UsesRelationshipImpl implements UsesRelationship {
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

		if (StringUtils.isEmpty(relationship.get_name())) {
			return false;
		}

		return true;
	}

	@Override
	public boolean getHasRelatedRelationshipName() {
		if (relatedRelationship == null) {
			return false;
		}

		if (StringUtils.isEmpty(relatedRelationship.get_name())) {
			return false;
		}

		return true;
	}

	@Override
	public String get_sql_join_alias_name() {
		String result = "";

		if (getHasRelationshipName() && relationship.get_has_fields()) // relationships that link to tables together
																		// via a field
		{
			// We use relationship_name.field_name instead of related_tableName.field_name,
			// because, in the JOIN below, will specify the relationship_name as an alias for the related table name
			result += ("relationship_" + relationship.get_name());

			if (getHasRelatedRelationshipName() && relatedRelationship.get_has_fields()) {
				result += ('_' + relatedRelationship.get_name());
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
	 * ((relatedRelationship == null) ? 0 : relatedRelationship.hashCode()); result = prime * result +
	 * ((relationship == null) ? 0 : relationship.hashCode()); return result; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * 
	 * TODO: This causes NullPointerExceptions when used from contains().
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
		} else if (!relationship_equals(relationship, other.relationship)) {
			return false;
		}

		if (relatedRelationship == null) {
			if (other.relatedRelationship != null) {
				return false;
			}
		} else if (!relationship_equals(relatedRelationship, other.relatedRelationship)) {
			return false;
		}

		return true;
	}

	/**
	 * We use this utility function because Relationship.equals() fails in the the generated SWIG C++ code with a
	 * NullPointerException.
	 */
	public static boolean relationship_equals(final Relationship a, final Relationship b) {
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

		final String a_name = a.get_name();
		final String b_name = b.get_name();

		if (!StringUtils.equals(a_name, b_name)) { // TODO: And the rest.
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String get_table_used(String parentTableName) {
		String result = "";
	
		if(relatedRelationship != null) {
			result = relatedRelationship.get_to_table();
		}
			
		if(StringUtils.isEmpty(result) && (relationship != null)) {
			result = relationship.get_to_table();
		}
		
		if(StringUtils.isEmpty(result)) {
			result = parentTableName;
		}
		
		return result;
}

	/* (non-Javadoc)
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
	 */
	@Override
	public String get_sql_table_or_join_alias_name(String parent_table) {
		if(getHasRelationshipName() || getHasRelatedRelationshipName())
		{
			final String result = get_sql_join_alias_name();
			if(StringUtils.isEmpty(result)) {
				//Non-linked-fields relationship:
				return get_table_used(parent_table);
			}
			else
				return result;
		}
		else
			return parent_table;
	}
	
	@Override
	public UsesRelationshipImpl clone() {
		UsesRelationshipImpl result;
		try {
			result = (UsesRelationshipImpl)super.clone();
		} catch (CloneNotSupportedException e) {
			 System.err.println("UsesRelationshipImpl.clone() failed: " + e.getMessage());
			 return null;
		}

		result.relationship = this.relationship.clone();
		result.relatedRelationship = this.relatedRelationship.clone();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getRelationshipNameUsed()
	 */
	@Override
	public String getRelationshipNameUsed() {
		if(relatedRelationship != null) {
			return relatedRelationship.get_name();
		} else if(relationship != null) {
			return relationship.get_name();
		} else {
			return "";
		}
	}
}
