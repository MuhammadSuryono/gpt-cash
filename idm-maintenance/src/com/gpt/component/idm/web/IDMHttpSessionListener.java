package com.gpt.component.idm.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;

@Configuration
public class IDMHttpSessionListener implements HttpSessionListener {
	private static final Logger logger = LoggerFactory.getLogger(IDMHttpSessionListener.class);
	
	private String serverUUID;
	private PartitionService partitionService;
	private boolean usingHazelcastStoreType;
	
	@Autowired
	private ISpringBeanInvoker invoker;
	
	@Autowired
	protected void setHazelcastInstance(HazelcastInstance hzInstance) {
		serverUUID = hzInstance.getCluster().getLocalMember().getUuid();
		partitionService = hzInstance.getPartitionService();
	}
	
	@Autowired
	protected void setSessionStoreType(@Value("${spring.session.store-type}") String storeType) {
		usingHazelcastStoreType = "hazelcast".equalsIgnoreCase(storeType);
		
		if(usingHazelcastStoreType) {
			// need to check if spring session is in the classpath
			try {
				Class.forName("org.springframework.session.hazelcast.HazelcastSessionRepository");
			}catch(ClassNotFoundException e) {
				usingHazelcastStoreType = false;
			}
		}
	}

	private boolean shouldHandleSessionEvent(HttpSessionEvent se) {
		boolean handleSession = false;
		
		if(usingHazelcastStoreType) {
			Partition partition = partitionService.getPartition(se.getSession().getId());
			Member ownerMember = partition.getOwner();
			
			if(ownerMember.getUuid().equals(serverUUID)) {
				handleSession = true;
			}
		} else {
			handleSession = true;
		}
		
		return handleSession;
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
//		if(shouldHandleSessionEvent(se)) {
//			System.out.println("========>>>> sessionCreated [" + se.getSession().getId() + "]" + new Date());
//		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		if(shouldHandleSessionEvent(se)) {
			HttpSession session = se.getSession();
			
			if(logger.isDebugEnabled())
				logger.debug("========>>>> session is destroyed [{}]", session.getId());
			
			/**
			 * REMEMBER: This method is called asynchronously, so if you need to update database, please ensure there is no race condition,
			 * otherwise you might end up updating the login flag just after the login process updating it. 
			 */
			Object loginHistoryId = session.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID);
			if(loginHistoryId != null) {
				String loginUserCode = (String) session.getAttribute(ApplicationConstants.LOGIN_USERCODE);
				
				Map<String, Object> param = new HashMap<>(3,1);
				
				if(loginUserCode != null) {
					param.put(ApplicationConstants.LOGIN_USERID, loginUserCode);
				} else {
					param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
				}
				param.put(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryId);
				param.put(ApplicationConstants.LOGIN_DATE, session.getAttribute(ApplicationConstants.LOGIN_DATE));
	
				invoker.invokeAndForget("IDMLoginSC", "logout", new Object[] {param}, 10000);
			}
		}
	}
}
