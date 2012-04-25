package org.glom.web.shared.libglom;

import java.util.Hashtable;

import org.glom.web.client.StringUtils;

public class Translatable {
	
	private String name = "";
	private String title_original = "";
	
	// A map of localeID to title:
	public Hashtable<String, String> translationsMap = new Hashtable<String, String>();
	
	public String get_name() {
		return name;
	}
	
	public void set_name(final String name) {
		this.name = name;
	}
	
	public String get_title_original() {
		return title_original;
	}

	public void set_title_original(final String title) {
		this.title_original = title;
	}
	
	public String get_title(final String locale) {
		final String title = translationsMap.get(locale);
		if(title == null) {
			return "";
		}
	
		return title;
	}
	
	/**
	 * @param locale
	 * @return
	 */
	public String get_title_or_name(String locale) {
		final String title = get_title(locale);
		if(StringUtils.isEmpty(title)) {
			return get_name();
		}
		
		return title;
	};
}
