package com.gpt.product.gpcash.calendar.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.calendar.CalendarConstants;
import com.gpt.component.calendar.model.CalendarModel;
import com.gpt.component.calendar.repository.CalendarRepository;
import com.gpt.component.calendar.services.CalendarService;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class ComCalendarServiceImpl implements ComCalendarService {
	
	@Autowired
	private CalendarService calendarService;
	
	@Autowired
	private CalendarRepository calendarRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return calendarService.search(map);
	}
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			
			if (map.get("holidayDateFrom") !=null) {
				
				Date holidayDateFrom = (Date) map.get("holidayDateFrom");
				Date holidayDateTo = (Date) map.get("holidayDateTo");
				
				if(holidayDateFrom.compareTo(holidayDateTo) > 0){
					throw new BusinessException("GPT-0100127");
				}
			}
			
			/*if (map.get("type").equals(CalendarConstants.TYPE_CURRENCY)) {
				if (!ValueUtils.hasValue(map.get("currencyCode"))) {
					throw new BusinessException("Currency is mandatory");
				}
			}*/
			
			PendingTaskVO vo = setPendingTaskVO(map); 
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
			} else {
				throw new BusinessException("GPT-0100003");
			}

			resultMap.putAll(pendingTaskService.savePendingTask(vo)); 
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo()); 

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("ComCalendarSC");

		return vo;
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				Date holidayDateFrom = Helper.DATE_FORMATTER.parse((String) map.get("holidayDateFrom"));
				Date holidayDateTo = Helper.DATE_FORMATTER.parse((String) map.get("holidayDateTo"));
				
				map.put("holidayDateFrom", holidayDateFrom);
				map.put("holidayDateTo", holidayDateTo);
				
				if(holidayDateFrom.compareTo(holidayDateTo) > 0){
					throw new BusinessException("GPT-0100127");
				}
				calendarService.submitRange(map);
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				//delete all record by holidayDate
				Date holidayDate = Helper.DATE_FORMATTER.parse((String) map.get("holidayDate"));
				map.put("holidayDate", holidayDate);
				calendarService.submitDelete(map);
				
				//add
				List<Map<String, Object>> holidayList = (List<Map<String,Object>>) map.get("holidayList");
				List<CalendarModel> holidays = new ArrayList<>();
				for(Map<String, Object> holidayMap: holidayList) {
					String dscp = (String) holidayMap.get("dscp");
					String type = (String) holidayMap.get("type");
					String currencyCode = ValueUtils.getValue((String)holidayMap.get("currencyCode"));
					String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERID);

					CalendarModel calendar = new CalendarModel();
					calendar.setHolidayDate(new java.sql.Date(holidayDate.getTime()));
					calendar.setDscp(dscp);
					calendar.setType(type);
					calendar.setCurrency(currencyCode);
					calendar.setCreatedDate(DateUtils.getCurrentTimestamp());
					calendar.setCreatedBy(createdBy);
					
					holidays.add(calendar);
				}
				calendarService.submitList(holidays);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				Date holidayDate = Helper.DATE_FORMATTER.parse((String) map.get("holidayDate"));
				map.put("holidayDate", holidayDate);
				calendarService.submitDelete(map);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}
	
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}
}
