package org.glom.web.shared.libglom;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;

import org.glom.web.client.StringUtils;

@SuppressWarnings("serial")
public class Translatable implements Serializable, Cloneable {

	// We use TreeMap instead of HashTable because GWT does not support HashTable.
	public static class TranslationsMap extends TreeMap<String, String> {
		@Override
		public Object clone() {
			final TranslationsMap result = (TranslationsMap) super.clone();

			// TODO: Performance: There is probably a better way to do this.
			result.clear();
			final Set<String> keys = this.keySet();
			for (final String key : keys) {
				final String value = this.get(key);
				result.put(key, value);
			}

			return result;
		}
	};

	private String name = "";
	private String titleOriginal = "";

	// A map of localeID to title:
	TranslationsMap translationsMap = new TranslationsMap();

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getTitleOriginal() {
		return titleOriginal;
	}

	public void setTitleOriginal(final String title) {
		this.titleOriginal = title;
	}

	public String getTitle() {
		return getTitleOriginal();
	}

	public String getTitle(final String locale) {
		if (StringUtils.isEmpty(locale)) {
			return getTitleOriginal();
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
	public String getTitleOrName(final String locale) {
		final String title = getTitle(locale);
		if (StringUtils.isEmpty(title)) {
			return getName();
		}

		return title;
	};

	/**
	 * Make sure that getTitle() or getTitleOriginal() returns the specified translation. And discard all translations.
	 * You should probably only call this on a clone()ed item.
	 * 
	 * @param locale
	 */
	public void makeTitleOriginal(final String locale) {
		final String title = getTitle(locale);
		translationsMap.clear();
		setTitleOriginal(title);
	}

	/**
	 * @override
	 */
	@Override
	public Object clone() {
		Translatable result = null;
		try {
			result = (Translatable) super.clone();
		} catch (final CloneNotSupportedException e) {
			System.err.println("Translatable.clone() failed: " + e.getMessage());
			return null;
		}

		result.name = new String(this.name);
		result.titleOriginal = new String(this.titleOriginal);
		result.translationsMap = (TranslationsMap) this.translationsMap.clone();
		return result;
	}

	/**
	 * @param translatedTitle
	 * @param locale
	 */
	public void setTitle(final String title, final String locale) {
		if (StringUtils.isEmpty(locale)) {
			setTitleOriginal(title);
			return;
		}

		translationsMap.put(locale, title);
	}
}
