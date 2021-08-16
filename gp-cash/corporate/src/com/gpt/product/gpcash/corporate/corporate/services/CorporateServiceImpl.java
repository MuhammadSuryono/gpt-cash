package com.gpt.product.gpcash.corporate.corporate.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.BusinessUnitModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.HandlingOfficerModel;
import com.gpt.component.maintenance.parametermt.model.IndustrySegmentModel;
import com.gpt.component.maintenance.parametermt.model.PostCodeModel;
import com.gpt.component.maintenance.parametermt.model.StateModel;
import com.gpt.component.maintenance.parametermt.model.SubstateModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.corporate.corporate.VAStatus;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateContactModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;
import com.gpt.product.gpcash.corporate.corporatelimit.repository.CorporateLimitRepository;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.repository.CorporateUserGroupRepository;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.repository.TokenUserRepository;
import com.gpt.product.gpcash.corporate.token.tokenuser.services.TokenUserService;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateServiceImpl implements CorporateService {

	@Autowired
	private CorporateRepository corporateRepo;

	@Autowired
	private CorporateUserGroupRepository corporateUserGroupRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private IDMUserService idmUserService;

	@Autowired
	private CorporateUserRepository corporateUserRepo;

	@Autowired
	private CorporateLimitRepository corporateLimitRepo;

	@Autowired
	private CorporateChargeRepository corporateChargeRepo;

	@Autowired
	private TokenUserService tokenUserService;

	@Autowired
	private TokenUserRepository tokenUserRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private EAIEngine eaiAdapter;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CorporateModel> result = corporateRepo.search(map, PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else if (ApplicationConstants.WF_ACTION_DETAIL.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CorporateModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CorporateModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.CORP_ID, model.getId());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("cifId", ValueUtils.getValue(model.getHostCifId()));
		map.put("inactiveFlag", ValueUtils.getValue(model.getInactiveFlag()));
		
		//additional for va
		map.put("vaStatus", model.getVaStatus() == null ? VAStatus.Unregistered.name() : model.getVaStatus());
		map.put("vaProductCode", ValueUtils.getValue(model.getVaProductCode()));
		//---------------------------
		
		if (isGetDetail) {
			map.put("address1", ValueUtils.getValue(model.getAddress1()));
			map.put("address2", ValueUtils.getValue(model.getAddress2()));
			map.put("address3", ValueUtils.getValue(model.getAddress3()));

			if (model.getPostcode() != null) {
				PostCodeModel postcode = maintenanceRepo.getPostCodeRepo().findOne(model.getPostcode().getCode());
				map.put("postcode", ValueUtils.getValue(postcode.getCode()));
				map.put("postcodeName", ValueUtils.getValue(postcode.getName()));
			}

			if (model.getCity() != null) {
				CityModel city = maintenanceRepo.getCityRepo().findOne(model.getCity().getCode());
				map.put("cityCode", ValueUtils.getValue(city.getCode()));
				map.put("cityName", ValueUtils.getValue(city.getName()));
			}

			if (model.getSubstate() != null) {
				SubstateModel substate = maintenanceRepo.getSubstateRepo().findOne(model.getSubstate().getCode());
				map.put("substateCode", ValueUtils.getValue(substate.getCode()));
				map.put("substateName", ValueUtils.getValue(substate.getName()));
			}

			if (model.getState() != null) {
				StateModel state = maintenanceRepo.getStateRepo().findOne(model.getState().getCode());
				map.put("stateCode", ValueUtils.getValue(state.getCode()));
				map.put("stateName", ValueUtils.getValue(state.getName()));
			}

			if (model.getCountry() != null) {
				CountryModel country = maintenanceRepo.getCountryRepo().findOne(model.getCountry().getCode());
				map.put("countryCode", ValueUtils.getValue(country.getCode()));
				map.put("countryName", ValueUtils.getValue(country.getName()));
			}

			map.put("email1", ValueUtils.getValue(model.getEmail1()));
			map.put("email2", ValueUtils.getValue(model.getEmail2()));
			map.put("phoneNo", ValueUtils.getValue(model.getPhoneNo()));
			map.put("extNo", ValueUtils.getValue(model.getExtNo()));
			map.put("faxNo", ValueUtils.getValue(model.getFaxNo()));
			map.put("specialChargeFlag", ValueUtils.getValue(model.getSpecialChargeFlag()));
			map.put("specialLimitFlag", ValueUtils.getValue(model.getSpecialLimitFlag()));
			map.put("outsourceAdminFlag", ValueUtils.getValue(model.getOutsourceAdminFlag()));

			if (model.getBranch() != null) {
				BranchModel branch = maintenanceRepo.getBranchRepo().findOne(model.getBranch().getCode());
				map.put("branchCode", ValueUtils.getValue(branch.getCode()));
				map.put("branchName", ValueUtils.getValue(branch.getName()));
			}

			if (model.getServicePackage() != null) {
				ServicePackageModel servicePackage = productRepo.getServicePackageRepo()
						.findOne(model.getServicePackage().getCode());
				map.put("servicePackageCode", ValueUtils.getValue(servicePackage.getCode()));
				map.put("servicePackageName", ValueUtils.getValue(servicePackage.getName()));
			}

			if (model.getIndustrySegment() != null) {
				IndustrySegmentModel industrySegment = maintenanceRepo.getIndustrySegmentRepo()
						.findOne(model.getIndustrySegment().getCode());
				map.put("industrySegmentCode", ValueUtils.getValue(industrySegment.getCode()));
				map.put("industrySegmentName", ValueUtils.getValue(industrySegment.getName()));
			}

			if (model.getBusinessUnit() != null) {
				BusinessUnitModel businessUnit = maintenanceRepo.getBusinessUnitRepo()
						.findOne(model.getBusinessUnit().getCode());
				map.put("businessUnitCode", ValueUtils.getValue(businessUnit.getCode()));
				map.put("businessUnitName", ValueUtils.getValue(businessUnit.getName()));
			}

			map.put("taxIdNo", ValueUtils.getValue(model.getTaxIdNo()));

			if (model.getHandlingOfficer() != null) {
				HandlingOfficerModel handlingOfficer = maintenanceRepo.getHandlingOfficerRepo()
						.findOne(model.getHandlingOfficer().getCode());
				map.put("handlingOfficerCode", ValueUtils.getValue(handlingOfficer.getCode()));
				map.put("handlingOfficerName", ValueUtils.getValue(handlingOfficer.getName()));
			} else {
				//untuk ui agar tidak undefined untuk yg tidak mandatory
				map.put("handlingOfficerCode", ApplicationConstants.EMPTY_STRING);
				map.put("handlingOfficerName", ApplicationConstants.EMPTY_STRING);
			}

			map.put("maxCorporateUser", ValueUtils.getValue(model.getMaxCorporateUser()));
			map.put("lldIsResidence", ValueUtils.getValue(model.getLldIsResidence()));
			map.put("lldIsCitizen", ValueUtils.getValue(model.getLldIsCitizen()));
			
			map.put("lldCategory", model.getLldCategory());
			
			if(model.getLldCategory() != null) {
				BeneficiaryTypeModel beneType = maintenanceRepo.isBeneficiaryTypeValid(model.getLldCategory());
				map.put("lldCategoryName", beneType.getName());
			}

			if (model.getCitizenCountry() != null) {
				CountryModel citizenCountry = maintenanceRepo.getCountryRepo()
						.findOne(model.getCitizenCountry().getCode());
				map.put("citizenCountryCode", ValueUtils.getValue(citizenCountry.getCode()));
				map.put("citizenCountryName", ValueUtils.getValue(citizenCountry.getName()));
			}

			if (model.getResidenceCountry() != null) {
				CountryModel residenceCountry = maintenanceRepo.getCountryRepo()
						.findOne(model.getResidenceCountry().getCode());
				map.put("residenceCountryCode", ValueUtils.getValue(residenceCountry.getCode()));
				map.put("residenceCountryName", ValueUtils.getValue(residenceCountry.getName()));
			}

			if (model.getCorporateContact() != null) {
				List<CorporateContactModel> contactList = model.getCorporateContact();

				List<Map<String, Object>> contactListMap = new ArrayList<>();
				for (CorporateContactModel contact : contactList) {
					Map<String, Object> contactMap = new HashMap<>();
					contactMap.put("name", ValueUtils.getValue(contact.getName()));
					contactMap.put("phoneNo", ValueUtils.getValue(contact.getPhoneNo()));
					contactMap.put("mobileNo", ValueUtils.getValue(contact.getMobileNo()));
					contactMap.put("email", ValueUtils.getValue(contact.getEmail()));
					contactMap.put("faxNo", ValueUtils.getValue(contact.getFaxNo()));
					contactListMap.add(contactMap);
				}
				map.put("contactList", contactListMap);
			}

			// get admin list
			List<CorporateUserModel> adminList = corporateUserRepo.findAdminUser(model.getId());

			List<Map<String, Object>> adminListMap = new ArrayList<>();
			for (CorporateUserModel adminUser : adminList) {
				Map<String, Object> adminMap = new HashMap<>();

				IDMUserModel idmUser = adminUser.getUser();
				adminMap.put("userId", ValueUtils.getValue(adminUser.getUserId()));
				adminMap.put("name", ValueUtils.getValue(idmUser.getName()));
				adminMap.put("mobileNo", ValueUtils.getValue(adminUser.getMobilePhoneNo()));
				adminMap.put("email", ValueUtils.getValue(idmUser.getEmail()));
				adminMap.put("status", ValueUtils.getValue(idmUser.getStatus()));

				TokenUserModel tokenUserModel = tokenUserRepo.findByAssignedUserCode(idmUser.getCode());

				if (tokenUserModel != null) {
					adminMap.put("tokenNo", tokenUserModel.getTokenNo());
					adminMap.put("tokenType", tokenUserModel.getTokenType().getCode());
				}

				WorkflowRoleModel wfRole = adminUser.getApprovalMap().getWorkflowRole();

				if ("CRP_ADM_MAKER".equals(wfRole.getCode())) {
					adminMap.put("role", ValueUtils.getValue("maker"));
				} else if ("CRP_ADM_CHECKER".equals(wfRole.getCode())) {
					adminMap.put("role", ValueUtils.getValue("checker"));
				}

				adminListMap.add(adminMap);
			}
			map.put("adminList", adminListMap);
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		}

		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.CORP_ID));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				CorporateModel corporateOld = getExistingRecord((String) map.get(ApplicationConstants.CORP_ID), true);
				vo.setJsonObjectOld(setModelToMap(corporateOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.CORP_ID), true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_STATUS")) {
				vo.setAction("UPDATE_STATUS");

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.CORP_ID), true);
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

	@SuppressWarnings("unchecked")
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			if (ValueUtils.hasValue(map.get("postcode")))
				maintenanceRepo.isPostCodeValid((String) map.get("postcode"));

			if (ValueUtils.hasValue(map.get("substateCode")))
				maintenanceRepo.isSubstateValid((String) map.get("substateCode"));

			if (ValueUtils.hasValue(map.get("stateCode")))
				maintenanceRepo.isStateValid((String) map.get("stateCode"));

			if (ValueUtils.hasValue(map.get("countryCode")))
				maintenanceRepo.isCountryValid((String) map.get("countryCode"));

			if (ValueUtils.hasValue(map.get("branchCode")))
				maintenanceRepo.isBranchValid((String) map.get("branchCode"));

			if (ValueUtils.hasValue(map.get("servicePackageCode")))
				productRepo.isServicePackageValid((String) map.get("servicePackageCode"));

			if (ValueUtils.hasValue(map.get("industrySegmentCode")))
				maintenanceRepo.isIndustrySegmentValid((String) map.get("industrySegmentCode"));

			if (ValueUtils.hasValue(map.get("businessUnitCode")))
				maintenanceRepo.isBusinessUnitValid((String) map.get("businessUnitCode"));

			if (ValueUtils.hasValue(map.get("handlingOfficerCode")))
				maintenanceRepo.isHandlingOfficerValid((String) map.get("handlingOfficerCode"));

			if (ValueUtils.hasValue(map.get("citizenCountryCode")))
				maintenanceRepo.isCountryValid((String) map.get("citizenCountryCode"));

			if (ValueUtils.hasValue(map.get("residenceCountryCode")))
				maintenanceRepo.isCountryValid((String) map.get("residenceCountryCode"));
			
			if(map.get("adminList") != null) {
				List<Map<String, Object>> adminList = (List<Map<String, Object>>)map.get("adminList");
				Map<String, Object> adminMaker = adminList.get(0);
				Map<String, Object> adminApprover = adminList.get(1);
				
				if(adminMaker.get("userId") != null && adminApprover.get("userId") != null) {
					if(((String) adminMaker.get("userId")).trim().equals((String) adminApprover.get("userId"))) {
						throw new BusinessException("GPT-0100229");
					}
				}
				
				if(adminMaker.get("email") != null && adminApprover.get("email") != null) {
					if(((String) adminMaker.get("email")).trim().equals((String) adminApprover.get("email"))) {
						throw new BusinessException("GPT-0100145");
					}
				}
				
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	public CorporateModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		CorporateModel model = corporateRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}

		return model;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CORP_ID));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	private CorporateModel setMapToModel(Map<String, Object> map) {
		CorporateModel corporate = new CorporateModel();
		corporate.setId((String) map.get(ApplicationConstants.CORP_ID));
		corporate.setName((String) map.get(ApplicationConstants.STR_NAME));
		corporate.setHostCifId((String) map.get("cifId"));
		corporate.setAddress1((String) map.get("address1"));
		corporate.setAddress2((String) map.get("address2"));
		corporate.setAddress3((String) map.get("address3"));

		PostCodeModel postCode = new PostCodeModel();
		postCode.setCode((String) map.get("postcode"));
		corporate.setPostcode(postCode);

		CityModel city = new CityModel();
		city.setCode((String) map.get("cityCode"));
		corporate.setCity(city);

		SubstateModel substate = new SubstateModel();
		substate.setCode((String) map.get("substateCode"));
		corporate.setSubstate(substate);

		StateModel state = new StateModel();
		state.setCode((String) map.get("stateCode"));
		corporate.setState(state);

		CountryModel country = new CountryModel();
		country.setCode((String) map.get("countryCode"));
		corporate.setCountry(country);

		corporate.setEmail1((String) map.get("email1"));
		corporate.setEmail2((String) map.get("email2"));
		corporate.setPhoneNo((String) map.get("phoneNo"));
		corporate.setExtNo((String) map.get("extNo"));
		corporate.setFaxNo((String) map.get("faxNo"));
		corporate.setSpecialChargeFlag((String) map.get("specialChargeFlag"));
		corporate.setSpecialLimitFlag((String) map.get("specialLimitFlag"));
		corporate.setOutsourceAdminFlag((String) map.get("outsourceAdminFlag"));
		
		if(ApplicationConstants.YES.equals(corporate.getOutsourceAdminFlag()))
			corporate.setTokenAuthenticationFlag(ApplicationConstants.NO);
		else {
			corporate.setTokenAuthenticationFlag(ApplicationConstants.YES);
		}
		
		BranchModel branch = new BranchModel();
		branch.setCode((String) map.get("branchCode"));
		corporate.setBranch(branch);

		ServicePackageModel servicePackage = productRepo.getServicePackageRepo()
				.findOne((String) map.get("servicePackageCode"));
		corporate.setServicePackage(servicePackage);

		IDMRoleModel role = new IDMRoleModel();
		role.setCode(servicePackage.getMenuPackage().getRole().getCode());
		corporate.setRole(role);
		
		if(ValueUtils.hasValue(map.get("industrySegmentCode") )) {
			IndustrySegmentModel industrySegment = new IndustrySegmentModel();
			industrySegment.setCode((String) map.get("industrySegmentCode"));
			corporate.setIndustrySegment(industrySegment);
		} else {
			corporate.setIndustrySegment(null);
		}
		
		if(ValueUtils.hasValue(map.get("businessUnitCode") )) {
			BusinessUnitModel businessUnit = new BusinessUnitModel();
			businessUnit.setCode((String) map.get("businessUnitCode"));
			corporate.setBusinessUnit(businessUnit);
		} else {
			corporate.setBusinessUnit(null);
		}

		corporate.setTaxIdNo((String) map.get("taxIdNo"));

		if(ValueUtils.hasValue(map.get("handlingOfficerCode") )) {
			HandlingOfficerModel handlingOfficer = new HandlingOfficerModel();
			handlingOfficer.setCode((String) map.get("handlingOfficerCode"));
			corporate.setHandlingOfficer(handlingOfficer);
		} else {
			corporate.setHandlingOfficer(null);
		}

		corporate.setMaxCorporateUser(Integer.parseInt((String) map.get("maxCorporateUser")));

		corporate.setLldIsResidence((String) map.get("lldIsResidence"));
		corporate.setLldIsCitizen((String) map.get("lldIsCitizen"));

		CountryModel citizenCountry = new CountryModel();
		citizenCountry.setCode((String) map.get("citizenCountryCode"));
		corporate.setCitizenCountry(citizenCountry);

		CountryModel residenceCountry = new CountryModel();
		residenceCountry.setCode((String) map.get("residenceCountryCode"));
		corporate.setResidenceCountry(citizenCountry);
		
		corporate.setLldCategory((String) map.get("lldCategory"));

		if (ValueUtils.hasValue(map.get("contactList"))) {
			List<CorporateContactModel> contactListModel = new ArrayList<>();

			List<Map<String, Object>> contactListMap = (ArrayList<Map<String, Object>>) map.get("contactList");
			for (Map<String, Object> contactMap : contactListMap) {
				CorporateContactModel contact = new CorporateContactModel();
				contact.setName((String) contactMap.get("name"));
				contact.setPhoneNo((String) contactMap.get("phoneNo"));
				contact.setMobileNo((String) contactMap.get("mobileNo"));
				contact.setEmail((String) contactMap.get("email"));
				contact.setFaxNo((String) contactMap.get("faxNo"));
				contact.setCorporate(corporate);
				contactListModel.add(contact);
			}
			corporate.setCorporateContact(contactListModel);
		}

		return corporate;
	}

	private void checkUniqueRecord(String id) throws Exception {
		CorporateModel corporate = corporateRepo.findOne(id);

		if (corporate != null) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CorporateModel corporate = setMapToModel(map);
				CorporateModel corporateExisting = corporateRepo.findOne(corporate.getId());

				boolean outsource = corporate.getOutsourceAdminFlag().equals(ApplicationConstants.YES);
				
				// cek jika record replacement
				if (corporateExisting != null && ApplicationConstants.YES.equals(corporateExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					corporateExisting.setName(corporate.getName());
					corporateExisting.setHostCifId(corporate.getHostCifId());
					corporateExisting.setAddress1(corporate.getAddress1());
					corporateExisting.setAddress2(corporate.getAddress2());
					corporateExisting.setAddress3(corporate.getAddress3());

					corporateExisting.setPostcode(corporate.getPostcode());

					corporateExisting.setCity(corporate.getCity());

					corporateExisting.setSubstate(corporate.getSubstate());

					corporateExisting.setState(corporate.getState());

					corporateExisting.setCountry(corporate.getCountry());

					corporateExisting.setEmail1(corporate.getEmail1());
					corporateExisting.setEmail2(corporate.getEmail2());
					corporateExisting.setPhoneNo(corporate.getPhoneNo());
					corporateExisting.setExtNo(corporate.getExtNo());
					corporateExisting.setFaxNo(corporate.getFaxNo());
					corporateExisting.setSpecialChargeFlag(corporate.getSpecialChargeFlag());
					corporateExisting.setSpecialLimitFlag(corporate.getSpecialLimitFlag());
					
					corporateExisting.setBranch(corporate.getBranch());

					ServicePackageModel servicePackage = corporate.getServicePackage();
					corporateExisting.setServicePackage(servicePackage);

					corporateExisting.setRole(servicePackage.getMenuPackage().getRole());

					corporateExisting.setIndustrySegment(corporate.getIndustrySegment());

					corporateExisting.setBusinessUnit(corporate.getBusinessUnit());

					corporateExisting.setTaxIdNo(corporate.getTaxIdNo());

					corporateExisting.setHandlingOfficer(corporate.getHandlingOfficer());

					corporateExisting.setMaxCorporateUser(corporate.getMaxCorporateUser());

					corporateExisting.setLldIsResidence(corporate.getLldIsResidence());
					corporateExisting.setLldIsCitizen(corporate.getLldIsCitizen());

					corporateExisting.setCitizenCountry(corporate.getCitizenCountry());

					corporateExisting.setResidenceCountry(corporate.getResidenceCountry());
					
					corporateExisting.setLldCategory(corporate.getLldCategory());
					
					corporateExisting.setOutsourceAdminFlag(corporate.getOutsourceAdminFlag());
					corporateExisting.setTokenAuthenticationFlag(corporate.getTokenAuthenticationFlag());

					saveCorporate(corporateExisting, vo.getCreatedBy(), true);
				} else {
					saveCorporate(corporate, vo.getCreatedBy(), true);

					String userIdAdminApproverForToken = ApplicationConstants.EMPTY_STRING;
					String userNameAdminApproverForToken = ApplicationConstants.EMPTY_STRING;
					
					
					// create token
					List<String> tokenList = (ArrayList<String>) map.get("tokenList");
					Map<String, TokenUserModel> tokenUserMap = new HashMap<>();
					for (String tokenNo : tokenList) {
						TokenUserModel tokenUser = tokenUserService.saveTokenUser(tokenNo, vo.getCreatedBy(),
								corporate.getId(), ApplicationConstants.TOKEN_TYPE_HARD_TOKEN);
						tokenUserMap.put(tokenNo, tokenUser);
					}

					// create corporate admin
					List<Map<String, Object>> adminList = (ArrayList<Map<String, Object>>) map.get("adminList");
					for (Map<String, Object> adminMap : adminList) {
						String userId = (String) adminMap.get("userId");
						String userName = (String) adminMap.get("name");
						String mobileNo = (String) adminMap.get("mobileNo");
						String email = (String) adminMap.get("email");
						String role = (String) adminMap.get("role");
						String tokenNo = (String) adminMap.get("tokenNo");

						if ("maker".equalsIgnoreCase(role)) {
							corporateUserService.saveCorporateAdminUser(corporate, userId, userName, mobileNo, email,
									vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_MAKER);
						} else if ("checker".equalsIgnoreCase(role)) {
							corporateUserService.saveCorporateAdminUser(corporate, userId, userName, mobileNo, email,
									vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
						}

						if(!outsource) {
							//assign user with token
							if (tokenUserMap.get(tokenNo) != null) {
								TokenUserModel tokenUser = tokenUserMap.get(tokenNo);
	
								userIdAdminApproverForToken = Helper.getCorporateUserCode(corporate.getId(), userId);
								userNameAdminApproverForToken = userName;
								IDMUserModel assignedUser = new IDMUserModel();
								assignedUser.setCode(userIdAdminApproverForToken);
								tokenUser.setAssignedUser(assignedUser);
								tokenUser.setAssignedDate(DateUtils.getCurrentTimestamp());
	
								IDMUserModel assignedBy = new IDMUserModel();
								assignedBy.setCode(vo.getCreatedBy());
								tokenUser.setAssignedBy(assignedBy);
								
								tokenUserRepo.save(tokenUser);
							}
						}

					}
					
					ServicePackageModel servicePackage = corporate.getServicePackage();

					// create corporate limit
					LimitPackageModel limitPackage = servicePackage.getLimitPackage();
					createCorporateLimit(limitPackage, corporate, vo.getCreatedBy());

					// create corporate charge
					ChargePackageModel chargePackage = servicePackage.getChargePackage();
					createCorporateCharge(chargePackage, corporate, vo.getCreatedBy());
					
					if(!outsource) {
						//register to token server
						Map<String, Object> inputs = new HashMap<>();
						inputs.put("corpId", corporate.getId());
						inputs.put("corpName", corporate.getName());
						inputs.put("userId", userIdAdminApproverForToken);
						inputs.put("userName", userNameAdminApproverForToken);
						inputs.put("listOfTokenNo", tokenList);
						
						eaiAdapter.invokeService(EAIConstants.REGISTER_CORPORATE_TOKEN, inputs);
					}else {
						Map<String, Object> inputs = new HashMap<>();
						inputs.put("corpId", corporate.getId());
						inputs.put("listOfTokenNo", tokenList);
						
						eaiAdapter.invokeService(EAIConstants.RESERVE_TOKEN, inputs);
					}

				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CorporateModel corporateNew = setMapToModel(map);

				CorporateModel corporateExisting = getExistingRecord(corporateNew.getId(), true);
				
				//update service package maka update jg limit dan charges
				if(!corporateExisting.getServicePackage().getCode().equals(corporateNew.getServicePackage().getCode())) {
					updateCorporateLimitAndCharges(corporateExisting, corporateNew, vo.getCreatedBy());
				}

				// set value yg boleh di edit
				corporateExisting.setName(corporateNew.getName());
				corporateExisting.setHostCifId(corporateNew.getHostCifId());
				corporateExisting.setAddress1(corporateNew.getAddress1());
				corporateExisting.setAddress2(corporateNew.getAddress2());
				corporateExisting.setAddress3(corporateNew.getAddress3());

				corporateExisting.setPostcode(corporateNew.getPostcode());

				corporateExisting.setCity(corporateNew.getCity());

				corporateExisting.setSubstate(corporateNew.getSubstate());

				corporateExisting.setState(corporateNew.getState());

				corporateExisting.setCountry(corporateNew.getCountry());

				corporateExisting.setEmail1(corporateNew.getEmail1());
				corporateExisting.setEmail2(corporateNew.getEmail2());
				corporateExisting.setPhoneNo(corporateNew.getPhoneNo());
				corporateExisting.setExtNo(corporateNew.getExtNo());
				corporateExisting.setFaxNo(corporateNew.getFaxNo());
				corporateExisting.setSpecialChargeFlag(corporateNew.getSpecialChargeFlag());
				corporateExisting.setSpecialLimitFlag(corporateNew.getSpecialLimitFlag());
//				corporateExisting.setOutsourceAdminFlag(corporateNew.getOutsourceAdminFlag());
//				corporateExisting.setTokenAuthenticationFlag(corporateNew.getTokenAuthenticationFlag());

				corporateExisting.setBranch(corporateNew.getBranch());

				ServicePackageModel servicePackage = corporateNew.getServicePackage();
				corporateExisting.setServicePackage(servicePackage);

				corporateExisting.setRole(servicePackage.getMenuPackage().getRole());

				corporateExisting.setIndustrySegment(corporateNew.getIndustrySegment());

				corporateExisting.setBusinessUnit(corporateNew.getBusinessUnit());

				corporateExisting.setTaxIdNo(corporateNew.getTaxIdNo());

				corporateExisting.setHandlingOfficer(corporateNew.getHandlingOfficer());

				corporateExisting.setMaxCorporateUser(corporateNew.getMaxCorporateUser());

				corporateExisting.setLldIsResidence(corporateNew.getLldIsResidence());
				corporateExisting.setLldIsCitizen(corporateNew.getLldIsCitizen());

				corporateExisting.setCitizenCountry(corporateNew.getCitizenCountry());

				corporateExisting.setResidenceCountry(corporateNew.getResidenceCountry());
				
				corporateExisting.setLldCategory(corporateNew.getLldCategory());
				
				// delete corporate contact
				corporateRepo.deleteByCorporateId(corporateExisting.getId());
				
				//add new corporate contact
				corporateExisting.setCorporateContact(corporateNew.getCorporateContact());

				
				//jika flag outsource berubah
				if(!corporateExisting.getOutsourceAdminFlag().equals(corporateNew.getOutsourceAdminFlag())) {
					
					corporateExisting.setOutsourceAdminFlag(corporateNew.getOutsourceAdminFlag());
					corporateExisting.setTokenAuthenticationFlag(corporateNew.getTokenAuthenticationFlag());
					
					
					//dari N ke Y
					if(corporateNew.getOutsourceAdminFlag().equals(ApplicationConstants.YES)) {
						
						//delete existing user and unassigned
						// get admin list
						List<CorporateUserModel> adminList = corporateUserRepo.findAdminUser(corporateNew.getId());
						
						for (CorporateUserModel corporateUser : adminList) {
							corporateUserService.deleteCorporateUser(corporateUser, vo.getCreatedBy());							
							if ("CRP_ADM_CHECKER".equals(corporateUser.getApprovalMap().getWorkflowRole().getCode())) {
								TokenUserModel tokenUserModel = tokenUserRepo.findByAssignedUserCode(corporateUser.getUser().getCode());
								//unassignToken
								if(tokenUserModel!=null) {
									tokenUserService.unassignToken(corporateUser.getUser().getCode(), tokenUserModel.getTokenNo(), corporateNew.getId());
								}
							}
						}
						
						//save new Outsource User
						// create corporate admin
						List<Map<String, Object>> osAdminList = (ArrayList<Map<String, Object>>) map.get("adminList");
						for (Map<String, Object> adminMap : osAdminList) {
							String userId = (String) adminMap.get("userId");
							String userName = (String) adminMap.get("name");
							String mobileNo = (String) adminMap.get("mobileNo");
							String email = (String) adminMap.get("email");
							String role = (String) adminMap.get("role");
							
							String userCode = Helper.getCorporateUserCode(corporateExisting.getId(), userId);
							IDMUserModel idmUser = idmUserRepo.findOne(userCode);
							
							if(idmUser!=null) {
								corporateUserService.updateCorporateAdminUser(corporateExisting, userId, userName, mobileNo, email, "", vo.getCreatedBy());
							}else {
								if ("maker".equalsIgnoreCase(role)) {								
									corporateUserService.saveCorporateAdminUser(corporateExisting, userId, userName, mobileNo, email,
											vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_MAKER);
								} else if ("checker".equalsIgnoreCase(role)) {
									corporateUserService.saveCorporateAdminUser(corporateExisting, userId, userName, mobileNo, email,
											vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
								}
							}
							
						}
						
					}else { //dari Y ke N
						
						//delete outsouce User
						List<CorporateUserModel> osAdminList = corporateUserRepo.findAdminUser(corporateNew.getId());
						
						for (CorporateUserModel corporateUser : osAdminList) {
							corporateUserService.deleteCorporateUser(corporateUser, vo.getCreatedBy());
						}
						
						//save new Admin User and assign Token						
						List<Map<String, Object>> adminList = (ArrayList<Map<String, Object>>) map.get("adminList");
						for (Map<String, Object> adminMap : adminList) {
							String userId = (String) adminMap.get("userId");
							String userName = (String) adminMap.get("name");
							String mobileNo = (String) adminMap.get("mobileNo");
							String email = (String) adminMap.get("email");
							String role = (String) adminMap.get("role");
							String tokenNo = (String) adminMap.get("tokenNo");
							
							String userCode = Helper.getCorporateUserCode(corporateExisting.getId(), userId);
							IDMUserModel idmUser = idmUserRepo.findOne(userCode);
							
							if(idmUser!=null) {
								corporateUserService.updateCorporateAdminUser(corporateExisting, userId, userName, mobileNo, email, tokenNo, vo.getCreatedBy());
								idmUserService.resetUser(idmUser, vo.getCreatedBy());								
							}else {
								if ("maker".equalsIgnoreCase(role)) {
									corporateUserService.saveCorporateAdminUser(corporateNew, userId, userName, mobileNo, email,
											vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_MAKER);
								} else if ("checker".equalsIgnoreCase(role)) {
									corporateUserService.saveCorporateAdminUser(corporateNew, userId, userName, mobileNo, email,
											vo.getCreatedBy(), ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
									//assignToken
									tokenUserService.assignToken(userCode, tokenNo, corporateNew.getId(), vo.getCreatedBy());
								}
							}
						}
						
					}
				}else {
					if(corporateNew.getOutsourceAdminFlag().equals(ApplicationConstants.NO)) {
						List<Map<String, Object>> adminList = (ArrayList<Map<String, Object>>) map.get("adminList");
						for (Map<String, Object> adminMap : adminList) {
							String userId = (String) adminMap.get("userId");
							String userName = (String) adminMap.get("name");
							String mobileNo = (String) adminMap.get("mobileNo");
							String email = (String) adminMap.get("email");
							String tokenNo = (String) adminMap.get("tokenNo");
	
							corporateUserService.updateCorporateAdminUser(corporateExisting, userId, userName, mobileNo, email,
									tokenNo, vo.getCreatedBy());
						}
					}
				}
				
				updateCorporate(corporateExisting, vo.getCreatedBy());

			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				CorporateModel corporate = getExistingRecord((String) map.get(ApplicationConstants.CORP_ID), true);

				deleteCorporate(corporate, vo.getCreatedBy());
			} else if ("UPDATE_STATUS".equals(vo.getAction())) {
				// check existing record exist or not
				CorporateModel corporate = getExistingRecord((String) map.get(ApplicationConstants.CORP_ID), true);

				updateCorporateStatus(corporate, (String) map.get(ApplicationConstants.CORP_ID), 
						(String) map.get("inactiveFlag"), (String) map.get("inactiveReason"), 
						(String) map.get(ApplicationConstants.LOGIN_USERID));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private void updateCorporateLimitAndCharges(CorporateModel existing, CorporateModel corporateNew, String createdBy) throws Exception {
		String corporateId = existing.getId();
		
		//delete old corporate limit
		corporateLimitRepo.deleteByCorporateId(corporateId);
		
		//delete  old corporate charge
		corporateChargeRepo.deleteByCorporateId(corporateId);
		
		ServicePackageModel servicePackage = corporateNew.getServicePackage();
		
		// create corporate limit
		LimitPackageModel limitPackage = servicePackage.getLimitPackage();
		createCorporateLimit(limitPackage, corporateNew, createdBy);

		// create corporate charge
		ChargePackageModel chargePackage = servicePackage.getChargePackage();
		createCorporateCharge(chargePackage, corporateNew, createdBy);
		
		//delete unused service code from corporate user group detail
		List<String> serviceCodeList = new ArrayList<>();
		for(LimitPackageDetailModel detail : servicePackage.getLimitPackage().getLimitPackageDetail()) {
			serviceCodeList.add(detail.getService().getCode());
		}
		
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("corporateId", corporateId);
		Page<CorporateUserGroupModel> corporateUserGroupPage = corporateUserGroupRepo.searchUserGroupNotAdmin(inputMap, null);
		List<String> corporateUserGroupList = new ArrayList<>();
		for(CorporateUserGroupModel userGroup : corporateUserGroupPage) {
			corporateUserGroupList.add(userGroup.getId());
		}
		if(corporateUserGroupList.size() > 0) {
			corporateUserGroupRepo.deleteDetailByCorporateIdAndServiceCode(corporateUserGroupList, serviceCodeList);
			corporateUserGroupRepo.resetLimitByUserGroupId(corporateUserGroupList);
		}
		//-----------------------------
		
		
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCorporate(CorporateModel corporate, String createdBy, boolean isNeedFlush)
			throws ApplicationException, BusinessException {
		// set default value
		corporate.setDeleteFlag(ApplicationConstants.NO);
		corporate.setInactiveFlag(ApplicationConstants.NO);
		corporate.setCreatedDate(DateUtils.getCurrentTimestamp());
		corporate.setCreatedBy(createdBy);
		corporate.setUpdatedDate(null);
		corporate.setUpdatedBy(null);

		if (isNeedFlush) {
			corporateRepo.saveAndFlush(corporate);
		} else {
			corporateRepo.save(corporate);
		}

	}

	@Override
	public void updateCorporate(CorporateModel corporate, String updatedBy)
			throws ApplicationException, BusinessException {
		corporate.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporate.setUpdatedBy(updatedBy);
		corporateRepo.save(corporate);
	}

	private void updateCorporateStatus(CorporateModel corporate, String corporateId, String inactiveFlag, String inactiveReason, String updatedBy)
			throws ApplicationException, BusinessException {
		corporate.setInactiveReason(inactiveReason);
		corporate.setInactiveFlag(inactiveFlag);
		updateCorporate(corporate, updatedBy);
	}

	@Override
	public void deleteCorporate(CorporateModel corporate, String deletedBy)
			throws ApplicationException, BusinessException {
		corporate.setDeleteFlag(ApplicationConstants.YES);
		corporate.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporate.setUpdatedBy(deletedBy);

		corporateRepo.save(corporate);
	}

	private void createCorporateLimit(LimitPackageModel limitPackage, CorporateModel corporate, String createdBy)
			throws Exception {
		List<LimitPackageDetailModel> limitPackageDetailList = limitPackage.getLimitPackageDetail();

		List<CorporateLimitModel> corporateLimitList = new ArrayList<>();
		for (LimitPackageDetailModel limitPackageDetail : limitPackageDetailList) {
			CorporateLimitModel corporateLimit = new CorporateLimitModel();
			corporateLimit.setCorporate(corporate);
			corporateLimit.setCurrency(limitPackageDetail.getCurrency());
			corporateLimit.setLimitPackage(limitPackage);
			corporateLimit.setService(limitPackageDetail.getService());
			corporateLimit.setServiceCurrencyMatrix(limitPackageDetail.getServiceCurrencyMatrix());
			corporateLimit.setApplication(limitPackage.getApplication());
			corporateLimit.setMaxAmountLimit(limitPackageDetail.getMaxAmountLimit());
			corporateLimit.setMaxOccurrenceLimit(limitPackageDetail.getMaxOccurrenceLimit());
			corporateLimit.setAmountLimitUsage(new BigDecimal(0));
			corporateLimit.setCreatedBy(createdBy);
			corporateLimit.setCreatedDate(DateUtils.getCurrentTimestamp());
			corporateLimitList.add(corporateLimit);
		}
		corporateLimitRepo.save(corporateLimitList);
	}

	private void createCorporateCharge(ChargePackageModel chargePackage, CorporateModel corporate, String createdBy)
			throws Exception {
		List<ChargePackageDetailModel> chargePackageDetailList = chargePackage.getChargePackageDetail();

		List<CorporateChargeModel> corporateChargeList = new ArrayList<>();
		for (ChargePackageDetailModel chargePackageDetail : chargePackageDetailList) {
			CorporateChargeModel corporateCharge = new CorporateChargeModel();
			corporateCharge.setCorporate(corporate);
			corporateCharge.setValue(chargePackageDetail.getValue());
			corporateCharge.setChargePackage(chargePackage);
			corporateCharge.setValueType(chargePackageDetail.getValueType());
			corporateCharge.setServiceCharge(chargePackageDetail.getServiceCharge());
			corporateCharge.setApplication(chargePackage.getApplication());
			corporateCharge.setCurrency(chargePackageDetail.getCurrency());
			corporateCharge.setCreatedBy(createdBy);
			corporateCharge.setCreatedDate(DateUtils.getCurrentTimestamp());
			corporateChargeList.add(corporateCharge);
		}
		corporateChargeRepo.save(corporateChargeList);
	}

	@Override
	public Map<String, Object> getContactList(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
			List<CorporateContactModel> corporateContactList = corporateRepo.findCorporateContact(corporateId);

			List<Map<String, Object>> contactList = new ArrayList<>();
			for (CorporateContactModel corporateContact : corporateContactList) {
				Map<String, Object> contactMap = new HashMap<>();
				contactMap.put("name", corporateContact.getName());
				contactMap.put("phoneNo", corporateContact.getPhoneNo());
				contactMap.put("mobileNo", corporateContact.getMobileNo());
				contactMap.put("email", corporateContact.getEmail());
				contactMap.put("faxNo", corporateContact.getFaxNo());
				contactList.add(contactMap);
			}
			resultMap.put("result", contactList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("cifId", map.get("cifId"));

			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.CIF_INQUIRY, inputs);

			Map<String, Object> result = new HashMap<>(10, 1);
			result.put("name", outputs.get("name"));
			result.put("cifId", outputs.get("cifId"));
			result.put("address1", outputs.get("address1"));
			result.put("address2", outputs.get("address2"));
			result.put("address3", outputs.get("address3"));
			result.put("postcode", outputs.get("postCode"));
			result.put("cityCode", outputs.get("cityCode"));
			result.put("stateCode", outputs.get("stateCode"));
			result.put("substateCode", outputs.get("substateCode"));
			result.put("countryCode", outputs.get("countryCode"));

			return result;
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findMenuForPendingTask(String corporateId)
			throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateModel corporate = corporateRepo.findOne(corporateId);
			
			String corporateRoleCode = corporate.getRole().getCode();

			List<IDMMenuModel> menuList = idmRepo.getMenuRepo().findTransactionAndMaintenanceMenuByRoleCode(corporateRoleCode);
			
			List<Map<String, Object>> menus = new ArrayList<>();
			for (IDMMenuModel model : menuList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("pendingTaskCode", model.getCode());
				modelMap.put("pendingTaskName", model.getName());
				menus.add(modelMap);
			}
			resultMap.put("menus", menus);

		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> findNonFinancialMenu(String corporateId, String nonFinMenuType)
			throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateModel corporate = corporateRepo.findOne(corporateId);
			
			String corporateRoleCode = corporate.getRole().getCode();

			List<String> roleList = new ArrayList<>();
			
			if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_ALL_MENU)) {
				roleList.add(corporateRoleCode);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_MAKER);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
			} else if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_ADMIN_MENU)) {
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_MAKER);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
			} else if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_USER_MENU)) {
				roleList.add(corporateRoleCode);
			}
			
			List<IDMMenuModel> menuList = idmRepo.getMenuRepo().findNonFinancialMenuByRoleCode(roleList);
			
			List<Map<String, Object>> menus = new ArrayList<>();
			for (IDMMenuModel model : menuList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("pendingTaskCode", model.getCode());
				modelMap.put("pendingTaskName", model.getName());
				menus.add(modelMap);
			}
			resultMap.put("menus", menus);

		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public List<String> findListOfStringNonFinancialMenu(String corporateId, String nonFinMenuType)
			throws ApplicationException {
		try {
			CorporateModel corporate = corporateRepo.findOne(corporateId);
			
			String corporateRoleCode = corporate.getRole().getCode();

			List<String> roleList = new ArrayList<>();
			if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_ALL_MENU)) {
				roleList.add(corporateRoleCode);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_MAKER);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
			} else if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_ADMIN_MENU)) {
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_MAKER);
				roleList.add(ApplicationConstants.ROLE_WF_ADMIN_CHECKER);
			} else if(nonFinMenuType.equals(ApplicationConstants.NON_FIN_USER_MENU)) {
				roleList.add(corporateRoleCode);
			}
			
			return idmRepo.getMenuRepo().findListOfStringForNonFinancialMenuByRoleCode(roleList);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchCorporates() throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<CorporateModel> result = corporateRepo.findCorporates();

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (CorporateModel model : result) {
				Map<String, Object> map = new HashMap<>();
				map.put(ApplicationConstants.CORP_ID, model.getId());
				map.put("corporateName", model.getId());
				resultList.add(map);
			}
			
			resultMap.put("result", resultList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
}
