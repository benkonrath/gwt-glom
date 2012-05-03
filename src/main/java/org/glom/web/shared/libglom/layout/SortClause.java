package org.glom.web.shared.libglom.layout;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class SortClause extends ArrayList<SortClause.SortField> {
	
	public static class SortField implements Cloneable {
		public SortField() {
		}
		
		public SortField(UsesRelationship field, boolean ascending) {
			this.field = field;
			this.ascending = ascending;
		}
		
		@Override
		public Object clone() {
			SortField result;
			try {
				result = (SortField)super.clone();
			} catch (CloneNotSupportedException e) {
				 System.err.println("SortField.clone() failed: " + e.getMessage());
				 return null;
			}

			result.field = (UsesRelationship)this.field.clone();
			result.ascending = this.ascending;

			return result;
		}

		public UsesRelationship field;
		public boolean ascending;
	}
	
	@Override
	public Object clone() {
		SortClause result = (SortClause)super.clone();

		result.clear();
		for(SortField item : this) {
			final SortField clone = (SortField)item.clone();
			result.add(clone);
		}

		return result;
	}
}
