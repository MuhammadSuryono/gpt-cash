package com.gpt.component.locale;

import java.util.Properties;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStoreFactory;

public class LocaleLoaderFactory implements MapStoreFactory<String, String> {
	
	@SuppressWarnings("unchecked")
	@Override
	public MapLoader<String, String> newMapStore(String mapName, Properties properties) {
		String[] names = mapName.split("[.]");
		try {
			/**
			 *  Somehow there is a messy classloader involved here, just to make sure that we are using the intended classloader 
			 *  when creating the instance here
			 */
			return (MapLoader<String, String>)Thread.currentThread().getContextClassLoader().loadClass(LocaleLoader.class.getName()).getConstructor(String.class).newInstance(names[names.length-1]);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
