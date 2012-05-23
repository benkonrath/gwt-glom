package org.glom.web.shared.libglom.layout;

import org.glom.web.client.StringUtils;
import org.glom.web.shared.libglom.Relationship;

public class LayoutItemPortal extends LayoutGroup implements UsesRelationship {
	private static final long serialVersionUID = 4952677991725269830L;

	/*
	 * Don't make this final, because that breaks GWT serialization. See
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1054
	 */
	private/* final */UsesRelationship usesRel = new UsesRelationshipImpl();

	public enum NavigationType {
		NAVIGATION_NONE, NAVIGATION_AUTOMATIC, NAVIGATION_SPECIFIC
	}

	private NavigationType navigationType = NavigationType.NAVIGATION_AUTOMATIC;
	private UsesRelationship navigationRelationshipSpecific = null;

	/**
	 * @return
	 */
	public String getFromField() {
		String result = null;

		final Relationship relationship = getRelationship();
		if (relationship != null) {
			result = relationship.getFromField();
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
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_join_alias_name()
	 */
	@Override
	public String getSqlJoinAliasName() {
		return usesRel.getSqlJoinAliasName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
	 */
	@Override
	public String getSqlTableOrJoinAliasName(final String tableName) {
		return usesRel.getSqlTableOrJoinAliasName(tableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String getTableUsed(final String parentTable) {
		return usesRel.getTableUsed(parentTable);
	}

	/**
	 * @return
	 */
	public NavigationType getNavigationType() {
		return navigationType;
	}

	/**
	 * @param navigationAutomatic
	 */
	public void setNavigationType(final NavigationType navigationType) {
		this.navigationType = navigationType;
	}

	/**
	 * @return
	 */
	public UsesRelationship getNavigationRelationshipSpecific() {
		if (getNavigationType() == NavigationType.NAVIGATION_SPECIFIC) {
			return navigationRelationshipSpecific;
		} else {
			return null;
		}
	}

	/**
	 * @return
	 */
	public void setNavigationRelationshipSpecific(final UsesRelationship relationship) {
		navigationRelationshipSpecific = relationship;
		navigationType = NavigationType.NAVIGATION_SPECIFIC;
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

	@Override
	public String getTitleOriginal() {
		String title = getTitleUsed("" /* parent table - not relevant */, "" /* locale */);
		if (StringUtils.isEmpty(title)) {
			title = "Undefined Table";
		}

		return title;
	}

	@Override
	public String getTitle(final String locale) {
		String title = getTitleUsed("" /* parent table - not relevant */, locale);
		if (StringUtils.isEmpty(title)) {
			title = "Undefined Table";
		}

		return title;
	}

	@Override
	public String getTitleOrName(final String locale) {
		String title = getTitleUsed("" /* parent table - not relevant */, locale);
		if (StringUtils.isEmpty(title)) {
			title = getRelationshipNameUsed();
		}

		if (StringUtils.isEmpty(title)) {
			title = "Undefined Table";
		}

		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#getTitleUsed(java.lang.String, java.lang.String)
	 */
	@Override
	public String getTitleUsed(final String parentTableTitle, final String locale) {
		return usesRel.getTitleUsed(parentTableTitle, locale);
	}
}
