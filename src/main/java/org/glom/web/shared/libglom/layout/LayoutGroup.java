package org.glom.web.shared.libglom.layout;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class LayoutGroup extends LayoutItem {

	private class LayoutItemList extends ArrayList<LayoutItem> {
		public Object clone() {
			LayoutItemList result = (LayoutItemList)super.clone();
			    
			//TODO: Performance: There is probably a better way to do this.
			result.clear();
			for(LayoutItem item : this) {
				result.add(item);
			}
			
			return result;
		}
	};
	
	private LayoutItemList items = new LayoutItemList();

	/**
	 * @return
	 */
	public List<LayoutItem> get_items() {
		return items;
	}

	/**
	 * @param layoutItemField
	 */
	public void add_item(LayoutItem layoutItem) {
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
	public void setExpectedResultSize(int expectedResultSize) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public List<String> getItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param b
	 */
	public void setHiddenPrimaryKey(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param i
	 */
	public void setPrimaryKeyIndex(int i) {
		// TODO Auto-generated method stub

	}

	public Object clone() {
		LayoutGroup result = (LayoutGroup)super.clone();

		result.items = (LayoutItemList)this.items.clone();

		return result;
	}

}
