package com.gpt.component.locale;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.hazelcast.core.MapLoader;

public class LocaleLoader implements MapLoader<String, String> {

	private String locale;
	
	public LocaleLoader(String locale) {
		this.locale = locale;
	}
	
	@Override
	public String load(String key) {
		return LocaleLoaderHelper.getInstance().load(locale, key);
	}

	@Override
	public Map<String, String> loadAll(Collection<String> keys) {
		return LocaleLoaderHelper.getInstance().loadAll(locale, keys);
	}

	@Override
	public Iterable<String> loadAllKeys() {
		return Collections.emptyList();
//		return LocaleLoaderHelper.getInstance().loadAllKeys(locale);
	}
	
}
