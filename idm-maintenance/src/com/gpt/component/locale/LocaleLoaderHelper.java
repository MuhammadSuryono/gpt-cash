package com.gpt.component.locale;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component
public class LocaleLoaderHelper implements ApplicationListener<ContextRefreshedEvent> {
	private static final String MAP_PREFIX = "gpcash.localization.";
	private static final String LOCK_ = "lock.localization";
	
	private static LocaleLoaderHelper instance;
	
	@Autowired
	private ErrorMappingRepository repo;
	
	@Autowired
	private HazelcastInstance hzInstance;
	
	@Value("${gpcash.messages.preload}")
	private boolean preload;
	
	public LocaleLoaderHelper() {
		instance = this;
	}
	
	public static LocaleLoaderHelper getInstance() {
		return instance;
	}

	public String load(String locale, String key) {
		ErrorMappingModel model = repo.findOne(key);
		if(model != null) {
			String message;
			if(locale.startsWith("en")) {
				message = model.getName();
			} else {
				message = model.getNameId();
			}
			
			if(model.getErrorFlag() != null && ApplicationConstants.YES.equals(model.getErrorFlag())) {
				message = key + ": " + message;
			}
			
			return message;
		}
				
		return key;
	}

	public Map<String, String> loadAll(String locale, Collection<String> keys) {
		List<Object[]> all;
		if(locale.startsWith("en")) {
			all = repo.findAllEnMessages(keys);
		} else {
			all = repo.findAllIdMessages(keys);
		}
		
		Map<String, String> messages = new HashMap<>(all.size(), 1);
		for(Object[] message : all) {
			if(message[2] != null && ApplicationConstants.YES.equals(message[2]))
				messages.put((String)message[0], (String)message[0] + ": " + (String)message[1]);
			else
				messages.put((String)message[0], (String)message[1]);
		}
		return messages;
	}

	public Iterable<String> loadAllKeys(String locale) {
//		if(locale == null || !preload) {
			return Collections.emptyList();
//		}
//		return repo.findAllCodes();
	}
	
	public static String getMapName(Locale locale) {
		return MAP_PREFIX + locale.getLanguage();
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// initialize locale maps
		if(preload) {
			Lock lock = hzInstance.getLock(LOCK_);
			lock.lock();
			try {
				IMap<Object, Object> enResources = hzInstance.getMap(getMapName(new Locale("en")));
				if(enResources.size() == 0) {
					IMap<Object, Object> idResources = hzInstance.getMap(getMapName(new Locale("id")));
					List<ErrorMappingModel> models = repo.findAll();
					for(ErrorMappingModel model : models) {
						String code = model.getCode();
						if(model.getErrorFlag() != null && ApplicationConstants.YES.equals(model.getErrorFlag())) {
							enResources.set(code, code + ": " + model.getName());
							idResources.set(code, code + ": " + model.getNameId());
						} else {
							enResources.set(code, model.getName());
							idResources.set(code, model.getNameId());
						}
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}
		
}
