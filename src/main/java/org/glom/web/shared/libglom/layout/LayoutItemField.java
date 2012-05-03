package org.glom.web.shared.libglom.layout;

import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Field.GlomFieldType;
import org.glom.web.shared.libglom.Relationship;

@SuppressWarnings("serial")
public class LayoutItemField extends LayoutItemWithFormatting implements UsesRelationship {
	private Field field;
	private UsesRelationship usesRel = new UsesRelationshipImpl();
	private Formatting formatting = new Formatting();
	private boolean useDefaultFormatting = true;

	/**
	 * @return the field
	 */
	public Field getFullFieldDetails() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setFullFieldDetails(final Field field) {
		this.field = field;
	}

	/**
	 * @return
	 */
	public Formatting getFormattingUsed() {
		if (useDefaultFormatting && (field != null)) {
			return field.getFormatting();
		} else {
			return formatting;
		}
	}

	/**
	 * @return
	 */
	public GlomFieldType getGlomType() {
		if (field != null) {
			return field.getGlomType();
		}

		return GlomFieldType.TYPE_INVALID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String getTableUsed(final String tableName) {
		return usesRel.getTableUsed(tableName);
	}

	/**
	 * @param forDetailsView
	 * @return
	 */
	public Formatting.HorizontalAlignment getFormattingUsedHorizontalAlignment(final boolean forDetailsView) {
		return null; // TODO
	}

	// TODO: This should actually be in LayoutItem, with an override here.
	/**
	 * @return
	 */
	public String getLayoutDisplayName() {
		String result = "";

		if (field != null) {
			result = field.getName();
		} else {
			result = getName();
		}

		// Indicate if it's a field in another table.
		if (getHasRelatedRelationshipName()) {
			final Relationship rel = getRelatedRelationship();
			if (rel != null) {
				result = rel.getName() + "::" + result;
			}
		}

		if (getHasRelationshipName()) {
			final Relationship rel = getRelationship();
			if (rel != null) {
				result = rel.getName() + "::" + result;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.glom.web.shared.libglom.layout.UsesRelationship#setRelationship(org.glom.web.shared.libglom.Relationship)
	 */
	@Override
	public void setRelationship(final Relationship relationship) {
		usesRel.setRelationship(relationship);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getRelationship()
	 */
	@Override
	public Relationship getRelationship() {
		return usesRel.getRelationship();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getHasRelationshipName()
	 */
	@Override
	public boolean getHasRelationshipName() {
		return usesRel.getHasRelationshipName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.glom.web.shared.libglom.layout.UsesRelationship#setRelatedRelationship(org.glom.web.shared.libglom.Relationship
	 * )
	 */
	@Override
	public void setRelatedRelationship(final Relationship relationship) {
		usesRel.setRelatedRelationship(relationship);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getRelatedRelationship()
	 */
	@Override
	public Relationship getRelatedRelationship() {
		return usesRel.getRelatedRelationship();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getHasRelatedRelationshipName()
	 */
	@Override
	public boolean getHasRelatedRelationshipName() {
		return usesRel.getHasRelatedRelationshipName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getSqlJoinAliasName()
	 */
	@Override
	public String getSqlJoinAliasName() {
		return usesRel.getSqlJoinAliasName();
	}

	/**
	 * @param attribute
	 */
	public void setUseDefaultFormatting(final boolean useDefaultFormatting) {
		this.useDefaultFormatting = useDefaultFormatting;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getSqlTableOrJoinAliasName(java.lang.String)
	 */
	@Override
	public String getSqlTableOrJoinAliasName(final String tableName) {
		return usesRel.getSqlTableOrJoinAliasName(tableName);
	}

	/**
	 * @return
	 */
	public boolean getAddNavigation() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public String getNavigationTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		final LayoutItemField result = (LayoutItemField) super.clone();

		result.field = (Field) this.field.clone();
		result.usesRel = (UsesRelationship) this.usesRel.clone();
		result.useDefaultFormatting = this.useDefaultFormatting;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getRelationshipNameUsed()
	 */
	@Override
	public String getRelationshipNameUsed() {
		return usesRel.getRelationshipNameUsed();
	}

}