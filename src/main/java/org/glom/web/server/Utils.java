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

package org.glom.web.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field.GlomFieldType;
import org.glom.web.shared.libglom.layout.LayoutItemField;

/**
 *
 */
public class Utils {

	/*
	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
	 * 
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	public static int safeLongToInt(final long value) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
		}
		return (int) value;
	}

	public static String getFileName(final String fileURI) {
		final String[] splitURI = fileURI.split(File.separator);
		return splitURI[splitURI.length - 1];
	}

	static public Object deepCopy(final Object oldObj) {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();
			final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);

			// return the new object
			return ois.readObject();
		} catch (final Exception e) {
			System.out.println("Exception in deepCopy:" + e);
			return null;
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (final IOException e) {
				System.out.println("Exception in deepCopy during finally: " + e);
				return null;
			}
		}
	}

	/** Build the URL for the service that will return the binary data for an image.
	 * 
	 * @param primaryKeyValue
	 * @param field
	 * @return
	 */
	public static String buildImageDataUrl(final TypedDataItem primaryKeyValue, final String documentID, final String tableName, final LayoutItemField field) {
		final URIBuilder uriBuilder = buildImageDataUrlStart(documentID, tableName);
		
		//TODO: Handle other types:
		if(primaryKeyValue != null) {
			uriBuilder.setParameter("value", Double.toString(primaryKeyValue.getNumber()));
		}
		
		uriBuilder.setParameter("field", field.getName());
		return uriBuilder.toString();
	}

	/** Build the URL for the service that will return the binary data for an image.
	 * 
	 * @param primaryKeyValue
	 * @param field
	 * @return
	 */
	public static String buildImageDataUrl(final String documentID, final String tableName, final String layoutName, final int[] path) {
		final URIBuilder uriBuilder = buildImageDataUrlStart(documentID, tableName);
		uriBuilder.setParameter("layout", layoutName);
		uriBuilder.setParameter("layoutpath", buildLayoutPath(path));
		return uriBuilder.toString();
	}

	/**
	 * @param documentID
	 * @param tableName
	 * @return
	 */
	private static URIBuilder buildImageDataUrlStart(final String documentID, final String tableName) {
		final URIBuilder uriBuilder = new URIBuilder();
		//uriBuilder.setHost(GWT.getModuleBaseURL());
		uriBuilder.setPath("OnlineGlom/gwtGlomImages"); //The name of our images servlet. See OnlineGlomImagesServlet.
		uriBuilder.setParameter("document", documentID);
		uriBuilder.setParameter("table", tableName);
		return uriBuilder;
	}

	/** Build a :-separated string to represent the path as a string.
	 * @param path
	 * @return
	 */
	public static String buildLayoutPath(int[] path) {
		if((path == null) || (path.length == 0)) {
			return null;
		}

		String result = new String();
		for(int i:path) {
			if(!result.isEmpty()) {
				result += ":";
			}
			
			final String strIndex = Integer.toString(i);
			result += strIndex;
		}
		
		return result;
	}

	/** Get an array of int indices from the :-separated string.
	 * See buildLayoutPath().
	 * 
	 * @param attrLayoutPath
	 * @return The array of indices of the layout items.
	 */
	public static int[] parseLayoutPath(final String attrLayoutPath) {
		if(StringUtils.isEmpty(attrLayoutPath)) {
			return null;
		}
		
		final String[] strIndices = attrLayoutPath.split(":");
		final int[] indices = new int[strIndices.length];
		for (int i = 0; i < strIndices.length; ++i) {
			final String str = strIndices[i];
	
			try
			{
				indices[i] = Integer.parseInt(str);
			}
			catch (final NumberFormatException nfe)
			{
				//TODO: Log the error.
				return null;
			}
		}
	
		return indices;
	}

	public static void transformUnknownToActualType(final TypedDataItem dataItem, final GlomFieldType actualType) {
		if(dataItem.getType() == actualType)
			return;
		
		String unknownText = dataItem.getUnknown();
		
		//Avoid repeated checks for null:
		if (unknownText == null) {
			unknownText = "";
		}
	
		switch(actualType) {
		case TYPE_NUMERIC:
			// TODO: Is this really locale-independent?
			double number = 0;
			if(!StringUtils.isEmpty(unknownText)) {
				try {
					number = Double.parseDouble(unknownText);		
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				}
			}

			dataItem.setNumber(number);
			break;
		case TYPE_TEXT:
			dataItem.setText(unknownText);
			break;
		case TYPE_DATE:
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	        Date date = null;
			try {
				date = formatter.parse(unknownText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			dataItem.setDate(date); 
			break;
		case TYPE_TIME:
			/*TODO :
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	        Date date;
			try {
				date = formatter.parse(unknownText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			setDate(date); 
			*/
			break;
		case TYPE_BOOLEAN:
			final boolean bool = unknownText.equals("true");
			dataItem.setBoolean(bool); //TODO
			break;
		case TYPE_IMAGE:
			dataItem.setImageDataUrl(unknownText);
			//setImageData(null);//TODO: Though this is only used for primary keys anyway.
			break;
		case TYPE_INVALID:
			break;
		default:
			break; //TODO: Warn because this is unusual?
		}
	}

}
