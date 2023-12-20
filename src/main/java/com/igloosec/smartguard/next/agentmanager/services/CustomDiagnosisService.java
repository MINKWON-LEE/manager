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


import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;

/**
 * Project : AgentManager
 * Package : com.mobigen.snet.agentmanager.services
 * Company : Mobigen
 * File    : CustomDiagnosisService.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 21.
 * Description : 
 * 
 */
public interface CustomDiagnosisService {

	/**
	 * 진단 데이터 처리
	 * @param jobEntity
	 * @throws Exception
	 */
	public void processingCustomDiagData(JobEntity jobEntity) throws Exception;

	/**
	 * 진단 파일 
	 * @param filePath
	 * @throws Exception
	 */
	public void processingCustomDiagData(String filePath) throws Exception;
}
