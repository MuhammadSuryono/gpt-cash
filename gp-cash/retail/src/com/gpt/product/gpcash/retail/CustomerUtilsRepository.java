package com.gpt.product.gpcash.retail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInHouseModel;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInternationalModel;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customeraccount.repository.CustomerAccountRepository;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;
import com.gpt.product.gpcash.retail.customercharge.repository.CustomerChargeRepository;
import com.gpt.product.gpcash.retail.customerlimit.model.CustomerLimitModel;
import com.gpt.product.gpcash.retail.customerlimit.repository.CustomerLimitRepository;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;
import com.gpt.product.gpcash.retail.token.tokenuser.repository.CustomerTokenUserRepository;

@Component
public class CustomerUtilsRepository {
	@Autowired
	private CustomerRepository customerRepo;	
	
	@Autowired
	private CustomerAccountRepository customerAccountRepo;
	
	@Autowired
	private CustomerTokenUserRepository tokenUserRepo;
	
	@Autowired
	private CustomerLimitRepository customerLimitRepo;
	
	@Autowired
	private CustomerChargeRepository customerChargeRepo;
	
	@Autowired
	private CustomerBeneficiaryListInHouseRepository beneficiaryListInHouseRepo;
	
	@Autowired
	private CustomerBeneficiaryListDomesticRepository beneficiaryListDomesticRepo;

	@Autowired
	private CustomerBeneficiaryListInternationalRepository beneficiaryListInternationalRepo;
	
	public CustomerAccountModel isCustomerAccountValid(String customerId, String accountNo) throws Exception {
		CustomerAccountModel model = customerAccountRepo.findByCustomerIdAndAccountNo(customerId, accountNo);

		if (model == null) {
			throw new BusinessException("GPT-0100052");
		}
		
		return model;
	}
	
	public CustomerAccountModel isCustomerAccountIdValid(String customerId, String accountId) throws Exception {
		CustomerAccountModel model = customerAccountRepo.findByCustomerIdAndAccountId(customerId, accountId);

		if (model == null) {
			throw new BusinessException("GPT-0100052");
		}
		
		return model;
	}
	
	public CustomerTokenUserModel isTokenNoAlreadyAssigned(String customerId, String tokenNo) throws Exception {
		CustomerTokenUserModel tokenUserModel = tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, customerId);

		if (tokenUserModel == null)
			throw new BusinessException("GPT-0100070");

		if(tokenUserModel.getAssignedUser() != null)
			throw new BusinessException("GPT-0100071");
		
		return tokenUserModel;
	}
	
	public CustomerModel isCustomerValid(String userCode) throws Exception {
		CustomerModel model = customerRepo.findOne(userCode);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100009");
			}
		} else {
			throw new BusinessException("GPT-0100009");
		}
		
		return model;
	}
	
	public CustomerLimitModel isCustomerLimitValid(String serviceCode, String currencyMatrixCode,
			String applicationCode, String currencyCode, String customerId) throws Exception {
		CustomerLimitModel model = customerLimitRepo
				.findByServiceAndCurrencyMatrix(applicationCode, serviceCode, currencyMatrixCode,
						currencyCode, customerId);
		
		if (model == null) {
			throw new BusinessException("GPT-R-0100098");
		}

		return model;
	}
	
	public CustomerChargeModel isCustomerChargeValid(String id) throws Exception {
		CustomerChargeModel model = customerChargeRepo.findOne(id);

		if (model == null) {
			throw new BusinessException("GPT-R-0100108");
		}

		return model;
	}
	
	public CustomerBeneficiaryListInHouseModel isCustomerBeneficiaryListInHouseValid(String id) throws Exception {
		CustomerBeneficiaryListInHouseModel model = beneficiaryListInHouseRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}
	
	public CustomerBeneficiaryListDomesticModel isCustomerBeneficiaryListDomesticValid(String id) throws Exception {
		CustomerBeneficiaryListDomesticModel model = beneficiaryListDomesticRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}
	
	public CustomerBeneficiaryListInternationalModel isCustomerBeneficiaryListInternationalValid(String id) throws Exception {
		CustomerBeneficiaryListInternationalModel model = beneficiaryListInternationalRepo.findOne(id);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100100");
			}
		} else {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}

	public CustomerRepository getCustomerRepo() {
		return customerRepo;
	}

	public CustomerAccountRepository getCustomerAccountRepo() {
		return customerAccountRepo;
	}

	public CustomerTokenUserRepository getTokenUserRepo() {
		return tokenUserRepo;
	}

	public CustomerLimitRepository getCustomerLimitRepo() {
		return customerLimitRepo;
	}

	public CustomerChargeRepository getCustomerChargeRepo() {
		return customerChargeRepo;
	}

	public CustomerBeneficiaryListInHouseRepository getBeneficiaryListInHouseRepo() {
		return beneficiaryListInHouseRepo;
	}

	public CustomerBeneficiaryListDomesticRepository getBeneficiaryListDomesticRepo() {
		return beneficiaryListDomesticRepo;
	}

	public CustomerBeneficiaryListInternationalRepository getBeneficiaryListInternationalRepo() {
		return beneficiaryListInternationalRepo;
	}
	
}
