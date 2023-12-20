/**
 * project : AgentManager
 * package : com.mobigen.snet.agentmanager.component
 * company : Mobigen
 * 
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
package com.igloosec.smartguard.next.agentmanager.component;

import com.igloosec.smartguard.next.agentmanager.entity.CustomValidation;
import com.igloosec.smartguard.next.agentmanager.entity.DscriptResultEntity;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagDataParseException;
import com.igloosec.smartguard.next.agentmanager.exception.CustomDiagValidationException;
import com.igloosec.smartguard.next.agentmanager.services.DscriptResultSaxHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : AgentManager
 * Package : com.mobigen.snet.agentmanager.component
 * Company : Mobigen
 * File    : CustomDiagComponent.java
 *
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 22.
 * Description : 
 * 
 */
@Component
public class CustomDiagComponent {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public List<CustomValidation> valdiationCustomDiagFile(String filePath) throws CustomDiagValidationException {
		
		List<CustomValidation> resultList = new ArrayList<CustomValidation>();
		try {
			
			File file = new File(filePath);
			
			int no =1;
			
			// 1. file size
			if (!file.isFile()) {
				resultList.add(new CustomValidation(no, false, "[FILE_NOT_EXIST] - "+ file.getCanonicalPath()));
				logger.warn("Diagnosis xml [FILE_NOT_EXIST] :: {}", file.getCanonicalPath());
			}
			
			no++;
			//2. file size
			if (file.length() < 200) {
				resultList.add(new CustomValidation(no, false, "[FILE_SIZE] - "+ file.length() +" byte"));
				logger.warn("Diagnosis xml [FILE_SIZE] :: {} byte", file.length());
			}
			
			no++;
			//3. XML Validation
			DocumentBuilderFactory xml = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = xml.newDocumentBuilder();
			xml.setValidating(true);
			if(!xml.isValidating())
				resultList.add(new CustomValidation(no, false, "[XML Validation Failed.]"));
			
			no++;

			Document xmlDoc = null;
			xmlDoc = parser.parse(file);
			Element root = xmlDoc.getDocumentElement();
			
			
			// 4. 주요 엘리먼트 있는 지 여부
			String[] validationElement = { "HOST_NM", "IP_ADDRESS", "PRG_NM"};
			
			for(String element : validationElement){
				NodeList nodeList = root.getElementsByTagName(element);
				logger.debug("element :: {}, length :: {}", element, nodeList.getLength());
				
				if(nodeList.getLength()!=1){
					resultList.add(new CustomValidation(no, false, "[필수 Element 체크] - "+ element));
				}
			}
			no++;
			
			// 5. 진단 코드 체크
			List<String> diagCodeList = new ArrayList<>();
			NodeList nodeList = root.getElementsByTagName("CODE");
			for(int i=0; i< nodeList.getLength(); i++){
				Node node = nodeList.item(i);
				if(node.getTextContent() !=null && !node.getTextContent().isEmpty()){
					
					//is unique diagnosis code ?
					if(diagCodeList.contains(node.getTextContent())){
						resultList.add(new CustomValidation(no, false, "[진단 코드 중복] - "+ node.getTextContent()));
					}else{
						diagCodeList.add(node.getTextContent());
					}
				}
			}
		} catch (Exception e) {
			throw new CustomDiagValidationException(e);
		}
		return resultList;
	}
	
	public DscriptResultEntity dataParseCustomDiagFile(String filePath) throws CustomDiagDataParseException {
		DscriptResultSaxHandler dscriptResultSaxHandler=null;
		InputStream xmlInput = null;
		try {
			File file = new File(filePath);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			xmlInput = new FileInputStream(file);
			SAXParser saxParser = factory.newSAXParser();
			dscriptResultSaxHandler = new DscriptResultSaxHandler();
			saxParser.parse(xmlInput, dscriptResultSaxHandler);

		} catch (Exception e) {
			throw new CustomDiagDataParseException(e);
		}finally {
			try {
				if(xmlInput != null){
					xmlInput.close();
				}
			} catch (IOException e) {
				throw new CustomDiagDataParseException(e);
			}
		}

		return dscriptResultSaxHandler.result;
	}
}
