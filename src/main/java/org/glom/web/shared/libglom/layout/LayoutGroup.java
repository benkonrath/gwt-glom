package org.glom.web.shared.libglom.layout;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class LayoutGroup extends LayoutItem {

	private class LayoutItemList extends ArrayList<LayoutItem> {
		@Override
		public Object clone() {
			final LayoutItemList result = (LayoutItemList) super.clone();

			// TODO: Performance: There is probably a better way to do this.
			result.clear();
			for (final LayoutItem item : this) {
				result.add(item);
			}

			return result;
		}
	};

	private LayoutItemList items = new LayoutItemList();

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
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return
	 */
	public int getExpectedResultSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return
	 */
	public int getPrimaryKeyIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param expectedResultSize
	 */
	public void setExpectedResultSize(final int expectedResultSize) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param b
	 */
	public void setHiddenPrimaryKey(final boolean b) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param i
	 */
	public void setPrimaryKeyIndex(final int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object clone() {
		final LayoutGroup result = (LayoutGroup) super.clone();

		result.items = (LayoutItemList) this.items.clone();

		return result;
	}

}
