package com.gpt.component.maintenance.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.branch.repository.BranchRepository;
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.domesticbank.repository.DomesticBankRepository;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.internationalbank.repository.InternationalBankRepository;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.BusinessUnitModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.parametermt.model.HandlingOfficerModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.component.maintenance.parametermt.model.IndustrySegmentModel;
import com.gpt.component.maintenance.parametermt.model.LevelModel;
import com.gpt.component.maintenance.parametermt.model.PostCodeModel;
import com.gpt.component.maintenance.parametermt.model.StateModel;
import com.gpt.component.maintenance.parametermt.model.SubstateModel;
import com.gpt.component.maintenance.parametermt.model.TimeOutErrorCodeModel;
import com.gpt.component.maintenance.parametermt.repository.AccountTypeRepository;
import com.gpt.component.maintenance.parametermt.repository.BeneficiaryTypeRepository;
import com.gpt.component.maintenance.parametermt.repository.BusinessUnitRepository;
import com.gpt.component.maintenance.parametermt.repository.CityRepository;
import com.gpt.component.maintenance.parametermt.repository.CountryRepository;
import com.gpt.component.maintenance.parametermt.repository.CurrencyRepository;
import com.gpt.component.maintenance.parametermt.repository.HandlingOfficerRepository;
import com.gpt.component.maintenance.parametermt.repository.IdentityTypeRepository;
import com.gpt.component.maintenance.parametermt.repository.IndustrySegmentRepository;
import com.gpt.component.maintenance.parametermt.repository.LevelRepository;
import com.gpt.component.maintenance.parametermt.repository.PostCodeRepository;
import com.gpt.component.maintenance.parametermt.repository.StateRepository;
import com.gpt.component.maintenance.parametermt.repository.SubstateRepository;
import com.gpt.component.maintenance.parametermt.repository.TimeOutErrorCodeRepository;
import com.gpt.component.maintenance.sysparam.model.SysParamModel;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.component.maintenance.wfrole.repository.WorkflowRoleRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Component
public class MaintenanceRepository {
	
	@Autowired
	private AccountTypeRepository accountTypeRepo;
	
	@Autowired
	private BeneficiaryTypeRepository beneficiaryTypeRepo;
	
	@Autowired
	private BusinessUnitRepository businessUnitRepo;
	
	@Autowired
	private HandlingOfficerRepository handlingOfficerRepo;
	
	@Autowired
	private IndustrySegmentRepository industrySegmentRepo;
	
	@Autowired
	private BranchRepository branchRepo;
	
	@Autowired
	private CountryRepository countryRepo;

	@Autowired
	private CityRepository cityRepo;

	@Autowired
	private LevelRepository levelRepo;
	
	@Autowired
	private StateRepository stateRepo;

	@Autowired
	private SubstateRepository substateRepo;

	@Autowired
	private PostCodeRepository postCodeRepo;
	
	@Autowired
	private DomesticBankRepository domesticBankRepo;
	
	@Autowired
	private InternationalBankRepository internationalBankRepo;
	
	@Autowired
	private CurrencyRepository currencyRepo;
	
	@Autowired
	private WorkflowRoleRepository workflowRoleRepo;
	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	@Autowired
	private IdentityTypeRepository identityTypeRepo;
	
	@Autowired
	private TimeOutErrorCodeRepository errorCodeRepository;
	
	
	public CityModel isCityValid(String code) throws Exception {
		CityModel model = cityRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100011");
			}
		} else {
			throw new BusinessException("GPT-0100011");
		}
		
		return model;
	}

	public LevelModel isLevelValid(String code) throws Exception {
		LevelModel model = levelRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100012");
			}
		} else {
			throw new BusinessException("GPT-0100012");
		}
		
		return model;
	}
	
	public BranchModel isBranchValid(String code) throws Exception {
		BranchModel model = branchRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100008");
			}
		} else {
			throw new BusinessException("GPT-0100008");
		}
		
		return model;
	}

	public BranchModel isParentBranchValid(String code) throws Exception {
		BranchModel model = branchRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100013");
			}
		} else {
			throw new BusinessException("GPT-0100013");
		}
		
		return model;
	}
	
	public StateModel isStateValid(String code) throws Exception {
		StateModel model = stateRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100014");
			}
		} else {
			throw new BusinessException("GPT-0100014");
		}
		
		return model;
	}
	
	public SubstateModel isSubstateValid(String code) throws Exception {
		SubstateModel model = substateRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT--0100015");
			}
		} else {
			throw new BusinessException("GPT--0100015");
		}
		
		return model;
	}
	
	public PostCodeModel isPostCodeValid(String code) throws Exception {
		PostCodeModel model = postCodeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100016");
			}
		} else {
			throw new BusinessException("GPT-0100016");
		}
		
		return model;
	}
	
	public DomesticBankModel isDomesticBankValid(String code) throws Exception {
		DomesticBankModel model = domesticBankRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100023");
			}
		} else {
			throw new BusinessException("GPT-0100023");
		}
		
		return model;
	}
	
	public InternationalBankModel isInternationalBankValid(String code) throws Exception {
		InternationalBankModel model = internationalBankRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100208");
			}
		} else {
			throw new BusinessException("GPT-0100208");
		}
		
		return model;
	}

	public CurrencyModel isCurrencyValid(String code) throws Exception {
		CurrencyModel model = currencyRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100020");
			}
		} else {
			throw new BusinessException("GPT-0100020");
		}
		
		return model;
	}
	
	public AccountTypeModel isAccountTypeValid(String code) throws Exception {
		AccountTypeModel model = accountTypeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100040");
			}
		} else {
			throw new BusinessException("GPT-0100040");
		}
		
		return model;
	}
	
	public BeneficiaryTypeModel isBeneficiaryTypeValid(String code) throws Exception {
		BeneficiaryTypeModel model = beneficiaryTypeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100041");
			}
		} else {
			throw new BusinessException("GPT-0100041");
		}
		
		return model;
	}
	
	public BusinessUnitModel isBusinessUnitValid(String code) throws Exception {
		BusinessUnitModel model = businessUnitRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100042");
			}
		} else {
			throw new BusinessException("GPT-0100042");
		}
		
		return model;
	}
	
	public HandlingOfficerModel isHandlingOfficerValid(String code) throws Exception {
		HandlingOfficerModel model = handlingOfficerRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100043");
			}
		} else {
			throw new BusinessException("GPT-0100043");
		}
		
		return model;
	}

	public IndustrySegmentModel isIndustrySegmentValid(String code) throws Exception {
		IndustrySegmentModel model = industrySegmentRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100044");
			}
		} else {
			throw new BusinessException("GPT-0100044");
		}
		
		return model;
	}
	
	public CountryModel isCountryValid(String code) throws Exception {
		CountryModel model = countryRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100045");
			}
		} else {
			throw new BusinessException("GPT-0100045");
		}
		
		return model;
	}
	
	public SysParamModel isSysParamValid(String code) throws Exception {
		SysParamModel model = sysParamRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100055");
			}
		} else {
			throw new BusinessException("GPT-0100055");
		}
		
		return model;
	}
	
	public WorkflowRoleModel isWorkflowRoleValid(String code) throws Exception {
		WorkflowRoleModel model = workflowRoleRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100063");
			}
		} else {
			throw new BusinessException("GPT-0100063");
		}
		
		return model;
	}
	
	public IdentityTypeModel isIdentityTypeValid(String code) throws Exception {
		IdentityTypeModel model = identityTypeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100219");
			}
		} else {
			throw new BusinessException("GPT-0100219");
		}
		
		return model;
	}
	
	public Boolean isTimeOutErrorCodeValid(String code) throws Exception {
		if(code==null) {
			return false;
		}
		TimeOutErrorCodeModel model = errorCodeRepository.findOne(code);		
		return (model != null) && (!ApplicationConstants.YES.equals(model.getDeleteFlag()));
	}
	
	public WorkflowRoleRepository getWorkflowRoleRepo() {
		return workflowRoleRepo;
	}

	public BranchRepository getBranchRepo() {
		return branchRepo;
	}

	public CityRepository getCityRepo() {
		return cityRepo;
	}

	public LevelRepository getLevelRepo() {
		return levelRepo;
	}

	public StateRepository getStateRepo() {
		return stateRepo;
	}

	public SubstateRepository getSubstateRepo() {
		return substateRepo;
	}

	public PostCodeRepository getPostCodeRepo() {
		return postCodeRepo;
	}

	public DomesticBankRepository getDomesticBankRepo() {
		return domesticBankRepo;
	}

	public CurrencyRepository getCurrencyRepo() {
		return currencyRepo;
	}

	public AccountTypeRepository getAccountTypeRepo() {
		return accountTypeRepo;
	}

	public BeneficiaryTypeRepository getBeneficiaryTypeRepo() {
		return beneficiaryTypeRepo;
	}

	public BusinessUnitRepository getBusinessUnitRepo() {
		return businessUnitRepo;
	}

	public HandlingOfficerRepository getHandlingOfficerRepo() {
		return handlingOfficerRepo;
	}

	public IndustrySegmentRepository getIndustrySegmentRepo() {
		return industrySegmentRepo;
	}

	public CountryRepository getCountryRepo() {
		return countryRepo;
	}

	public SysParamRepository getSysParamRepo() {
		return sysParamRepo;
	}

	public InternationalBankRepository getInternationalBankRepo() {
		return internationalBankRepo;
	}

	public IdentityTypeRepository getIdentityTypeRepo() {
		return identityTypeRepo;
	}
	
	public TimeOutErrorCodeRepository getErrorCodeRepository() {
		return errorCodeRepository;
	}
	
	
}
