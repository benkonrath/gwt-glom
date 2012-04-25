package org.glom.web.shared.libglom;

import java.util.Hashtable;

public class Translatable {
	public String original = "";
	
	// A map of localeID to title:
	public Hashtable<String, String> translationsMap = new Hashtable<String, String>();
	
	
	public String get_title_original() {
		return original;
	}
	
	public String get_title(final String locale) {
		final String title = translationsMap.get(locale);
		if(title == null) {
			return "";
		}
	
		return title;
	}
}
