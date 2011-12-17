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

package org.glom.web.client.mvp;

import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.place.DocumentSelectionPlace;
import org.glom.web.client.place.ListPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be aware of. This is
 * done via the @WithTokenizers annotation or by extending PlaceHistoryMapperWithFactory and creating a separate
 * TokenizerFactory.
 * 
 * This code is mostly from AppPlaceHistoryMapper.java in the hellomvp GWT example:
 * 
 * https://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html
 */
@WithTokenizers({ ListPlace.Tokenizer.class, DocumentSelectionPlace.Tokenizer.class, DetailsPlace.Tokenizer.class })
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper {
}
