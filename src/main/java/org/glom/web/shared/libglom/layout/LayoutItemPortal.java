package org.glom.web.shared.libglom.layout;

import org.glom.web.shared.libglom.Relationship;

@SuppressWarnings("serial")
public class LayoutItemPortal extends LayoutGroup implements UsesRelationship {

	private UsesRelationship usesRel = new UsesRelationshipImpl();

	public enum NavigationType {
		NAVIGATION_NONE, NAVIGATION_AUTOMATIC, NAVIGATION_SPECIFIC
	}

	private NavigationType navigationType = NavigationType.NAVIGATION_AUTOMATIC;
	private UsesRelationship navigationRelationshipSpecific = null;
	private boolean addNavigation = false;

	/**
	 * @return
	 */
	public String getFromField() {
		String from_table = null;

		final Relationship relationship = getRelationship();
		if (relationship != null) {
			from_table = relationship.getFromTable();
		}

		return from_table;
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
	public String getSqlJoinAliasName() {
		return usesRel.getSqlJoinAliasName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_sql_table_or_join_alias_name(java.lang.String)
	 */
	@Override
	public String getSqlTableOrJoinAliasName(String tableName) {
		return usesRel.getSqlTableOrJoinAliasName(tableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String getTableUsed(String parentTable) {
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
	public void setNavigationType(NavigationType navigationType) {
		this.navigationType = navigationType;
	}

	/**
	 * @return
	 */
	public UsesRelationship getNavigationRelationshipSpecific() {
		if (getNavigationType() == NavigationType.NAVIGATION_SPECIFIC)
			return navigationRelationshipSpecific;
		else
			return null;
	}

	/**
	 * @return
	 */
	public void setNavigationRelationshipSpecific(UsesRelationship relationship) {
		navigationRelationshipSpecific = relationship;
		navigationType = NavigationType.NAVIGATION_SPECIFIC;
	}

	// TODO: Where is getAddNavigation?
	/**
	 * Whether the UI should show a navigation button. TODO: Remove this?
	 * 
	 * @param b
	 */
	public void setAddNavigation(boolean addNavigation) {
		this.addNavigation = addNavigation;
	}

	@Override
	public Object clone() {
		LayoutItemPortal result = (LayoutItemPortal) super.clone();

		result.usesRel = (UsesRelationshipImpl) this.usesRel.clone();
		result.navigationRelationshipSpecific = (UsesRelationship) this.navigationRelationshipSpecific.clone();
		result.navigationType = this.navigationType;
		result.addNavigation = this.addNavigation;

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
