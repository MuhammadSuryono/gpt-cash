package com.gpt.component.locale;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component("messageSource")
public class DistributedMessageResource extends AbstractMessageSource {
	@Autowired
	private HazelcastInstance hzInstance;
	
	@Autowired
	private Broadcaster broadcaster;
	
	private String broadcasterId;
	
	private final Map<String, String> mapNameCache = new ConcurrentHashMap<>();
	
	private final Map<String, Map<String, MessageFormat>> cachedMessageFormats = new HashMap<>();
	
	@PostConstruct
	public void init() {
		broadcasterId = broadcaster.registerListener(ApplicationConstants.BROADCAST_LOCALIZATION, (String message) -> {
			invalidateCache(message);
		});
	}
	
	@PreDestroy
	public void destroy() {
		synchronized (cachedMessageFormats) {
			cachedMessageFormats.clear();
		}
		if(broadcasterId != null)
			broadcaster.removeListener(ApplicationConstants.BROADCAST_LOCALIZATION, broadcasterId);
	}
	
	private String getMapName(Locale locale) {
		String mapName = mapNameCache.get(locale.getLanguage());
		if(mapName == null) {
			mapName = LocaleLoaderHelper.getMapName(locale);
			mapNameCache.put(locale.getLanguage(), mapName);
		}
		return mapName;
	}
	
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		IMap<String, String> mapLocale = hzInstance.getMap(getMapName(locale));
		return mapLocale.get(code);
	}
	
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String mapName = getMapName(locale);

		Map<String, MessageFormat> codeMap = cachedMessageFormats.get(mapName);
		if (codeMap != null) {
			MessageFormat result = codeMap.get(code);
			if (result != null) {
				return result;
			}
		}
		
		synchronized (cachedMessageFormats) {
			// other might have created this while we wait for synchronization
			if(codeMap == null)
				codeMap = cachedMessageFormats.get(mapName);

			if (codeMap != null) {
				MessageFormat result = codeMap.get(code);
				if (result != null) {
					return result;
				}
			}

			IMap<String, String> mapLocale = hzInstance.getMap(mapName);
			String msg = mapLocale.get(code);
			if (msg != null) {
				if (codeMap == null) {
					codeMap = new HashMap<>();
					cachedMessageFormats.put(mapName, codeMap);
				}
				MessageFormat result = createMessageFormat(msg, locale);
				codeMap.put(code, result);
				return result;
			}

			return null;
		}		
	}
	
	private void invalidateCache(String code) {
		mapNameCache.values().forEach( language -> {
			IMap<String, String> map = hzInstance.getMap(language);
			map.remove(code);
			synchronized(cachedMessageFormats) {
				Map<String, MessageFormat> codeMap = cachedMessageFormats.get(language);
				if(codeMap != null)
					codeMap.remove(code);
			}
		});
	}

}
