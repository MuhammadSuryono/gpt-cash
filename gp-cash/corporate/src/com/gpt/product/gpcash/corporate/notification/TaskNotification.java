package com.gpt.product.gpcash.corporate.notification;

import java.util.List;

import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;

public interface TaskNotification {

	void sendNotification(CorporateUserPendingTaskModel pendingTask, List<CorporateUserModel> users) throws Exception;
	
}
