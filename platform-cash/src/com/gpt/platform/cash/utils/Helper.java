package com.gpt.platform.cash.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Component
public class Helper extends com.gpt.component.common.Helper {
	private static final Logger logger = LoggerFactory.getLogger(Helper.class);
	
	private static int stringBlockSize;
	
	@Value("${gpcash.string-block-size:2000}")
	public void setStringBlockSize(int size) throws Exception {
		stringBlockSize = size;
	}
	
	private static String applicationCode;
	
	@Value("${gpcash.active.application.code:GPCASHIB}")
	public void setActiveApplicationCode(String activeApplicationCode) throws Exception {
		applicationCode = activeApplicationCode;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <T> T loadHibernateObjectFromProxy(Object obj) throws Exception {
		Hibernate.initialize(obj);
		if (obj instanceof HibernateProxy) {
			return (T)((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
		}
		return (T)obj;
	}	
	
	public static List<String> chopItUp(String datum) throws ApplicationException{
		List<String> data = null;
		if ((datum != null) && (datum.length() > 0)) {
			data = new ArrayList<>();
			int index = 0;
			while ((datum.length() - index) > stringBlockSize) {
				String strBlock = datum.substring(index, index + stringBlockSize);
				data.add(strBlock);
				index += stringBlockSize;
			}
			data.add(datum.substring(index));
		}
		return data;
	}
	
	public static String glueStringChopsBackTogether(List<String> data) {
		if (data != null && !data.isEmpty()) {
			if(data.size() == 1)
				return data.get(0);
			
			int size = 0;
			
			for (int i = 0; i < data.size(); i++) {
				size += data.get(i).length();
			}
			
			StringBuilder value = new StringBuilder(size);
	
			for (int i = 0; i < data.size(); i++) {
				value.append(data.get(i));
			}
			
			return value.toString();
		}
		
		return "";
	}

	public static String generateHibernateUUIDGenerator() {
		return (String)new org.hibernate.id.UUIDHexGenerator().generate(null, null);
	}
	
	public static String generateTransactionReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return (new StringBuilder(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).append(String.valueOf(transactionNo)).toString()).replaceAll("-", "");
	}
	
	public static String generateShortTransactionReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return String.valueOf(transactionNo).replaceAll("-", "");
	}
	
	public static String generateReportReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return (new StringBuilder("R").append(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).append(String.valueOf(transactionNo)).toString()).replaceAll("-", "");
	}
	
	public static String generateBackOfficeReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return (new StringBuilder("B").append(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).append(String.valueOf(transactionNo)).toString()).replaceAll("-", "");
	}
	
	public static String generateCorporateReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return (new StringBuilder("C").append(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).append(String.valueOf(transactionNo)).toString()).replaceAll("-", "");
	}
	
	public static String generateCustomerReferenceNo() throws Exception {
		int transactionNo = ((DateUtils.getCurrentDateTime().getTime()) + generateHibernateUUIDGenerator()).hashCode();

		return (new StringBuilder("R").append(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).append(String.valueOf(transactionNo)).toString()).replaceAll("-", "");
	}
		
//	public static Date parseStringToDate(String source) throws Exception {
//		SimpleDateFormat sdf = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT);
//		return sdf.parse(source);
//	}
//	
	public static Timestamp parseStringToTimestamp(String source) throws Exception {
		return new Timestamp(DATE_TIME_FORMATTER.parse(source).getTime());
	}
//	
//	public static String formatDateToString(Date date) throws Exception {
//		SimpleDateFormat sdf = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT);
//		return sdf.format(date);
//	}
//	

	public static String getCorporateUserCode(String corporateId, String userId){
		return corporateId.concat(ApplicationConstants.DELIMITER_PIPE).concat(userId).toUpperCase();
	}
	
	public static String getCustomerUserCode(String cifId, String cifName){
		return cifId.concat(ApplicationConstants.DELIMITER_PIPE).concat(cifName).toUpperCase();
	}
	
	public static String getSearchWildcardValue(String value){
		if(!ValueUtils.hasValue(value)){
			value = ApplicationConstants.WILDCARD;
		} else {
			value = ApplicationConstants.WILDCARD.concat(value).concat(ApplicationConstants.WILDCARD);
		}
		
		return value;
	}
	
	public static String formatTimestampToString(Timestamp timestamp, String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new Date(timestamp.getTime()));
	}
	
	public static String getRandomPassword(int passwordLength)
	{
		int char_ = 65;
		int randomize = 0;
		StringBuffer password = new StringBuffer();
		
		for (int i = 0; i < passwordLength; i++) {
			if(i == 0) {
				char_ = 65 + (int)(Math.random() * 26.0D); //upper
			} else if(i == (passwordLength - 2)) {
				char_ = 48 + (int)(Math.random() * 10.0D); //number
			} else if(i == (passwordLength - 1)) {
				char_ = 35 + (int)(Math.random() * 4.0D); //special char
			}  else {
				randomize = (int)(Math.random() * 2.0D);
				
				switch (randomize) {
					case 0: 
						char_ = 65 + (int)(Math.random() * 26.0D); //upper
						break;
					case 1: 
						char_ = 97 + (int)(Math.random() * 26.0D); //lower
						break;
				}
			} 
			
			
			password.append((char)char_);
		}
		String newPassword = password.toString();
		
		if(logger.isDebugEnabled())
			logger.debug("random passwd : " + newPassword);
		
		return newPassword;
	}
	
	
	public static String getRandomString(int passwordLength)
	{
		int char_ = 65;
		int randomize = 0;
		StringBuffer password = new StringBuffer();
		
		for (int i = 0; i < passwordLength; i++) {
			randomize = (int)(Math.random() * 2.0D);
			
			switch (randomize) {
				case 0: 
					char_ = 65 + (int)(Math.random() * 26.0D); //upper
					break;
				case 1: 
					char_ = 48 + (int)(Math.random() * 10.0D); //number
					break;
			} 
			
			
			password.append((char)char_);
		}
		String newPassword = password.toString();
		
		if(logger.isDebugEnabled())
			logger.debug("random passwd : " + newPassword);
		
		return newPassword;
	}
	public static String generateHash(String str) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] data = str.getBytes(ApplicationConstants.CHARSET);
		data = digest.digest(data);
		return Base64.encodeBase64String(data);
	}
	
	public static boolean passwordHashEquals(String password, String heartBeat, String passwordHash) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] data = (password + heartBeat).getBytes(ApplicationConstants.CHARSET);
		data = digest.digest(data);
		return passwordHash.equals(Base64.encodeBase64String(data));
	}
	
	public static byte[] generateAESKey(String password, String heartBeat) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] data = (password + heartBeat).getBytes(ApplicationConstants.CHARSET);
		data = digest.digest(data);
		return Base64.encodeBase64(data);
	}
	
	public static String getRandomNumber(int length)
	{
		int char_ = 65;
		StringBuffer randomNumber = new StringBuffer();
		
		for (int i = 0; i < length; i++) {
			char_ = 48 + (int)(Math.random() * 10.0D); //number
			
			randomNumber.append((char)char_);
		}
		return randomNumber.toString();
	}
	
	public static void addToZipFile(String[] sourceFiles, String destinationFile, boolean deleteSourceFile) throws FileNotFoundException, IOException {
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		Path destinationPath = Paths.get(destinationFile);
        try {
            fos = new FileOutputStream(destinationPath.toFile());
            zos = new ZipOutputStream(fos);
            for(String sourceFile : sourceFiles) {
                Path sourcePath = Paths.get(sourceFile);
                ZipEntry zipEntry = new ZipEntry(sourcePath.getFileName().toString());
                zos.putNextEntry(zipEntry);
                
                byte[] bytes = Files.readAllBytes(sourcePath);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
                
                if (deleteSourceFile)
                	try {
                    	Files.delete(sourcePath);
                	} catch (Exception ex) {
                	}
            }
        } catch (Exception e) {
        	if (fos!=null)
        		try {
        			fos.close();
        		} catch (Exception ex) {
        		}

        	try {
            	Files.delete(destinationPath);
        	} catch (Exception ex) {
        	}
        	
        	throw e;
        } finally {
        	if (zos!=null)
        		try {
        			zos.close();
        		} catch (Exception ex) {
        		}
        	if (fos!=null)
        		try {
        			fos.close();
        		} catch (Exception ex) {
        		}
        }
	}
	
//	public static void main(String[] args) throws Exception {
//		String iosPublicKey = "-----BEGIN PUBLIC KEY-----\r\n" + 
//				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2F99QDjYYEIXbzOXGl6E\r\n" + 
//				"mcLGKYbREt1lCtqDdzH5JjJvVCkB1WSJtwhKBtsdj8MEKjPxKLDiOtlnOFa4oNG+\r\n" + 
//				"RTfCVLn5RDa//BGwYeQweJIISzzoZTfinRELT1cdqLMcEt0VvmaW/1HCZnjZ1fOr\r\n" + 
//				"pHLHXUTeFmLSG7yhU+7b+x+c0Rb16CJEK46Wuuw1F9EyOaLz/T5/k2s1C0Xh+eEf\r\n" + 
//				"cqiYfLXrVjq+C0WcOJ+/H/UM8lAyM+55QyMlxTCc2Og640867tmMHXn/Tu6xs/DC\r\n" + 
//				"3h2+oDqbXzZ7k63vkUwKr9QFCEoO+uHdbxV3Pjo8MLXEZOLYdXW60o4cbiCPJSWS\r\n" + 
//				"7QIDAQAB\r\n" + 
//				"-----END PUBLIC KEY-----";
//		
//		String publicKey = "-----BEGIN PUBLIC KEY-----\r\n" + 
//				"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJNfGnmb0aQtP5D5gEa3CTmWluTwjooZ\r\n" + 
//				"WcdA7VhcNk5R+1eSGOKwb0BY0X2mpiMJEtY14/drF6vXYL/+XHJI4qUCAwEAAQ==\r\n" + 
//				"-----END PUBLIC KEY-----";
//        String publicKeyContent = new String(publicKey).replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
//		byte[] publicKeyRaw = Base64.decodeBase64(publicKeyContent);
//
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKeyRaw);
//		
//		Signature sn = Signature.getInstance("SHA1withRSA");
//		sn.initVerify(kf.generatePublic(keySpecX509));
//		
//	}
	
	public static String getDownloadFileExtention(String fileFormat) {
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		}
		
		return "txt";
	}
	
	public static String getActiveApplicationCode() {
		return applicationCode;
	}
	
	public static BigDecimal getBigDecimalValue(Object value, BigDecimal defaultValue)
	{
		BigDecimal valueBig = null;
		if(value instanceof Integer) {
			valueBig = new BigDecimal((Integer) value);
		} else if(value instanceof String) {
			valueBig = new BigDecimal((String) value);
		} else if(value instanceof BigDecimal) {
			valueBig = (BigDecimal) value;
		}
		
		if(defaultValue != null && valueBig == null) {
			valueBig = defaultValue;
		}
			
		return valueBig;
	}
}
