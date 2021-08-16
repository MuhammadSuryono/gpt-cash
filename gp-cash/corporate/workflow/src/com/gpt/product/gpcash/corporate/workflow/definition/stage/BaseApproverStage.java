package com.gpt.product.gpcash.corporate.workflow.definition.stage;

import java.util.List;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.corporate.workflow.CorporateStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;

public abstract class BaseApproverStage extends CorporateStage {
	
	protected abstract void createAndAssignTasksToUsers(CorporateProcessInstance pi, int noOfUser, int currApprLv, List<Object> approvalData) throws BusinessException, ApplicationException;
	
	@Override
	public String getId() {
		return ApplicationConstants.WF_STAGE_APPROVE;
	}
	
	protected boolean isSkipToNextStage(CorporateProcessInstance pi) {
		return false;
	}
	
	@Override
	public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		try {
			List<List<Object>> levels = pi.getListOfApprovalMatrix();
			for(int i=0;i<levels.size();i++) {
				List<Object> level = levels.get(i);
				int noOfUser = (Integer)level.get(1);
				if(noOfUser == 0) {
					continue;
				}
				
				createAndAssignTasksToUsers(pi, noOfUser, i, level);
				
				// stay on this stage
				return null;
			}
	
			// no task has been created, continue the flow to APPROVED stage
			pi.setStatus(Status.APPROVED);
			return getNextStage(Status.APPROVED.name());
		} catch(BusinessException e) {
			throw e;
		} catch(ApplicationException e) {
			throw e;
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public CorporateStage execute(CorporateTaskInstance ti) throws BusinessException, ApplicationException {
		Status result = ti.getStatus();

		assert(result !=null && (result.equals(Status.APPROVED) || result.equals(Status.DECLINED) || result.equals(Status.REJECTED))) : "TaskInstance: " + ti.getId() + ", status must be set to APPROVED, DECLINED or REJECTED";

		CorporateProcessInstance pi = ti.getProcessInstance();
		if(result.equals(Status.REJECTED) || result.equals(Status.DECLINED))  {
			cancelUnfinishedTasks(pi);
			return getNextStage(result.name());
		} else {
			try {
		        boolean approved = false;
		        
		        if(isSkipToNextStage(pi)) {
		        		cancelUnfinishedTasks(pi);
		        		approved = true;
		        } else {
				    //get current approval level
				    int currApprLv = pi.getCurrApprLv();
				    int currApprLvCount = pi.getCurrApprLvCount() + 1;
				    int currApprLvCountRequired = 0;

				    pi.setCurrApprLvCount(currApprLvCount);
				    
			        List<List<Object>> approvalMatrix = pi.getListOfApprovalMatrix();
					currApprLvCountRequired = (Integer)approvalMatrix.get(currApprLv).get(1);
			        
				    	if(currApprLvCount >= currApprLvCountRequired) {
				    		// cancel any remaining tasks
				    		cancelUnfinishedTasks(pi);
			            if(currApprLv == approvalMatrix.size() - 1) {
			                //all level has approved
			            		approved = true;
			            } else {
							//next level approval
							for(currApprLv++;currApprLv < approvalMatrix.size();currApprLv++) {
								List<Object> approvalData = approvalMatrix.get(currApprLv);
								int noOfUser = (Integer)approvalData.get(1);
								if(noOfUser == 0) {
									if(currApprLv == approvalMatrix.size() - 1) {
							            //this is the final aproval level but no approval is required
										approved = true;
									}
									continue;
								}
								
								createAndAssignTasksToUsers(pi, noOfUser, currApprLv, approvalData);
								
								break;
							}
			            }
				    	} 
		        }
				    
			    	if(approved) {
			    		return getNextStage(result.name());
			    	}
			} catch(BusinessException e) {
				throw e;
			} catch(ApplicationException e) {
				throw e;
			} catch(Exception e) {
				throw new ApplicationException(e);
			}
		}
		
		// stay on this stage
		return null;
	}
}
