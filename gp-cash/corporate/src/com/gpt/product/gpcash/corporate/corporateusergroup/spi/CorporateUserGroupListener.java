package com.gpt.product.gpcash.corporate.corporateusergroup.spi;

import java.sql.Timestamp;
import java.util.Map;

public interface CorporateUserGroupListener {
	Map<String, Object> getTrxInfoFromPendingTask(String serviceCode, Timestamp instructionDate, String instructionMode, String userGroupId,String currencyMatrixCode)
			throws Exception;
	
}
