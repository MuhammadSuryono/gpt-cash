package com.gpt.product.gpcash.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.account.repository.AccountRepository;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.approvallevel.repository.ApprovalLevelRepository;
import com.gpt.product.gpcash.bankforexlimit.model.BankForexLimitModel;
import com.gpt.product.gpcash.bankforexlimit.repository.BankForexLimitRepository;
import com.gpt.product.gpcash.banktransactionlimit.model.BankTransactionLimitModel;
import com.gpt.product.gpcash.banktransactionlimit.repository.BankTransactionLimitRepository;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.chargepackage.repository.ChargePackageRepository;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.limitpackage.repository.LimitPackageRepository;
import com.gpt.product.gpcash.menupackage.model.MenuPackageModel;
import com.gpt.product.gpcash.menupackage.repository.MenuPackageRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.service.repository.ServiceRepository;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.servicepackage.repository.ServicePackageRepository;

@Component
public class ProductRepository {
	@Autowired
	private ChargePackageRepository chargePackageRepo;

	@Autowired
	private LimitPackageRepository limitPackageRepo;

	@Autowired
	private MenuPackageRepository menuPackageRepo;

	@Autowired
	private ServicePackageRepository servicePackageRepo;

	@Autowired
	private ApprovalLevelRepository approvalLevelRepo;

	@Autowired
	private AccountRepository accountRepo;

	@Autowired
	private BankTransactionLimitRepository bankTransactionLimitRepo;
	
	@Autowired
	private BankForexLimitRepository bankForexLimitRepo;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	public ChargePackageModel isChargePackageValid(String code) throws Exception {
		ChargePackageModel model = chargePackageRepo.findOne(code);

		if (model != null) {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100024");
			}
		} else {
			throw new BusinessException("GPT-0100024");
		}

		return model;
	}

	public LimitPackageModel isLimitPackageValid(String code) throws Exception {
		LimitPackageModel model = limitPackageRepo.findOne(code);

		if (model != null) {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100025");
			}
		} else {
			throw new BusinessException("GPT-0100025");
		}

		return model;
	}

	public MenuPackageModel isMenuPackageValid(String code) throws Exception {
		MenuPackageModel model = menuPackageRepo.findOne(code);

		if (model != null) {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100026");
			}
		} else {
			throw new BusinessException("GPT-0100026");
		}

		return model;
	}

	public ServicePackageModel isServicePackageValid(String code) throws Exception {
		ServicePackageModel model = servicePackageRepo.findOne(code);

		if (model != null) {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100027");
			}
		} else {
			throw new BusinessException("GPT-0100027");
		}

		return model;
	}

	public ApprovalLevelModel isApprovalLevelValid(String code) throws Exception {
		ApprovalLevelModel model = approvalLevelRepo.findOne(code);

		if (model != null) {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100028");
			}
		} else {
			throw new BusinessException("GPT-0100028");
		}

		return model;
	}

	public AccountModel isAccountValid(String accountNo) throws Exception {
		AccountModel model = accountRepo.findOne(accountNo);

		if (model == null) {
			throw new BusinessException("GPT-0100049");
		}

		return model;
	}

	public BankTransactionLimitModel isBankTransactionLimitValid(String serviceCode, String currencyMatrixCode,
			String applicationCode, String currencyCode) throws Exception {
		BankTransactionLimitModel model = bankTransactionLimitRepo
				.findByServiceCodeAndCurrencyMatrixCode(applicationCode, serviceCode, currencyMatrixCode,
						currencyCode);

		if (model == null) {
			throw new BusinessException("GPT-0100097");
		}

		return model;
	}

	public BankForexLimitModel isBankForexLimitValid(String currencyCode) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("currencyCode", currencyCode);
		BankForexLimitModel model = null;
		
		List<BankForexLimitModel> limit = bankForexLimitRepo.search(map, null).getContent();
		if (!limit.isEmpty()) {
			model = limit.get(0);
		}else {
			throw new BusinessException("GPT-131193");
		}
		return model;
	}
	
	public ChargePackageRepository getChargePackageRepo() {
		return chargePackageRepo;
	}

	public LimitPackageRepository getLimitPackageRepo() {
		return limitPackageRepo;
	}

	public MenuPackageRepository getMenuPackageRepo() {
		return menuPackageRepo;
	}

	public ServicePackageRepository getServicePackageRepo() {
		return servicePackageRepo;
	}

	public ApprovalLevelRepository getApprovalLevelRepo() {
		return approvalLevelRepo;
	}

	public AccountRepository getAccountRepo() {
		return accountRepo;
	}

	public BankTransactionLimitRepository getBankTransactionLimitRepo() {
		return bankTransactionLimitRepo;
	}
	
	public BankForexLimitRepository getBankForexLimitRepo() {
		return bankForexLimitRepo;
	}
	
	public ServiceModel isServiceValid(String code) throws Exception {
		ServiceModel model = serviceRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT--0100035");
			}
		} else {
			throw new BusinessException("GPT--0100035");
		}
		
		return model;
	}
	
	public ServiceRepository getServiceRepo() {
		return serviceRepo;
	}
}
