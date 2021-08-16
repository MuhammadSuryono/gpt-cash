package com.gpt.component.idm.web.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.platform.cash.utils.Helper;
import com.hazelcast.core.HazelcastInstance;

@Component("heartbeat")
public class HeartBeatHandler {

	private Map<String, String> heartBeatHolder;
	
	@Autowired
	public void init(HazelcastInstance hzInstance) {
		heartBeatHolder = hzInstance.getMap("heartBeat");
	}
	
	public String getHeartBeat(String code) {
		String rand = Helper.getRandomPassword(16);
		
		if(code != null) {
			heartBeatHolder.put(code, rand);
		}
		
		return rand;
	}

	public String getRecordedHeartBeat(String code) {
		String key = heartBeatHolder.remove(code);
		if(key == null)
			key = Helper.getRandomPassword(16); // if not found just ignore, generate a new random key and proceed. This will fail aniway.
		return key;
	}

}
