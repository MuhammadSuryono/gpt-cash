package com.gpt.product.gpcash.corporate.transaction.va.report.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.account.repository.AccountRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.corporate.transaction.va.registration.repository.VARegistrationRepository;
import com.gpt.product.gpcash.corporate.transaction.va.report.bean.TransactionVAData;
import com.gpt.product.gpcash.corporate.transaction.va.report.constants.VAReportConstants;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class VAReportServiceImpl implements VAReportService {
	
	@Autowired
    private AccountRepository accountRepo;
	
	@Autowired
    private CorporateRepository corporateRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;
	
	@Autowired
    private VARegistrationRepository vaRegistrationRepo;

	@Autowired
    private EAIEngine eaiAdapter;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.varegistration.download.path}")
	private String pathDownload;
	
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) 
			throws ApplicationException, BusinessException {
		try {
			String mainAccountNo = (String) map.get("mainAccountNo");
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String productCode = (String)map.get("productCode");
			
			AccountModel account = accountRepo.findOne(mainAccountNo);
			CorporateModel corporate = corporateRepo.findOne(loginCorporateId);
			String corpProductCode = corporate.getVaProductCode();
			String productName = (String) vaRegistrationRepo.findProductName(corporate.getId(), account.getAccountNo(), productCode);
			
			Map<String, Object> inputs = new HashMap<>(5,1);
			inputs.put("mainAccountNo", mainAccountNo);
			inputs.put("cif", corporate.getHostCifId());
			inputs.put("productCode", productCode);
			inputs.put("fromDate", map.get("fromDate"));
			inputs.put("toDate", map.get("toDate"));
			Map<String, Object> outputs = eaiAdapter.invokeService(VAReportConstants.VA_ACCOUNT_INQUIRY, inputs);
			
			List<Map<String, Object>> trxList = (List<Map<String, Object>>) outputs.get("vaTrxList");
			
			for(Map<String, Object> dataTrx : trxList) {
				dataTrx.put("productName", productName);
			}	
			
			Map<String, Object> result = new HashMap<>(3,1);
			result.put("mainAccountNo", mainAccountNo);
			result.put("mainAccountName", account.getAccountName());
			result.put("mainAccountCurrencyCode", account.getCurrency().getCode());
			result.put("corpProductCode", corpProductCode);
			result.put("corporateId", corporate.getId());
			result.put("corporateName", corporate.getName());
			result.put("fromDate", map.get("fromDate"));
			result.put("toDate", map.get("toDate"));
			result.put("vaTrxList", trxList);
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> downloadVAReportPDF(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		try {
			
			byte[] bytes;
			
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
			DecimalFormat df = new DecimalFormat(moneyFormat); 
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			
			String mainAccountNo = (String) map.get("mainAccountNo");
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String productCode = (String)map.get("productCode");
			
			String userName = idmUserRepo.findOne((String)map.get(ApplicationConstants.LOGIN_USERCODE)).getName();
			
			AccountModel account = accountRepo.findOne(mainAccountNo);
			CorporateModel corporate = corporateRepo.findOne(loginCorporateId);
			String fileFormat = ApplicationConstants.FILE_FORMAT_PDF;
			String productName = (String) vaRegistrationRepo.findProductName(corporate.getId(), account.getAccountNo(), productCode);
			
			Map<String, Object> inputs = new HashMap<>(5,1);
			inputs.put("mainAccountNo", mainAccountNo);
			inputs.put("cif", corporate.getHostCifId());
			inputs.put("productCode", productCode);
			inputs.put("fromDate", map.get("fromDate"));
			inputs.put("toDate", map.get("toDate"));
			Map<String, Object> outputs = eaiAdapter.invokeService(VAReportConstants.VA_ACCOUNT_INQUIRY, inputs);
			
			List<Map<String, Object>> transactions = (ArrayList<Map<String,Object>>)outputs.get("vaTrxList");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			List<TransactionVAData> detailDSList = new ArrayList<>();
			Collection<Map<String, Object>> beanCollection = new ArrayList<>();
			Collection<Map<String, Object>> masterReportCollection = new ArrayList<>();
			
			Map<String, Object> recordMap = new HashMap<>();
			recordMap.put("masterReportCounter", 0);
			masterReportCollection.add(recordMap);
			
			if (transactions!=null) {
				TransactionVAData trx = null;
				int record = 1;
				for (Map<String, Object> trxMap : transactions) {
					trx = new TransactionVAData();
					BigDecimal amount = (BigDecimal) trxMap.get("amount");
					String amountStr = "";
					
					trx.setNo(String.valueOf(record));
					
					if(!amount.equals(BigDecimal.ZERO)) {
						amountStr = df.format(amount);
					}
					
					trx.setPostDate(sdfDate.format((Date) trxMap.get("postingDate")));
					trx.setAmount(amountStr);
					trx.setVaNo((String) trxMap.get("vaNo"));
					trx.setVaName((String) trxMap.get("vaName"));
//					trx.setDescription((String) trxMap.get("dscp"));
					trx.setDescription(productName);
					trx.setFileFormat(fileFormat);
					detailDSList.add(trx);
					
					record++;
				}
			}
			
			reportParams.put("bankLogo", bankLogo);
			
			JRBeanCollectionDataSource transactionDataSource= new JRBeanCollectionDataSource(detailDSList);
			reportParams.put("transactionDataSource", transactionDataSource);
			
			
			reportParams.put("fileFormat", fileFormat);
			reportParams.put("corporateName", corporate.getName());
			reportParams.put("corporateCode", corporate.getVaProductCode());
			reportParams.put("accountInfo", mainAccountNo.concat(" - ").concat(account.getAccountName()));
			reportParams.put("accountCurrency", account.getCurrency().getCode());
			reportParams.put("periods", sdfDate.format((Date) map.get("fromDate")).concat(" - ").concat(sdfDate.format((Date) map.get("toDate"))));
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			reportParams.put("requestReportUserName", userName);
			beanCollection.add(reportParams);
			
			
			Locale locale = LocaleContextHolder.getLocale();
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			String sourceFile = pathDownload + File.separator + account.getAccountNo().concat("-").concat(downloadRefNo).concat(".").concat(fileFormat.toLowerCase());
			
			Path destinationFile = Paths.get(sourceFile);
			
			String masterReportFile = reportFolder + File.separator + "VirtualAccount" + File.separator + "download-va-master.jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			String subReportFile = reportFolder + File.separator + "VirtualAccount" + File.separator + "download-transaction" + "-" + "va-" + locale.getLanguage() + ".jasper";
			JasperReport subReport = (JasperReport) JRLoader.loadObject(new File(subReportFile));
			
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("SUB_REPORT", subReport);
	        parameters.put("DATA",  beanCollection);
	        parameters.put("fileFormat", fileFormat);
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, parameters, new JRBeanCollectionDataSource(masterReportCollection));
			
			if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
				bytes = JasperExportManager.exportReportToPdf(print);
				
				//write files
				Files.write(destinationFile, bytes);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, sourceFile);
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {			
			throw new ApplicationException(e);
		}		
		
	}

	@Override
	public Map<String, Object> searchProductForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDtlId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			
			CorporateAccountGroupDetailModel corpAcctGrpDtl = corporateAccountGroupRepo.findDetailById(loginCorporateId, accountGroupDtlId);
			
			List<Object[]> result = vaRegistrationRepo.findProductByCorporateIdAndAccountNo(loginCorporateId, corpAcctGrpDtl.getCorporateAccount().getAccount().getAccountNo());
			
			resultMap.put("result", setModelToMap(result));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<Object[]> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (Object[] model : list) {
			
			resultList.add(setModelToMap(model));			
		}
		
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(Object[] model) {
		Map<String, Object> map = new HashMap<>();
		map.put("productCode", model[0]);
		map.put("productName", model[1]);
		
		return map;
	}
}