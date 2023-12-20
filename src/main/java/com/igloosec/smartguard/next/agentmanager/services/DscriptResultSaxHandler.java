/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.services.DscriptResultSaxHandler.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 2. 29.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.entity.Diagnosis;
import com.igloosec.smartguard.next.agentmanager.entity.DscriptResultEntity;
import com.igloosec.smartguard.next.agentmanager.entity.ProgramEntity;
import com.igloosec.smartguard.next.agentmanager.entity.SummaryEntity;
import com.igloosec.smartguard.next.agentmanager.entity.SystemInfoEntity;
import jodd.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class DscriptResultSaxHandler extends DefaultHandler {
	public DscriptResultEntity result = new DscriptResultEntity();

	private Stack<String> elementStack = new Stack<String>();
	private Stack<Object> objectStack = new Stack<Object>();

	@Override
	public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
		this.elementStack.push(qName);

		if ("DIAGNOSIS_LIST".equals(qName)) {
			DscriptResultEntity dscriptResultEntity = new DscriptResultEntity();
			List<Diagnosis> list = new ArrayList<>();
			dscriptResultEntity.setDiagnosis(list);
			this.objectStack.push(dscriptResultEntity);
			this.result = dscriptResultEntity;
		} else if ("PROGRAM".equals(qName)) {
			this.objectStack.push(new ProgramEntity());
		} else if ("SYSTEMINFO".equals(qName)) {
			this.objectStack.push(new SystemInfoEntity());
		} else if ("SUMMARY".equals(qName)) {
			this.objectStack.push(new SummaryEntity());
		} else if ("DIAGNOSIS".equals(qName)) {
			this.objectStack.push(new Diagnosis());
		}
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		String value = new String(ch, start, length).trim();

		if (value.length() == 0) {
			return;
		}
		
		if ("HOST_NM".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setHostNm((systemInfoEntity.getHostNm() != null
					? systemInfoEntity.getHostNm() : "") + value);

		} else if ("ASSET".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setAsset((systemInfoEntity.getAsset() != null
					? systemInfoEntity.getAsset() : "") + value);

		} else if ("SCRIPT".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			value = value.replace("./", "").trim();
			systemInfoEntity.setScript((systemInfoEntity.getScript() != null
					? systemInfoEntity.getScript() : "") + value);

		} else if ("DATE".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setDate((systemInfoEntity.getDate() != null
					? systemInfoEntity.getDate() : "") + value);

		} else if ("TIME".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setTime((systemInfoEntity.getTime() != null
					? systemInfoEntity.getTime() : "") + value);

		} else if ("IP_ADDRESS".equals(currentElement().toUpperCase())
				&& "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			String[] strs = StringUtil.split(value, ",");
			ArrayList<String> ipAdresslist = new ArrayList<String>(Arrays.asList(strs));
			systemInfoEntity.setIpAddress(value);
			systemInfoEntity.setListipAddress(ipAdresslist);

		} else if("SW_NM".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity
					.setSwNm((systemInfoEntity.getSwNm() != null
							? systemInfoEntity.getSwNm() : "") + value);
			
		} else if("SW_TYPE".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity
					.setSwType((systemInfoEntity.getSwType() != null
							? systemInfoEntity.getSwType() : "") + value);
			
		} 
		else if("SW_INFO".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity
					.setSwInfo((systemInfoEntity.getSwInfo() != null
							? systemInfoEntity.getSwInfo() : "") + value);
		}
		else if("SW_DIR".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setSwDir((systemInfoEntity.getSwDir() != null ? systemInfoEntity.getSwDir() : "") + value);
		}
		else if("SW_USER".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setSwUser((systemInfoEntity.getSwUser() != null ? systemInfoEntity.getSwUser() : "") + value);
		}
		else if("SW_ETC".equals(currentElement().toUpperCase()) && "SYSTEMINFO".equals(currentElementParent().toUpperCase())) {
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) this.objectStack.peek();
			systemInfoEntity.setSwEtc((systemInfoEntity.getSwEtc() != null ? systemInfoEntity.getSwEtc() : "") + value);
		}
		else if ("VUL_COUNT".equals(currentElement().toUpperCase())
				&& "SUMMARY".equals(currentElementParent().toUpperCase())) {
			SummaryEntity summaryEntity = (SummaryEntity) this.objectStack.peek();
			summaryEntity.setVulCount((summaryEntity.getVulCount() != null
					? summaryEntity.getVulCount() : "") + value);

		} else if ("SCORE".equals(currentElement().toUpperCase())
				&& "SUMMARY".equals(currentElementParent().toUpperCase())) {
			SummaryEntity summaryEntity = (SummaryEntity) this.objectStack.peek();
			summaryEntity.setScore((summaryEntity.getScore() != null
					? summaryEntity.getScore() : "") + value);

		} else if ("VUL_ITEMLIST".equals(currentElement().toUpperCase())
				&& "SUMMARY".equals(currentElementParent().toUpperCase())) {
			SummaryEntity summaryEntity = (SummaryEntity) this.objectStack.peek();
			summaryEntity.setVulItemList((summaryEntity.getVulItemList() != null
					? summaryEntity.getVulItemList() : "") + value);

		} else if ("NOCHECK_ITEMLIST".equals(currentElement().toUpperCase())
				&& "SUMMARY".equals(currentElementParent().toUpperCase())) {
			SummaryEntity summaryEntity = (SummaryEntity) this.objectStack.peek();
			summaryEntity.setNochechItemList(
					(summaryEntity.getNochechItemList() != null
							? summaryEntity.getNochechItemList() : "") + value);

		} else if ("MANUALCHECK_ITEMLIST".equals(currentElement().toUpperCase())
				&& "SUMMARY".equals(currentElementParent().toUpperCase())) {
			SummaryEntity summaryEntity = (SummaryEntity) this.objectStack.peek();
			summaryEntity.setManualCheckItemList(
					(summaryEntity.getManualCheckItemList() != null
							? summaryEntity.getManualCheckItemList() : "") + value);
		}else if("PRG_NM".equals(currentElement().toUpperCase()) && "PROGRAM".equals(currentElementParent().toUpperCase())) {
			ProgramEntity programEntity = (ProgramEntity) this.objectStack.peek();
			programEntity.setPrgNm((programEntity.getPrgNm() != null ? programEntity.getPrgNm() : "") + value);
		} else if ("CODE".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setCode(
					(diagnosis.getCode() != null ? diagnosis.getCode() : "") + value);

		} else if ("ITEM_GROUP_NAME".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setItemGroupName(
					(diagnosis.getItemGroupName() != null ? diagnosis.getItemGroupName() : "") + value);

		} else if ("ITEM_NAME".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setItemName((diagnosis.getItemName() != null
					? diagnosis.getItemName() : "") + value);

		} else if ("ITEM_GRADE".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setItemGrade((diagnosis.getItemGrade() != null
					? diagnosis.getItemGrade() : "") + value);

		} else if ("STANDARD".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setStandard((diagnosis.getStandard() != null
					? diagnosis.getStandard() : "") + value);

		} else if ("STATUS".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setStatus(
					(diagnosis.getStatus() != null ? diagnosis.getStatus() : "") + value);

		} else if ("COUNTERMEASURE".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setCountermeasure((diagnosis.getCountermeasure() != null
					? diagnosis.getCountermeasure() : "") + value);

		} else if ("TIP".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setTip(
					(diagnosis.getTip() != null ? diagnosis.getTip() : "") + value);

		} else if ("RESULT".equals(currentElement().toUpperCase())
				&& "DIAGNOSIS".equals(currentElementParent().toUpperCase())) {
			Diagnosis diagnosis = (Diagnosis) this.objectStack.peek();
			diagnosis.setResult(
					(diagnosis.getResult() != null ? diagnosis.getResult() : "") + value);

		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		this.elementStack.pop();

		if ("SYSTEMINFO".equals(qName)) {
			Object object = this.objectStack.pop();
			SystemInfoEntity systemInfoEntity = (SystemInfoEntity) object;
			DscriptResultEntity dscriptResultEntity = (DscriptResultEntity) this.objectStack.peek();
			dscriptResultEntity.setSystemInfoEntity(systemInfoEntity);
		}

		if ("SUMMARY".equals(qName)) {
			Object object = this.objectStack.pop();
			SummaryEntity summaryEntity = (SummaryEntity) object;
			DscriptResultEntity dscriptResultEntity = (DscriptResultEntity) this.objectStack.peek();
			dscriptResultEntity.setSummaryEntity(summaryEntity);
		}

		if ("PROGRAM".equals(qName)) {
			Object object = this.objectStack.pop();
			ProgramEntity programEntity = (ProgramEntity) object;
			DscriptResultEntity dscriptResultEntity = (DscriptResultEntity) this.objectStack.peek();
			dscriptResultEntity.setProgramEntity(programEntity);
		}
		if ("DIAGNOSIS".equals(qName)) {
			Object object = this.objectStack.pop();
			Diagnosis diagnosis = (Diagnosis) object;
			DscriptResultEntity dscriptResultEntity = (DscriptResultEntity) this.objectStack.peek();
			dscriptResultEntity.getDiagnosis().add(diagnosis);
		}
	}

	private String currentElement() {
		return this.elementStack.peek();
	}

	private String currentElementParent() {
		if (this.elementStack.size() < 2)
			return null;
		return this.elementStack.get(this.elementStack.size() - 2);
	}

}
