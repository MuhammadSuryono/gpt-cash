package com.gpt.product.gpcash.corporate.report.log.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class DownloadLogServiceImpl implements DownloadLogService {
	
	@Value("${gpcash.log.folder}")
	private String logFolder;
	
	@Value("${gpcash.temp.download.path}")
	private String pathDownload;
	Locale locale = LocaleContextHolder.getLocale();
	
	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map, String requestBy) throws ApplicationException {
		try {
			String type = (String) map.get("type");
			String logName = (String) map.get("logName");
			Date logDate = (Date) map.get("logDate");
			String fullPath = "";
			
			if(type.equals("today")) {
				fullPath = logFolder + logName;
			}else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				File folderHostFile = new File(logFolder.concat("/backup/").concat(sdf.format(logDate)).concat("/"));		
				File[] folderFiles = folderHostFile.listFiles();
				if (folderFiles.length>0) {
					List<String> localFileNameList = new ArrayList<>(folderFiles.length);		
					for (File file : folderFiles) {
							String filename = file.getName();					
							if (filename.contains(logName)){
								String fullFileName = logFolder + File.separator + "backup" + File.separator + sdf.format(logDate) + File.separator + filename;				        
						        localFileNameList.add(fullFileName);	
							} 				
					}
					
					if (ValueUtils.hasValue(localFileNameList)) {
						String[] sourceFiles = localFileNameList.toArray(new String[localFileNameList.size()]);
						String destinationFile = pathDownload + File.separator + logName.concat(sdf.format(logDate)).concat(".zip");
						Helper.addToZipFile(sourceFiles, destinationFile, false);
						
						fullPath = destinationFile;
					}
				}
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, fullPath);
			
			return result;
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
