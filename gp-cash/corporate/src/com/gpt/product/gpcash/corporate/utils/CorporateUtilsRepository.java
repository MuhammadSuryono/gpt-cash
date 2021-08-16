package com.gpt.product.gpcash.corporate.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.repository.AuthorizedLimitSchemeRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccount.repository.CorporateAccountRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;
import com.gpt.product.gpcash.corporate.corporatelimit.repository.CorporateLimitRepository;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.repository.CorporateUserGroupRepository;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.repository.TokenUserRepository;

@Component
public class CorporateUtilsRepository {
	@Autowired
	private CorporateRepository corporateRepo;	
	
	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;
	
	@Autowired
	private CorporateAccountRepository corporateAccountRepo;
	
	@Autowired
	private AuthorizedLimitSchemeRepository authorizedLimitSchemeRepo;
	
	@Autowired
	private CorporateUserGroupRepository corporateUserGroupRepo;
	
	@Autowired
	private CorporateUserRepository corporateUserRepo;
	
	@Autowired
	private TokenUserRepository tokenUserRepo;
	
	@Autowired
	private CorporateLimitRepository corporateLimitRepo;
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private BeneficiaryListInHouseRepository beneficiaryListInHouseRepo;
	
	@Autowired
	private BeneficiaryListDomesticRepository beneficiaryListDomesticRepo;

	@Autowired
	private BeneficiaryListInternationalRepository beneficiaryListInternationalRepo;
	
	public List<CorporateAccountGroupDetailModel> isCorporateAccountGroupDetailValid(String corporateId, String accountNo) throws Exception {
		List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountNo(corporateId, accountNo);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100051");
		}
		
		return modelList;
	}
	
	public CorporateAccountGroupDetailModel isCorporateAccountGroupDetailIdValid(String corporateId, String accountGroupDetaiId) throws Exception {
		CorporateAccountGroupDetailModel model = corporateAccountGroupRepo.findDetailById(corporateId, accountGroupDetaiId);

		if (model == null) {
			throw new BusinessException("GPT-0100051");
		}
		
		return model;
	}
	
	public CorporateAccountGroupModel isCorporateAccountGroupValid(String corporateId, String accountGroupCode) throws Exception {
		CorporateAccountGroupModel model = corporateAccountGroupRepo.findByCorporateIdAndCode(corporateId, accountGroupCode);

		if (model==null)
			throw new BusinessException("GPT-0100053");
		
		return model;
	}
	
	public List<CorporateAccountModel> isCorporateAccountValid(String corporateId, String accountNo) throws Exception {
		List<CorporateAccountModel> modelList = corporateAccountRepo.findByCorporateIdAndAccountNo(corporateId, accountNo, null).getContent();

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100052");
		}
		
		return modelList;
	}
	
	public List<AuthorizedLimitSchemeModel> isAuthorizedLimitSchemeValid(String corporateId, String authorizedLimitId) throws Exception {
		List<AuthorizedLimitSchemeModel> modelList = authorizedLimitSchemeRepo.findByCorporateIdAndId(corporateId, authorizedLimitId);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100064");
		}
		
		return modelList;
	}
	
	public CorporateUserGroupModel isCorporateUserGroupValid(String corporateId, String userGroupCode) throws Exception {
		CorporateUserGroupModel model = corporateUserGroupRepo.findByCorporateIdAndCode(corporateId, userGroupCode);

		if (model==null)
			throw new BusinessException("GPT-0100065");
		
		return model;
	}
	
	public TokenUserModel isTokenNoAlreadyAssigned(String corporateId, String tokenNo) throws Exception {
		TokenUserModel tokenUserModel = tokenUserRepo.findByCorporateIdAndTokenNo(corporateId, tokenNo);

		if (tokenUserModel == null)
			throw new BusinessException("GPT-0100070");

		if(tokenUserModel.getAssignedUser() != null)
			throw new BusinessException("GPT-0100071");
		
		return tokenUserModel;
	}
	
	public CorporateModel isCorporateValid(String corporateId) throws Exception {
		CorporateModel model = corporateRepo.findOne(corporateId);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100054");
			}
		} else {
			throw new BusinessException("GPT-0100054");
		}
		
		return model;
	}	
	
	public CorporateUserModel isCorporateUserValid(String userCode) throws Exception {
		CorporateUserModel model = corporateUserRepo.findOne(userCode);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100009");
			}
		} else {
			throw new BusinessException("GPT-0100009");
		}
		
		return model;
	}
	
	public CorporateAccountGroupDetailModel isCorporateAccountGroupDetailByAccountNoForInquiry(String corporateId, String accountNo) throws Exception {
		List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountNoAndIsInquiry(corporateId, accountNo);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100077");
		}
		
		return modelList.get(0);
	}
	
	public CorporateAccountGroupDetailModel isCorporateAccountGroupDetailByAccountNoForInquiry(String corporateId, String accountNo, String accountGroupId) throws Exception {
		List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountNoAndAccountGroupIdAndIsInquiry(corporateId, accountNo, accountGroupId);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100077");
		}
		
		return modelList.get(0);
	}
	
	public CorporateLimitModel isCorporateLimitValid(String serviceCode, String currencyMatrixCode,
			String applicationCode, String currencyCode, String corporateId) throws Exception {
		CorporateLimitModel model = corporateLimitRepo
				.findByServiceAndCurrencyMatrix(applicationCode, serviceCode, currencyMatrixCode,
						currencyCode, corporateId);
		
		if (model == null) {
			throw new BusinessException("GPT-0100098");
		}

		return model;
	}
	
	public CorporateChargeModel isCorporateChargeValid(String id) throws Exception {
		CorporateChargeModel model = corporateChargeRepo.findOne(id);

		if (model == null) {
			throw new BusinessException("GPT-0100108");
		}

		return model;
	}
	
	public CorporateUserGroupDetailModel isCorporateUserGroupDetailValid(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId) throws Exception {
		CorporateUserGroupDetailModel model = corporateUserGroupRepo
				.findDetailByServiceCodeAndCurrencyMatrixCodeAndUserGroupId(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId);

		if (model == null) {
			throw new BusinessException("GPT-0100098");
		}

		return model;
	}
	
	public BeneficiaryListInHouseModel isBeneficiaryListInHouseValid(String id) throws Exception {
		BeneficiaryListInHouseModel model = beneficiaryListInHouseRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}
	
	public BeneficiaryListDomesticModel isBeneficiaryListDomesticValid(String id) throws Exception {
		BeneficiaryListDomesticModel model = beneficiaryListDomesticRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}
	
	public CorporateRepository getCorporateRepo() {
		return corporateRepo;
	}

	public TokenUserRepository getTokenUserRepo() {
		return tokenUserRepo;
	}

	public CorporateUserGroupRepository getCorporateUserGroupRepo() {
		return corporateUserGroupRepo;
	}

	public CorporateAccountGroupRepository getCorporateAccountGroupRepo() {
		return corporateAccountGroupRepo;
	}

	public CorporateAccountRepository getCorporateAccountRepo() {
		return corporateAccountRepo;
	}

	public AuthorizedLimitSchemeRepository getAuthorizedLimitSchemeRepo() {
		return authorizedLimitSchemeRepo;
	}

	public CorporateUserRepository getCorporateUserRepo() {
		return corporateUserRepo;
	}

	public CorporateLimitRepository getCorporateLimitRepo() {
		return corporateLimitRepo;
	}

	public BeneficiaryListInHouseRepository getBeneficiaryListInHouseRepo() {
		return beneficiaryListInHouseRepo;
	}

	public BeneficiaryListDomesticRepository getBeneficiaryListDomesticRepo() {
		return beneficiaryListDomesticRepo;
	}

	public CorporateChargeRepository getCorporateChargeRepo() {
		return corporateChargeRepo;
	}
	
	public BeneficiaryListInternationalRepository getBeneficiaryListInternationalRepo() {
		return beneficiaryListInternationalRepo;
	}
	
	public BeneficiaryListInternationalModel isBeneficiaryListInternationalValid(String id) throws Exception {
		BeneficiaryListInternationalModel model = beneficiaryListInternationalRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}
	
}
