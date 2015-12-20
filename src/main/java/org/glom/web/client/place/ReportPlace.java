/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.client.place;

import java.util.HashMap;

import org.glom.web.client.StringUtils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ReportPlace extends HasRecordsPlace {

	private final String reportName;

	public ReportPlace(final String documentID, final String tableName, final String reportName, final String quickFind) {
		super(documentID, tableName, quickFind);
		this.reportName = reportName;
	}

	public String getReportName() {
		return reportName;
	}

	@Prefix("report")
	public static class Tokenizer extends HasRecordsPlace.Tokenizer implements PlaceTokenizer<ReportPlace> {
		protected final String reportKey = "report";

		@Override
		public String getToken(final ReportPlace place) {
			final HashMap<String, String> params = new HashMap<>();
			params.put(documentKey, place.getDocumentID());
			params.put(tableKey, place.getTableName());
			params.put(reportKey, place.getReportName());
			params.put(quickFindKey, place.getQuickFind());
			return buildParamsToken(params);
		}

		@Override
		public ReportPlace getPlace(final String token) {
			// default empty values
			String documentID = "";
			String tableName = ""; // an empty value represents the default table
			String reportName = "";
			String quickFind = "";

			final HashMap<String, String> params = getTokenParams(token);

			if (params == null) {
				return new ReportPlace("", "", "", "");
			}

			if (params.get(documentKey) != null) {
				documentID = params.get(documentKey);
			}

			if (params.get(tableKey) != null) {
				tableName = params.get(tableKey);
			}

			if (params.get(quickFindKey) != null) {
				quickFind = params.get(quickFindKey);
			}

			if (params.get(reportKey) != null) {
				reportName = params.get(reportKey);
			}

			if (StringUtils.isEmpty(documentID)) {
				// The documentID was not retrieved from the URL. Use empty values for the list place.
				return new ReportPlace("", "", "", "");
			}

			return new ReportPlace(documentID, tableName, reportName, quickFind);
		}
	}

}
