package org.glom.web.shared.libglom;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;

import org.glom.web.client.StringUtils;

@SuppressWarnings("serial")
public class Translatable implements Serializable, Cloneable {

	// TODO: Use Hashmap everywhere instead?
	public static class TranslationsMap extends Hashtable<String, String> {
		@Override
		public Object clone() {
			TranslationsMap result = (TranslationsMap) super.clone();

			// TODO: Performance: There is probably a better way to do this.
			result.clear();
			final Set<String> keys = this.keySet();
			for (String key : keys) {
				final String value = this.get(key);
				result.put(key, value);
			}

			return result;
		}
	};

	private String name = "";
	private String title_original = "";

	// A map of localeID to title:
	TranslationsMap translationsMap = new TranslationsMap();

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

	public String get_title() {
		return get_title_original();
	}

	public String get_title(final String locale) {
		if (StringUtils.isEmpty(locale)) {
			return get_title_original();
		}

		final String title = translationsMap.get(locale);
		if (title == null) {
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
		if (StringUtils.isEmpty(title)) {
			return get_name();
		}

		return title;
	};

	/**
	 * Make sure that getTitle() or getTitleOriginal() returns the specified translation. And discard all translations.
	 * You should probably only call this on a clone()ed item.
	 * 
	 * @param locale
	 */
	public void makeTitleOriginal(String locale) {
		final String title = get_title(locale);
		translationsMap.clear();
		set_title_original(title);
	}

	/**
	 * @override
	 */
	@Override
	public Object clone() {
		Translatable result = null;
		try {
			result = (Translatable) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Translatable.clone() failed: " + e.getMessage());
			return null;
		}

		result.name = new String(this.name);
		result.title_original = new String(this.title_original);
		result.translationsMap = (TranslationsMap) this.translationsMap.clone();
		return result;
	}
}
