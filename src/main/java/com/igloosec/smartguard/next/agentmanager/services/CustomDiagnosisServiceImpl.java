/**
 * project : AgentManager
 * package : com.mobigen.snet.agentmanager.services
 * company : Mobigen
 * 
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 21.
 * Description : 
 * 
 */
package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.component.CustomDiagComponent;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;

import com.igloosec.smartguard.next.agentmanager.entity.CustomValidation;
import com.igloosec.smartguard.next.agentmanager.entity.DscriptResultEntity;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagDataParseException;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagProcessingException;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagValidationException;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Project : AgentManager Package : com.mobigen.snet.agentmanager.services
 * Company : Mobigen File : CustomDiagnosisServiceImpl.java
 *
 * @author Hyeon-sik Jung
 * @Date 2017. 2. 21. Description :
 * 
 */
@Service
public class CustomDiagnosisServiceImpl extends AbstractManager implements CustomDiagnosisService {

	@Autowired
	private Dao dao;
	
	@Autowired
	private CustomDiagComponent customDiagComponent;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mobigen.snet.agentmanager.services.CustomDiagnosisService#
	 * processingCustomDiagData(com.mobigen.snet.agentmanager.entity.
	 * CustomDiagResultEntity)
	 */
	@Override
	public void processingCustomDiagData(JobEntity jobEntity) throws Exception {
		
		StringBuilder reason = new StringBuilder();
		try {
			String dir = INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.RECV, jobEntity.getJobType());
			String filePath = dir.concat(jobEntity.getFileName()).concat(jobEntity.getFileType());
			
			
			List<CustomValidation> resultList = customDiagComponent.valdiationCustomDiagFile(filePath);
			
			boolean passValidate = true;
			for(CustomValidation validation : resultList){
				if(!validation.isPass()){
					passValidate = false;
					reason.append(validation.getNo()+". "+validation.getReason()+", ");
				}
			}
			
			if(passValidate){
				
				DscriptResultEntity resultEntity = customDiagComponent.dataParseCustomDiagFile(filePath);

				List<String> ipAddress = resultEntity.getSystemInfoEntity().getListipAddress();

				if(ipAddress.size() > 1){
					List<String> ipAddressTemp = new ArrayList<String>();
					ipAddressTemp.add(ipAddress.get(0));
					resultEntity.getSystemInfoEntity().setListipAddress(ipAddressTemp);
				}

				List<String> assetCdList = dao.setDscriptAssetCd(resultEntity.getSystemInfoEntity());
				
				for(String assetCd : assetCdList){
					DscriptResultEntity customEntity = new DscriptResultEntity();
					customEntity.setAssetCd(assetCd);
					customEntity.setAuditDay(DateUtil.getCurrDate());
					customEntity.setProgramEntity(resultEntity.getProgramEntity());
					customEntity.setDiagnosis(resultEntity.getDiagnosis());

					dao.insertCustomDiagJob(customEntity);
					
					// 기존긴급진단 파일 삭제 ASSET_CD, PRG_ID, AUDIT_DAY 기준
					dao.deleteCustomDiagReport(customEntity);
					dao.insertCustomDiagReport(customEntity);
				}
			}else{
				throw new CustomDiagValidationException(reason.toString());
			}
			
		} catch (CustomDiagValidationException e) {
			throw new CustomDiagValidationException(e.getMessage(), e);
		} catch (CustomDiagDataParseException e) {
			throw new CustomDiagDataParseException(e.getMessage(), e);
		} catch (Exception e) {
			throw new CustomDiagProcessingException(e.getMessage(), e);
		}

	}
	@Override
	public void processingCustomDiagData(String filePath) throws Exception {
		StringBuilder reason = new StringBuilder();
		try {
			List<CustomValidation> resultList = customDiagComponent.valdiationCustomDiagFile(filePath);
			
			boolean passValidate = true;
			for(CustomValidation validation : resultList){
				if(!validation.isPass()){
					passValidate = false;
					reason.append("\n"+validation.getNo()+". "+validation.getReason());
				}
			}
			
			if(passValidate){
				
				DscriptResultEntity resultEntity = customDiagComponent.dataParseCustomDiagFile(filePath);
				List<String> assetCdList = dao.setDscriptAssetCd(resultEntity.getSystemInfoEntity());
				for(String assetCd : assetCdList){
					DscriptResultEntity customEntity = new DscriptResultEntity();
					customEntity.setAssetCd(assetCd);
					customEntity.setAuditDay(DateUtil.getCurrDate());
					customEntity.setProgramEntity(resultEntity.getProgramEntity());
					customEntity.setDiagnosis(resultEntity.getDiagnosis());

					dao.insertCustomDiagJob(customEntity);
					
					// 기존긴급진단 파일 삭제 ASSET_CD, PRG_ID, AUDIT_DAY 기준
					dao.deleteCustomDiagReport(customEntity);
					dao.insertCustomDiagReport(customEntity);
				}
			}else{
				throw new CustomDiagValidationException(reason.toString());
			}
		} catch (CustomDiagValidationException e) {
			throw new CustomDiagValidationException(e.getMessage(), e);
		} catch (CustomDiagDataParseException e) {
			throw new CustomDiagDataParseException(e.getMessage(), e);
		} catch (Exception e) {
			throw new CustomDiagProcessingException(e.getMessage(), e);
		}
	}

}
