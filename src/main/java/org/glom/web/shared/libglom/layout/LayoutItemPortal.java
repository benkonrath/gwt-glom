package org.glom.web.shared.libglom.layout;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.libglom.Document;
import org.glom.web.shared.libglom.Relationship;
import org.jfree.util.Log;

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
			from_table = relationship.get_from_table();
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
	public String get_sql_join_alias_name() {
		return usesRel.get_sql_join_alias_name();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.libglom.layout.UsesRelationship#get_table_used(java.lang.String)
	 */
	@Override
	public String get_table_used(String parentTable) {
		return usesRel.get_table_used(parentTable);
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

	public class TableToViewDetails {
		public String tableName;
		public UsesRelationship usesRelationship;
	};

	/**
	 * @param tableName
	 *            Output parameter
	 * @param relationship
	 * @param document
	 */
	public TableToViewDetails get_suitable_table_to_view_details(final Document document) {
		UsesRelationship navigation_relationship = null;

		// Check whether a relationship was specified:
		if (getNavigationType() == NavigationType.NAVIGATION_AUTOMATIC) {
			navigation_relationship = getPortalNavigationRelationshipAutomatic(document);
		} else {
			navigation_relationship = getNavigationRelationshipSpecific();
		}

		// Get the navigation table name from the chosen relationship:
		String directly_related_table_name = get_table_used("" /* not relevant */);

		// The navigation_table_name (and therefore, the table_name output parameter,
		// as well) stays empty if the navrel type was set to none.
		String navigation_table_name = null;
		if (navigation_relationship != null) {
			navigation_table_name = navigation_relationship.get_table_used(directly_related_table_name);
		} else if (getNavigationType() != NavigationType.NAVIGATION_NONE) {
			// An empty result from get_portal_navigation_relationship_automatic() or
			// get_navigation_relationship_specific() means we should use the directly related table:
			navigation_table_name = directly_related_table_name;
		}

		if (StringUtils.isEmpty(navigation_table_name)) {
			return null;
		}

		if (document == null) {
			Log.error("document is null.");
			return null;
		}

		if (document.get_table_is_hidden(navigation_table_name)) {
			Log.error("navigation_table_name indicates a hidden table: " + navigation_table_name);
			return null;
		}

		TableToViewDetails result = new TableToViewDetails();
		result.tableName = navigation_table_name;
		result.usesRelationship = navigation_relationship;
		return result;
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

	/**
	 * @param document
	 * @return
	 */
	private UsesRelationship getPortalNavigationRelationshipAutomatic(Document document) {
		if (document == null) {
			return null;
		}

		// If the related table is not hidden then we can just navigate to that:
		final String direct_related_table_name = get_table_used("" /* parent table - not relevant */);
		if (!document.get_table_is_hidden(direct_related_table_name)) {
			// Non-hidden tables can just be shown directly. Navigate to it:
			return null;
		} else {
			// If the related table is hidden,
			// then find a suitable related non-hidden table by finding the first layout field that mentions one:
			final LayoutItemField field = getFieldIsFromNonHiddenRelatedRecord(document);
			if (field != null) {
				return field; // Returns the UsesRelationship base part. (A relationship belonging to the portal's
								// related table.)
			} else {
				// Instead, find a key field that's used in a relationship,
				// and pretend that we are showing the to field as a related field:
				final FieldIdentifies fieldIndentifies = get_field_identifies_non_hidden_related_record(document);
				if (fieldIndentifies != null) {
					if (fieldIndentifies.usedInRelationShip != null) {
						UsesRelationship result = new UsesRelationshipImpl();
						result.setRelationship(fieldIndentifies.usedInRelationShip);
						return result;
					}
				}
			}
		}

		// There was no suitable related table to show:
		return null;
	}

	class FieldIdentifies {
		public LayoutItemField field;
		public Relationship usedInRelationShip;
	}

	/**
	 * @param used_in_relationship
	 * @param document
	 * @return
	 */
	private FieldIdentifies get_field_identifies_non_hidden_related_record(Document document) {
		// Find the first field that is from a non-hidden related table.

		if (document == null) {
			Log.error("document is null");
			return null;
		}

		final String parent_table_name = get_table_used("" /* parent table - not relevant */);

		List<LayoutItem> items = get_items();
		for (LayoutItem item : items) {
			if (item instanceof LayoutItemField) {
				LayoutItemField field = (LayoutItemField) item;
				if (field.getHasRelationshipName()) {
					final Relationship relationship = document
							.getFieldUsedInRelationshipToOne(parent_table_name, field);
					if (relationship != null) {
						final String table_name = relationship.get_to_table();
						if (!StringUtils.isEmpty(table_name)) {
							if (!(document.get_table_is_hidden(table_name))) {

								FieldIdentifies result = new FieldIdentifies();
								result.field = field;
								result.usedInRelationShip = relationship;
								return result;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * @param document
	 * @return
	 */
	private LayoutItemField getFieldIsFromNonHiddenRelatedRecord(Document document) {
		// Find the first field that is from a non-hidden related table.

		if (document == null) {
			return null;
		}

		LayoutItemField result = null;

		final String parent_table_name = get_table_used("" /* parent table - not relevant */);

		final List<LayoutItem> items = get_items();
		for (LayoutItem item : items) {
			if (item instanceof LayoutItemField) {
				LayoutItemField field = (LayoutItemField) item;
				if (field.getHasRelationshipName()) {
					final String table_name = field.get_table_used(parent_table_name);
					if (!(document.get_table_is_hidden(table_name)))
						return field;
				}
			}
		}

		return result;
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
