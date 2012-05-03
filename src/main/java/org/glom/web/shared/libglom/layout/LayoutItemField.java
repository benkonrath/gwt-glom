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
	public Field get_full_field_details() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void set_full_field_details(Field field) {
		this.field = field;
	}

	/**
	 * @return
	 */
	public Formatting get_formatting_used() {
		if (useDefaultFormatting && (field != null)) {
			return field.getFormatting();
		} else {
			return formatting;
		}
	}

	/**
	 * @return
	 */
	public GlomFieldType get_glom_type() {
		if (field != null) {
			return field.get_glom_type();
		}

		return GlomFieldType.TYPE_INVALID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String get_table_used(String tableName) {
		return usesRel.get_table_used(tableName);
	}

	/**
	 * @param forDetailsView
	 * @return
	 */
	public Formatting.HorizontalAlignment get_formatting_used_horizontal_alignment(boolean forDetailsView) {
		return null; // TODO
	}

	// TODO: This should actually be in LayoutItem, with an override here.
	/**
	 * @return
	 */
	public String get_layout_display_name() {
		String result = "";

		if (field != null) {
			result = field.get_name();
		} else {
			result = get_name();
		}

		// Indicate if it's a field in another table.
		if (getHasRelatedRelationshipName()) {
			final Relationship rel = getRelatedRelationship();
			if (rel != null) {
				result = rel.get_name() + "::" + result;
			}
		}

		if (getHasRelationshipName()) {
			final Relationship rel = getRelationship();
			if (rel != null) {
				result = rel.get_name() + "::" + result;
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
	public void setRelationship(Relationship relationship) {
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
	public void setRelatedRelationship(Relationship relationship) {
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
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_join_alias_name()
	 */
	@Override
	public String get_sql_join_alias_name() {
		return usesRel.get_sql_join_alias_name();
	}

	/**
	 * @param attribute
	 */
	public void setUseDefaultFormatting(boolean useDefaultFormatting) {
		this.useDefaultFormatting = useDefaultFormatting;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
	 */
	@Override
	public String get_sql_table_or_join_alias_name(String tableName) {
		return usesRel.get_sql_table_or_join_alias_name(tableName);
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
		LayoutItemField result = (LayoutItemField) super.clone();

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
