package org.glom.web.shared.libglom.layout;

import java.util.ArrayList;
import java.util.List;

public class LayoutGroup extends LayoutItem {

	private static final long serialVersionUID = 2795852472980010553L;
	private int columnCount = 0;

	// Extras:

	// This is maybe only used in top-level List groups and portals.
	// This is the primary key index of the LayoutFieldVector that is used for getting the SQL query. It's being used
	// here to avoid having to set an isPrimaryKey boolean with every LayoutItemField. This also has the advantage of
	// not having to iterate through all of the LayoutItemFields to find the primary key index on the client side.
	private int primaryKeyIndex = -1;

	// This is maybe only used in top-level List groups and portals.
	// indicates if the primary key is hidden and has been added to the end of the LayoutListFields list and the
	// database data list (DataItem).
	private boolean hiddenPrimaryKey = false;

	// expectedResultSize is used only for the list layout
	private int expectedResultSize = -1;

	/**
	 * @param columnCount
	 *            the columnCount to set
	 */
	public void setColumnCount(final int columnCount) {
		this.columnCount = columnCount;
	}

	static protected class LayoutItemList extends ArrayList<LayoutItem> {
		private static final long serialVersionUID = 8610424318876440333L;
	};

	/*
	 * Don't make this final, because that breaks GWT serialization. See
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1054
	 */
	private/* final */LayoutItemList items = new LayoutItemList();

	/**
	 * @return
	 */
	public List<LayoutItem> getItems() {
		return items;
	}

	/**
	 * @param layoutItemField
	 */
	public void addItem(final LayoutItem layoutItem) {
		items.add(layoutItem);
	}

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @param expectedResultSize
	 */
	public void setExpectedResultSize(final int expectedResultSize) {
		this.expectedResultSize = expectedResultSize;
	}

	/**
	 * @return
	 */
	public int getExpectedResultSize() {
		return expectedResultSize;
	}

	/**
	 * @return
	 */
	public int getPrimaryKeyIndex() {
		return primaryKeyIndex;
	}

	/**
	 * @param primaryKeyIndex
	 */
	public void setPrimaryKeyIndex(final int primaryKeyIndex) {
		this.primaryKeyIndex = primaryKeyIndex;

	}

	/**
	 * @param hiddenPrimaryKey
	 */
	public void setHiddenPrimaryKey(final boolean hiddenPrimaryKey) {
		this.hiddenPrimaryKey = hiddenPrimaryKey;
	}

	/**
	 * @return
	 */
	public boolean hasHiddenPrimaryKey() {
		return hiddenPrimaryKey;
	}
}
