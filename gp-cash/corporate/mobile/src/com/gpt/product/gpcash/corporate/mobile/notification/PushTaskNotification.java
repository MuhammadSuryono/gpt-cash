package com.gpt.product.gpcash.corporate.mobile.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.gpt.component.common.invoker.spi.ICallableInvoker;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.mobile.model.MobileDevice;
import com.gpt.product.gpcash.corporate.mobile.repository.MobileDeviceRepository;
import com.gpt.product.gpcash.corporate.notification.TaskNotification;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Component
public class PushTaskNotification implements TaskNotification {
	public static final Logger logger = LoggerFactory.getLogger(PushTaskNotification.class);
	
	@Autowired
	private MobileDeviceRepository repo;
	
	@Autowired
	private ICallableInvoker invoker;

	private RestTemplate rest;
	private HttpHeaders headers;
	
	@Autowired
	private void setMessageConverter(MappingJackson2HttpMessageConverter converter, @Value("${gpcash.mobile.fcm.server.key}") String serverKey) {
		rest = new RestTemplate(Arrays.asList(converter));
		headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "key=" + serverKey);
	}
	
	@Override
	public void sendNotification(CorporateUserPendingTaskModel pendingTask, List<CorporateUserModel> users)
			throws Exception {
		invoker.invokeAndForget(new Executor(pendingTask, users), 30000);
	}
	
//	public static void main(String[] args) throws Exception {
//		RestTemplate rest = new RestTemplate(Arrays.asList(new MappingJackson2HttpMessageConverter(new ObjectMapper())));
//		HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//		headers.set("Authorization", "key=AIzaSyBrL3EfzF7qsPFTQ9S2sw9X8SR9pbVBBMs");
//		
//		String desc = "Overbooking - Pending Release";
//		
//		Map<String, Object> params = new HashMap<>();
//		Map<String, Object> data = new HashMap<>();
//		params.put("data", data);
//		
//		Map<String, Object> notification = new HashMap<>();
//		
//		notification.put("title", "Task Notification");
//		notification.put("body", desc);
//		notification.put("show_in_foreground", true);
//		notification.put("priority", "high");
//		notification.put("click_action", "ACTION");
//		notification.put("sound", "default");
//		notification.put("opened_from_tray", true);
//		
//		Map<String, Object> extra = new HashMap<>();
//		extra.put("pendingTaskId", "123");
//		extra.put("menuCode", "MNU_GPCASH_F_PENDING_TASK");
//		
//		notification.put("data", extra);
//		
//		String id = "dapprls-task";
//		notification.put("id", id);
//		notification.put("group", id);
//		notification.put("sub_text", "Maximillian Superman");
//		
//		params.put("to", "1bFBZ3_clTY4kfcc3FUvE8lvatnsdDxtwQzYgZp2KLKGBTMnKUH-TyNmG-p4Qs05-g_6KgORllXtCy_wL0sn2D_MKjjzGFiQET8qckrOb_qoFR1k4htTGJIr5--al-L_4WlTcsep");
//
//		data.put("custom_notification", notification);
//		
//		logger.error("====>>>>\n" + new JSONObject(params).toString(4));
//		
//		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
//		ResponseEntity<Map> response = rest.postForEntity("https://fcm.googleapis.com/fcm/send", entity, Map.class);			
//		if(response.getStatusCode() != HttpStatus.OK) {
//			System.err.println("Error sending push notification for user: dapprls\nHttp Response: " + response.getStatusCode() + "\nReason: " + response.getBody().toString());
//			logger.error("Error sending push notification for user: {}\nHttp Response: {}\nReason: {}", "dapprls", response.getStatusCode(), response.getBody().toString());
//		} else {
//			Map body = response.getBody();
//			Number failure = (Number)body.get("failure");
//			if(failure!=null && failure.intValue() == 1) {
//				String error = (String)((ArrayList<Map>)body.get("results")).get(0).get("error");
//				System.err.println("Error sending push notification for user: dapprls\nHttp Response: " + response.getStatusCode() + "\nReason: " + error);
//				logger.error("Error sending push notification for user: {}\nHttp Response: {}\nReason: {}", "dapprls", response.getStatusCode(), error);
//			}
//		}
//	}
	
	class Executor implements Callable<Object> {
		CorporateUserPendingTaskModel pendingTask;
		List<CorporateUserModel> users;
		
		Executor(CorporateUserPendingTaskModel pendingTask, List<CorporateUserModel> users) {
			this.pendingTask = pendingTask;
			this.users = users;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object call() throws Exception {
			try {
				List<MobileDevice> devices = repo.findPushIdByCorpUserCode(users);
				if(devices == null || devices.isEmpty())
					return null;
				
				ServiceModel serviceModel = pendingTask.getTransactionService();
				String desc = serviceModel.getName();
				
//				if(pendingTask.getTrxStatus() != null) {
//					desc += " - " + pendingTask.getTrxStatus().name();
//				}
				
				Map<String, Object> params = new HashMap<>();
				Map<String, Object> data = new HashMap<>();
				params.put("data", data);
				
				Map<String, Object> notification = new HashMap<>();
				
				notification.put("title", "Task Notification");
				notification.put("body", desc);
				notification.put("show_in_foreground", true);
				notification.put("priority", "high");
				notification.put("large_icon", "ic_task");
				notification.put("click_action", "ACTION");
				notification.put("sound", "default");
				notification.put("opened_from_tray", true);
				
				Map<String, Object> extra = new HashMap<>();
				extra.put("pendingTaskId", pendingTask.getId());
//				extra.put("menuCode", pendingTask.getMenu().getCode());
				
				notification.put("data", extra);
				
				for(MobileDevice device : devices) {
					CorporateUserModel user = device.getUser();
					
					String id = user.getUserId() + "-task";
					notification.put("id", id);
					notification.put("group", id);
					notification.put("sub_text", user.getUser().getName());
					
					params.put("to", device.getPushNotificationId());
	
					if(device.getPlatform().equals("ios")) {
						//TODO: stil don't know what to provide
						data.remove("custom_notification");
					} else {
						data.put("custom_notification", notification);
					}
					
					HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
					ResponseEntity<Map> response = rest.postForEntity("https://fcm.googleapis.com/fcm/send", entity, Map.class);			
					if(response.getStatusCode() != HttpStatus.OK) {
						logger.error("Error sending push notification for user: {}\nHttp Response: {}\nReason: {}", user.getUserId(), response.getStatusCode(), response.getBody().toString());
					} else {
						Map body = response.getBody();
						Number failure = (Number)body.get("failure");
						if(failure!=null && failure.intValue() == 1) {
							String error = (String)((ArrayList<Map>)body.get("results")).get(0).get("error");
							logger.error("Error sending push notification for user: {}\nHttp Response: {}\nReason: {}", user.getUserId(), response.getStatusCode(), error);
						}
					}
				}
			}catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
			
			return null;
		}
	}
}
