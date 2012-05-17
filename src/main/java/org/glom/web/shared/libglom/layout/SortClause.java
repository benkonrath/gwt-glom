package org.glom.web.shared.libglom.layout;

import java.io.Serializable;
import java.util.ArrayList;

public class SortClause extends ArrayList<SortClause.SortField> {

	private static final long serialVersionUID = 4211595362491092668L;

	public static class SortField implements Serializable {

		private static final long serialVersionUID = -4946144159226347837L;

		public SortField() {
		}

		public SortField(final UsesRelationship field, final boolean ascending) {
			this.field = field;
			this.ascending = ascending;
		}

		public UsesRelationship field;
		public boolean ascending;
	}
}
