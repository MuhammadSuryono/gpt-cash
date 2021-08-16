package com.gpt.product.gpcash.bankapprovalmatrix.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixDetailModel;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixModel;
import com.gpt.product.gpcash.bankapprovalmatrix.repository.BankApprovalMatrixRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BankApprovalMatrixServiceImpl implements BankApprovalMatrixService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BankApprovalMatrixRepository bankApprovalMatrixRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BankApprovalMatrixModel> result = bankApprovalMatrixRepo.search(map, PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent(), false));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<BankApprovalMatrixModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BankApprovalMatrixModel model : list) {

			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(BankApprovalMatrixModel model, boolean isGetDetail) {
		IDMMenuModel menu = model.getMenu();

		Map<String, Object> map = new HashMap<>();
		map.put("id", model.getId());
		map.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
		map.put("menuName", menu.getName());
		map.put("noOfApprover", model.getNoOfApprover());

		if (isGetDetail) {
			if (model.getBankApprovalMatrixDetail() != null) {
				List<Map<String, Object>> listDetail = new ArrayList<>();
				List<BankApprovalMatrixDetailModel> detailModels = model.getBankApprovalMatrixDetail();
				for (BankApprovalMatrixDetailModel detailModel : detailModels) {
					if (detailModel != null) {
						Map<String, Object> detail = new HashMap<>();

						ApprovalLevelModel approvalLevel = detailModel.getApprovalLevel();

						detail.put("sequenceNo", detailModel.getSequenceNo());
						detail.put("noOfUser", detailModel.getNoOfUser());
						detail.put("approvalLevelCode", approvalLevel.getCode());
						detail.put("approvalLevelName", approvalLevel.getName());
						detail.put("branchOpt", detailModel.getBranchOption());

						listDetail.add(detail);
					}

				}
				map.put("bankApprovalMatrixDetailList", listDetail);
			}
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);

			List<Map<String, Object>> bankApprovalMatrixDetailList = (ArrayList<Map<String, Object>>) map
					.get("bankApprovalMatrixDetailList");

			int totalNoOfApprover = Integer.parseInt((String) map.get("noOfApprover"));
			int totalNoOfApproverDetail = 0;

			// check exist
			for (Map<String, Object> bankApprovalMatrixDetailMap : bankApprovalMatrixDetailList) {
				productRepo.isApprovalLevelValid((String) bankApprovalMatrixDetailMap.get("approvalLevelCode"));
				idmRepo.isIDMMenuValid((String) map.get(ApplicationConstants.STR_MENUCODE));

				totalNoOfApproverDetail = totalNoOfApproverDetail
						+ Integer.parseInt((String) bankApprovalMatrixDetailMap.get("noOfUser"));
			}

			if (totalNoOfApprover != totalNoOfApproverDetail) {
				throw new BusinessException("GPT-0100029");
			}

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// jika edit maka simpan pending task untuk old value. Old value
				// diambil dari DB
				BankApprovalMatrixModel bankApprovalMatrixOld = getExistingRecord((String) map.get("id"), true);
				vo.setJsonObjectOld(setModelToMap(bankApprovalMatrixOld, true));
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

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws Exception {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get("id"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("BankApprovalMatrixSC");

		return vo;
	}

	private BankApprovalMatrixModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		BankApprovalMatrixModel model = bankApprovalMatrixRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		}

		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

				String bankApprovalMatrixId = (String) map.get("id");

				bankApprovalMatrixRepo.deleteDetailByBankApprovalMatrixId(bankApprovalMatrixId);

				int totalNoOfApprover = Integer.parseInt((String) map.get("noOfApprover"));

				BankApprovalMatrixModel bankApprovalMatrix = bankApprovalMatrixRepo.findOne(bankApprovalMatrixId);
				bankApprovalMatrix.setNoOfApprover(totalNoOfApprover);

				IDMMenuModel menu = new IDMMenuModel();
				menu.setCode((String) map.get("approvalMatrixMenuCode"));
				bankApprovalMatrix.setMenu(menu);

				bankApprovalMatrix.setCreatedDate(DateUtils.getCurrentTimestamp());
				bankApprovalMatrix.setCreatedBy(vo.getCreatedBy());

				List<Map<String, String>> bankApprovalMatrixDetailList = (ArrayList<Map<String, String>>) map
						.get("bankApprovalMatrixDetailList");

				List<BankApprovalMatrixDetailModel> detailList = new ArrayList<>();

				int totalNoOfApproverDetail = 0;
				for (int i=0;i<bankApprovalMatrixDetailList.size();i++) {
					Map<String, String> detailMap = bankApprovalMatrixDetailList.get(i);
					BankApprovalMatrixDetailModel detail = new BankApprovalMatrixDetailModel();
					detail.setSequenceNo(i+1);
					detail.setNoOfUser(Integer.parseInt(detailMap.get("noOfUser")));

					totalNoOfApproverDetail = totalNoOfApproverDetail + Integer.parseInt(detailMap.get("noOfUser"));

					ApprovalLevelModel approvalLevel = new ApprovalLevelModel();
					approvalLevel.setCode((String) detailMap.get("approvalLevelCode"));
					detail.setApprovalLevel(approvalLevel);

					detail.setBranchOption((String) detailMap.get("branchOpt"));
					detail.setUpdatedDate(DateUtils.getCurrentTimestamp());
					detail.setUpdatedBy(vo.getCreatedBy());

					detail.setBankApprovalMatrix(bankApprovalMatrix);

					detailList.add(detail);
				}

				if (totalNoOfApprover != totalNoOfApproverDetail) {
					throw new BusinessException("GPT-0100029");
				}

				bankApprovalMatrix.setBankApprovalMatrixDetail(detailList);

				bankApprovalMatrixRepo.save(bankApprovalMatrix);
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

	@Override
	public Map<String, Object> getBankApprovalMatrixMenu(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<BankApprovalMatrixModel> result = bankApprovalMatrixRepo.findAll();
			List<Map<String, Object>> menuList = new ArrayList<>();
			for (BankApprovalMatrixModel model : result) {
				Map<String, Object> menuMap = new HashMap<>();
				IDMMenuModel menu = model.getMenu();
				menuMap.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
				menuMap.put("menuName", menu.getName());
				menuList.add(menuMap);
			}
			
			resultMap.put("result", menuList);

		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	@Override
	public List<BankApprovalMatrixDetailModel> getBankApprovalMatrixDetailForWorkflow(String menuCode)
			throws ApplicationException, BusinessException {
		try {
			List<BankApprovalMatrixDetailModel> result = bankApprovalMatrixRepo.findByMenuCode(menuCode);

			return result;

		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> searchBankApprovalMatrixDetail(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BankApprovalMatrixModel> result = bankApprovalMatrixRepo.search(map,
					null);
			resultMap.put("result", setModelToMap(result.getContent(), true));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getApprovaLevelforDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<ApprovalLevelModel> result = productRepo.getApprovalLevelRepo().findApprovalLevelAndSort();
			
			List<Map<String, Object>> levelList = new ArrayList<>();
			for (ApprovalLevelModel model : result) {
				Map<String, Object> levelMap = new HashMap<>();
				levelMap.put(ApplicationConstants.STR_CODE, model.getCode());
				levelMap.put(ApplicationConstants.STR_NAME, model.getName());
				levelList.add(levelMap);
			}
			
			resultMap.put("result", levelList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
}
