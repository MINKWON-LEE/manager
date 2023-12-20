/**
 * project : AgentManager
 * package : com.mobigen.snet.agentmanager.services
 * company : Mobigen
 * 
 * @author Hyeon-sik Jung
 * @Date   2017. 2. 13.
 * Description : 
 * 
 */
package com.igloosec.smartguard.next.agentmanager.component;

import com.igloosec.smartguard.next.agentmanager.entity.AssetIpDBEntity;
import com.igloosec.smartguard.next.agentmanager.entity.AssetMasterDBEntity;
import com.igloosec.smartguard.next.agentmanager.entity.AssetOpenPort;
import com.igloosec.smartguard.next.agentmanager.entity.GscriptResultEntity;
import com.igloosec.smartguard.next.agentmanager.entity.SwAuditDayDBEntity;
import com.igloosec.smartguard.next.agentmanager.exception.GetScriptException;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobFactory;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;

import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Project : AgentManager Package : com.mobigen.snet.agentmanager.services
 * Company : Mobigen File : GetScriptResult.java
 *
 * @author Hyeon-sik Jung
 * @Date 2017. 2. 13. Description :
 * 
 */
@Component
public class GetScriptResultComponent {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	public GscriptResultEntity getResultEntityToFile(File file) throws Exception {
		GscriptResultEntity resultEntity = new GscriptResultEntity();

		ArrayList<String> arrayList = new ArrayList<String>();
		String line = "";
		BufferedReader in = null;

		try {

			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			List<SwAuditDayDBEntity> auditDayDBEntities = new ArrayList<SwAuditDayDBEntity>();
			List<AssetIpDBEntity> assetIpDBEntities = new ArrayList<AssetIpDBEntity>();
			List<AssetOpenPort> openPorts = new ArrayList<AssetOpenPort>();

			// 2016. 05. 03 추가
			String swNm = "";

			while ((line = in.readLine()) != null) {
				if (line.indexOf("OS=") > -1)
					swNm = StringUtil.split(StringUtil.split(line, "=")[1], "^")[0].trim();
				arrayList.add(line);
			}

			HashSet<String> hs = new HashSet<String>(arrayList);
			ArrayList<String> list = new ArrayList<>(hs);
			list.remove(list.indexOf(ManagerJobFactory.BEGINELEMENT));
			list.remove(list.indexOf(ManagerJobFactory.ENDELEMENT));
			if (list.indexOf("") != -1) {
				list.remove(list.indexOf(""));
			}
			Iterator<String> itor = list.iterator();

			while (itor.hasNext()) {
				String str = itor.next();
				String[] strings = StringUtil.split(str, "=");
				String[] valueArr = {};
				try {
					if (strings[0].trim().equals("COMPANYNAME")) {
						valueArr[0] = strings[1];
					} else {
						valueArr = StringUtil.split(strings[1], ",");
					}
				} catch (IndexOutOfBoundsException e) {
				}

				if (valueArr.length > 1) {
					//==== value가 여러개인 경우 (코마로 구분돼서)
					HashSet<String> ths = new HashSet<String>(Arrays.asList(valueArr));
					ArrayList<String> horizontalDataList = new ArrayList<>(ths);

					if ("PORTTCP4".equals(strings[0])) {
						for (String portInfos : horizontalDataList) {
							AssetOpenPort assetOpenPort = new AssetOpenPort();
							assetOpenPort.setIpVersion(1);
							assetOpenPort.setOpenType(1);
							if (portInfos.contains("^")) {
								String[] portinfo = StringUtil.split(portInfos, "^");
								if (portinfo.length >= 2) {
									try {
										assetOpenPort.setOpenPort(Integer.valueOf(portinfo[0]));
										assetOpenPort.setProcessNm(portinfo[1].trim());
										assetOpenPort.setCdate(DateUtil.getCurrDate());
										
										SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
										swAuditDayDBEntity.setSwType(strings[0].trim());
										swAuditDayDBEntity.setSwNm(portinfo[0]);
										swAuditDayDBEntity.setSwInfo(portinfo[1]);
										swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
										auditDayDBEntities.add(swAuditDayDBEntity);
									} catch (Exception e) {
										logger.error("PORTTCP4:: {} :: Exception :: {}, {}", portinfo[0], e.getMessage(), e.getCause());
									}
								}
							} else {
								/*
								 * 2016-06-22 HP-UX ProcessNm 없을 경우 추가됨
								 */
								assetOpenPort.setOpenPort(Integer.valueOf(portInfos));
								assetOpenPort.setCdate(DateUtil.getCurrDate());

								SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
								swAuditDayDBEntity.setSwType(strings[0].trim());
								swAuditDayDBEntity.setSwNm(portInfos);
								swAuditDayDBEntity.setSwInfo("-");
								swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
								auditDayDBEntities.add(swAuditDayDBEntity);
							}
							// parsePortInfo(assetOpenPort, portInfos);
							openPorts.add(assetOpenPort);
						}

					} else if ("PORTTCP6".equals(strings[0])) {
						for (String portInfos : horizontalDataList) {
							AssetOpenPort assetOpenPort = new AssetOpenPort();
							assetOpenPort.setIpVersion(2);
							assetOpenPort.setOpenType(1);
							if (portInfos.contains("^")) {
								String[] portinfo = StringUtil.split(portInfos, "^");
								if (portinfo.length >= 2) {
									try {
										assetOpenPort.setOpenPort(Integer.valueOf(portinfo[0]));
										assetOpenPort.setProcessNm(portinfo[1].trim());
										assetOpenPort.setCdate(DateUtil.getCurrDate());
										
										SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
										swAuditDayDBEntity.setSwType(strings[0].trim());
										swAuditDayDBEntity.setSwNm(portinfo[0]);
										swAuditDayDBEntity.setSwInfo(portinfo[1]);
										swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
										auditDayDBEntities.add(swAuditDayDBEntity);
									} catch (Exception e) {
										logger.error("PORTTCP6:: {} :: Exception :: {}", portinfo[0], e.getMessage(),
												e.getCause());
									}
								}
							} else {
								/*
								 * 2016-06-22 HP-UX ProcessNm 없을 경우 추가됨
								 */
								assetOpenPort.setOpenPort(Integer.valueOf(portInfos));
								assetOpenPort.setCdate(DateUtil.getCurrDate());

								SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
								swAuditDayDBEntity.setSwType(strings[0].trim());
								swAuditDayDBEntity.setSwNm(portInfos);
								swAuditDayDBEntity.setSwInfo("-");
								swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
								auditDayDBEntities.add(swAuditDayDBEntity);
							}
							// parsePortInfo(assetOpenPort, portInfos);
							openPorts.add(assetOpenPort);
						}

					} else if ("PORTUDP4".equals(strings[0])) {
						for (String portInfos : horizontalDataList) {
							AssetOpenPort assetOpenPort = new AssetOpenPort();
							assetOpenPort.setIpVersion(1);
							assetOpenPort.setOpenType(2);
							if (portInfos.contains("^")) {
								String[] portinfo = StringUtil.split(portInfos, "^");
								if (portinfo.length >= 2) {
									try {
										assetOpenPort.setOpenPort(Integer.valueOf(portinfo[0]));
										assetOpenPort.setProcessNm(portinfo[1].trim());
										assetOpenPort.setCdate(DateUtil.getCurrDate());
										
										SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
										swAuditDayDBEntity.setSwType(strings[0].trim());
										swAuditDayDBEntity.setSwNm(portinfo[0]);
										swAuditDayDBEntity.setSwInfo(portinfo[1]);
										swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
										auditDayDBEntities.add(swAuditDayDBEntity);
									} catch (Exception e) {
										logger.error("PORTUDP4:: {} :: Exception :: {}", portinfo[0], e.getMessage(),
												e.getCause());
									}
								}
							} else {
								/*
								 * 2016-06-22 HP-UX ProcessNm 없을 경우 추가됨
								 */
								assetOpenPort.setOpenPort(Integer.valueOf(portInfos));
								assetOpenPort.setCdate(DateUtil.getCurrDate());

								SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
								swAuditDayDBEntity.setSwType(strings[0].trim());
								swAuditDayDBEntity.setSwNm(portInfos);
								swAuditDayDBEntity.setSwInfo("-");
								swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
								auditDayDBEntities.add(swAuditDayDBEntity);
							}
							// parsePortInfo(assetOpenPort, portInfos);
							openPorts.add(assetOpenPort);
						}

					} else if ("PORTUDP6".equals(strings[0])) {
						for (String portInfos : horizontalDataList) {
							AssetOpenPort assetOpenPort = new AssetOpenPort();
							assetOpenPort.setIpVersion(2);
							assetOpenPort.setOpenType(2);
							if (portInfos.contains("^")) {
								String[] portinfo = StringUtil.split(portInfos, "^");
								if (portinfo.length >= 2) {
									try {
										assetOpenPort.setOpenPort(Integer.valueOf(portinfo[0]));
										assetOpenPort.setProcessNm(portinfo[1].trim());
										assetOpenPort.setCdate(DateUtil.getCurrDate());
										
										SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
										swAuditDayDBEntity.setSwType(strings[0].trim());
										swAuditDayDBEntity.setSwNm(portinfo[0]);
										swAuditDayDBEntity.setSwInfo(portinfo[1]);
										swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
										auditDayDBEntities.add(swAuditDayDBEntity);
									} catch (Exception e) {
										logger.error("PORTUDP6:: {} :: Exception :: {}", portinfo[0], e.getMessage(),
												e.getCause());
									}
								}
							} else {
								/*
								 * 2016-06-22 HP-UX ProcessNm 없을 경우 추가됨
								 */
								assetOpenPort.setOpenPort(Integer.valueOf(portInfos));
								assetOpenPort.setCdate(DateUtil.getCurrDate());

								SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
								swAuditDayDBEntity.setSwType(strings[0].trim());
								swAuditDayDBEntity.setSwNm(portInfos);
								swAuditDayDBEntity.setSwInfo("-");
								swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
								auditDayDBEntities.add(swAuditDayDBEntity);
							}
							// parsePortInfo(assetOpenPort, portInfos);
							openPorts.add(assetOpenPort);
						}
						
					}else if( "BIN".equals(strings[0]) || "LIB".equals(strings[0]) ){
						//== 저 위에 없는 항목 (2017-03-15 홍순풍 추가)
						for (String thisStr : horizontalDataList) {
							SwAuditDayDBEntity swAuditDayDBEntity = setDefaultValueToAuditDayEntity(thisStr);
							if(swAuditDayDBEntity!=null){
								swAuditDayDBEntity.setSwType(strings[0].trim());
								auditDayDBEntities.add(swAuditDayDBEntity);
							}
						}
					} else if ( "LOG4J".equals(strings[0])) {
						int checkCnt = INMEMORYDB.useLog4JgetCnt;
						int idx = 0;
						for (String horizontalData : horizontalDataList) {
							if (checkCnt > 0) {
								if (idx > (checkCnt - 1)) {
									logger.debug("exceed log4j get cnt : " + checkCnt);
									break;
								}
							}
							SwAuditDayDBEntity swAuditDayDBEntity = getLog4JInfo(horizontalData);
							auditDayDBEntities.add(swAuditDayDBEntity);
							logger.debug("horizontalData <log4j> :  " + horizontalData);
							idx++;
						}
					}

					if ("INTERFACE".equals(strings[0])) {
						for (String horizontalData : horizontalDataList) {
							getIpDBEntity(assetIpDBEntities, horizontalData);
						}
					}

					for (String horizontalData : horizontalDataList) {
						SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
						String[] data = StringUtil.split(horizontalData, "^");
						if ("IP".equals(strings[0]) || "IP4".equals(strings[0]) || strings[0].equals("IP6")) {
							AssetIpDBEntity assetIpDBEntity = new AssetIpDBEntity();
							if (strings[0].endsWith("4")) {
								assetIpDBEntity.setIpVersion(1);
							} else if (strings[0].endsWith("6")) {
								assetIpDBEntity.setIpVersion(2);
							}
							assetIpDBEntity.setIpAddress(horizontalData);
							assetIpDBEntity.setIfNm("-");
							assetIpDBEntity.setIpV6Address("-");
							assetIpDBEntity.setMacAddress("-");
							assetIpDBEntity.setDefaultGw("-");
							if (!checkExistIp(assetIpDBEntities, horizontalData)) {
								assetIpDBEntities.add(assetIpDBEntity);
							}
						}
						if (data.length == 5 && !"BIN".equals(strings[0])
								&& !"INTERFACE".equals(strings[0]) && !"LOG4J".equals(strings[0])) {
							swAuditDayDBEntity.setSwType(strings[0].trim());
							swAuditDayDBEntity.setSwNm(data[0].trim());
							swAuditDayDBEntity.setSwInfo(data[1].trim());
							swAuditDayDBEntity.setSwDir(data[2].trim());
							swAuditDayDBEntity.setSwUser(data[3].trim());
							swAuditDayDBEntity.setSwEtc(data[4].trim());
							swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
							auditDayDBEntities.add(swAuditDayDBEntity);
						}
						// 클라우드 자산
						if (data.length == 9 && !"BIN".equals(strings[0])
								&& !"INTERFACE".equals(strings[0]) && !"LOG4J".equals(strings[0])) {
							swAuditDayDBEntity.setSwType(strings[0].trim());
							swAuditDayDBEntity.setSwNm(data[0].trim());
							swAuditDayDBEntity.setSwInfo(data[1].trim());
							swAuditDayDBEntity.setSwDir(data[2].trim());
							swAuditDayDBEntity.setSwUser(data[3].trim());
							swAuditDayDBEntity.setSwEtc(data[4].trim());
							swAuditDayDBEntity.setContainerId(data[5].trim());
							swAuditDayDBEntity.setContainerNm(data[6].trim());
							swAuditDayDBEntity.setPod(data[7].trim());
							swAuditDayDBEntity.setNameSpace(data[8].trim());
							swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
							auditDayDBEntities.add(swAuditDayDBEntity);
						}
					}
				} else {
					//==== value가 딸랑 한개인 경우
					SwAuditDayDBEntity dayDBEntity = new SwAuditDayDBEntity();

					AssetMasterDBEntity assetMasterDBEntity = resultEntity.getAssetMasterDBEntity();
					if (assetMasterDBEntity == null) {
						assetMasterDBEntity = new AssetMasterDBEntity();
					}
					if (strings[0].trim().equals("VENDOR")) {
						assetMasterDBEntity.setVendor(strings[1].trim());
					}
					if (strings[0].trim().equals("SERIAL")) {
						assetMasterDBEntity.setSerial(strings[1].trim());
					}
					if (strings[0].trim().equals("CPU")) {
						assetMasterDBEntity.setCpu(strings[1].trim());
					}
					if (strings[0].trim().equals("MEM")) {
						assetMasterDBEntity.setMem(strings[1].trim());
					}
					if (strings[0].trim().equals("DISK")) {
						assetMasterDBEntity.setDisk(strings[1].trim());
					}
					if (strings[0].trim().equals("INSTANCE")) {
						assetMasterDBEntity.setAssetRmk(strings[1].trim());
					}
					resultEntity.setAssetMasterDBEntity(assetMasterDBEntity);

					if ("INTERFACE".equals(strings[0])) {
						getIpDBEntity(assetIpDBEntities, strings[1].trim());
					}

					if (strings[0].equals("MANAGER")) {
						if (strings.length >= 2) {
							resultEntity.setManagerCd(strings[1].trim());
						}
					} else if (strings[0].equals("ASSETCD")) {
						if (strings.length >= 2) {
							resultEntity.setAssetCd(strings[1].trim());
						}
					} else if (strings[0].equals("OSARCH")) {
						if (strings.length >= 2) {
							if (strings[1].startsWith("3")) {
								resultEntity.setOsArch(1);
							} else if (strings[1].startsWith("6")) {
								resultEntity.setOsArch(2);
							}

						}
					} else if (strings[0].equals("cOTP")) {
						if (strings.length >= 2) {
							resultEntity.setcOtp(strings[1]);
						}
					} else if (strings[0].equals("IP") || strings[0].equals("IP4") || strings[0].equals("IP6")) {
						if (strings.length >= 2) {
							AssetIpDBEntity ipDBEntity = new AssetIpDBEntity();
							ipDBEntity.setIfNm("-");
							ipDBEntity.setIpV6Address("-");
							ipDBEntity.setMacAddress("-");
							ipDBEntity.setDefaultGw("-");

							if (strings[0].endsWith("4")) {
								ipDBEntity.setIpVersion(1);
							} else if (strings[0].endsWith("6")) {
								ipDBEntity.setIpVersion(2);
							}

							if (!strings[1].trim().equals("-") && !strings[1].trim().toUpperCase().equals("NONE")) {
								ipDBEntity.setIpAddress(strings[1].trim());
								if (!checkExistIp(assetIpDBEntities, strings[1].trim())) {
									assetIpDBEntities.add(ipDBEntity);
								}
							}
							if (strings[1].toUpperCase().equals("NONE") || strings[1].toUpperCase().equals("-")) {
								resultEntity.setConnectLog("COLLECTED IP IS 'NONE'");
							}
						}
					} else if (strings[0].trim().equals("COMPANYNAME")) {
						dayDBEntity.setSwType("LIB");
					} else {
						dayDBEntity.setSwType(strings[0].trim());
					}

					if (strings[0].trim().equals("LOG4J")) {
						if (!strings[1].trim().toLowerCase().equals("none")) {
							dayDBEntity = getLog4JInfo(strings[1].trim());
							auditDayDBEntities.add(dayDBEntity);
							logger.debug("horizontalData <log4j>: - " + resultEntity.getAssetCd() + " - " + strings[1].trim());
						}
					}

					if (strings[0].trim().equals("COMPANYNAME")) {
						// 머트리얼즈 전용 자산
						setExctraAssetInfo(dayDBEntity, strings[1].trim());
					} else if ("INTERFACE".equals(strings[0].trim()) || "LOG4J".equals(strings[0].trim())) {
						logger.debug("INTERFACE or LOG4J already checked....");
					} else {
						String[] tstrs = {};
						try {
							tstrs = StringUtil.split(strings[1], "^");
						} catch (ArrayIndexOutOfBoundsException e) {
						}

						if (tstrs.length == 1) {
							dayDBEntity.setSwInfo(tstrs[0].trim());
						}
						if (tstrs.length == 2) {
							dayDBEntity.setSwNm(tstrs[0].trim());
							dayDBEntity.setSwInfo(tstrs[1].trim());
						}

						/*
						 * 2016.11.17 다중 진단을 위해 추가적인 인자로 받음
						 */
						if (tstrs.length == 3) {
							dayDBEntity.setSwNm(tstrs[0].trim());
							dayDBEntity.setSwInfo(tstrs[1].trim());
							dayDBEntity.setSwDir(tstrs[2].trim());
						}
						if (tstrs.length == 4) {
							dayDBEntity.setSwNm(tstrs[0].trim());
							dayDBEntity.setSwInfo(tstrs[1].trim());
							dayDBEntity.setSwDir(tstrs[2].trim());
							dayDBEntity.setSwUser(tstrs[3].trim());
						}
						if (tstrs.length == 5) {
							dayDBEntity.setSwNm(tstrs[0].trim());
							dayDBEntity.setSwInfo(tstrs[1].trim());
							dayDBEntity.setSwDir(tstrs[2].trim());
							dayDBEntity.setSwUser(tstrs[3].trim());
							dayDBEntity.setSwEtc(tstrs[4].trim());
						}
						if (tstrs.length == 9) {
							dayDBEntity.setSwNm(tstrs[0].trim());
							dayDBEntity.setSwInfo(tstrs[1].trim());
							dayDBEntity.setSwDir(tstrs[2].trim());
							dayDBEntity.setSwUser(tstrs[3].trim());
							dayDBEntity.setSwEtc(tstrs[4].trim());
							dayDBEntity.setContainerId(tstrs[5].trim());
							dayDBEntity.setContainerNm(tstrs[6].trim());
							dayDBEntity.setPod(tstrs[7].trim());
							dayDBEntity.setNameSpace(tstrs[8].trim());
						}
					}

					// 주의: VENDOR, SERIAL, CPU, MEM, DISK 정보는 auditDayDBEntities에 들어가지 않는다.
					// snet_asset_master 에 들어가야 되는 정보.
					// swtype, swinfo, swdir, swuser, swetc 정보도 없다.
					dayDBEntity.setAuditDay(DateUtil.getCurrDate());
					if (dayDBEntity.getSwType() != null && dayDBEntity.getSwInfo() != null
							&& dayDBEntity.getSwNm() != null && dayDBEntity.getSwDir() != null
							&& dayDBEntity.getSwUser() != null && dayDBEntity.getSwEtc() != null) {
						if (dayDBEntity.getSwType().equals("LOG4J")) {
							logger.debug("INTERFACE or LOG4J already checked....");
						} else if (dayDBEntity.getSwType().equals("INSTANCE")) {
							logger.debug("INSTANCE-ID already checked....");
						} else {
							auditDayDBEntities.add(dayDBEntity);
						}
					}
				}
				//=== value가 딸랑 한개인 경우 - 끝.
				
				if (strings[0].equals("HOSTNAME")) {
					if (strings.length >= 2) {
						if (!strings[1].trim().toUpperCase().equals("NONE") && !strings[1].equals("-")) {
							AssetMasterDBEntity assetMasterDBEntity = resultEntity.getAssetMasterDBEntity();
							if (assetMasterDBEntity == null) {
								assetMasterDBEntity = new AssetMasterDBEntity();
							}
							assetMasterDBEntity.setHostNm(!strings[1].trim().isEmpty() ? strings[1].trim() : "-");
							resultEntity.setAssetMasterDBEntity(assetMasterDBEntity);
						} else {
							resultEntity.setConnectLog("COLLECTED HOST IS 'NONE'");
						}
					}
				}
				// 2016.05. 03 PROCESS list추가 됨
				if ("PROCESS".equals(strings[0])) {
					List<String> resultList = pressStringList(valueArr);
					for (String data : resultList) {
						SwAuditDayDBEntity swAuditDayDBEntity = new SwAuditDayDBEntity();
						swAuditDayDBEntity.setSwType("PR");
						swAuditDayDBEntity.setSwNm(swNm.trim()); // OS TYPE
						swAuditDayDBEntity.setSwInfo(data.trim());
						swAuditDayDBEntity.setAuditDay(DateUtil.getCurrDate());
						auditDayDBEntities.add(swAuditDayDBEntity);
					}
				}
				
				/* SHADOW 파일 추가 저장 */
				if ("SHADOW".equals(strings[0])) {
					if(strings[1] !=null && !strings[1].toUpperCase().equals("NONE"))
						resultEntity.setShadow(StringUtil.trimRight(strings[1]));
				}
			}

			resultEntity.setListSwAuditDay(auditDayDBEntities);
			resultEntity.setListAssetIp(assetIpDBEntities);

			resultEntity.setListassetOpenPort(openPorts);
			resultEntity.setCdate(DateUtil.getCurrDate());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GetScriptException(SnetCommonErrCode.ERR_0020.getMessage());
		} finally {
			CommonUtils.close(in);
		}

		return resultEntity;
	}
	
	
	private SwAuditDayDBEntity setDefaultValueToAuditDayEntity(String str){
		if(str.trim().length()==0){
			return null;
		}
		SwAuditDayDBEntity entity = new SwAuditDayDBEntity();
		String[] arr = str.split("\\^");
		if(arr.length==1){
			entity.setSwNm(arr[0]);
			return entity;
		}else if(arr.length>=2){
			entity.setSwNm(arr[0]);
			entity.setSwInfo(arr[1]);
			return entity;
		}else{
			return null;
		}
		
	}
	
	
	/**
	 * 중복 프로세스 정보 압축 하기 
	 * 프로세스 정보 길이 제한 50 
	 * ex) http.., http..., http,...=> http(3)
	 * 
	 * @param list
	 * @return
	 */
	private List<String> pressStringList(String[] list){
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<String> result = new ArrayList<>();
		for(String str : list){
			if(map.containsKey(str)){
				int value = map.get(str);
				value++;				
				map.put(str, value);
			}else{
				map.put(str, 1);
			}
		}
		
        for( Map.Entry<String, Integer> elem : map.entrySet() ){
        	
        	StringBuilder sb = new StringBuilder();
        	
        	if(elem.getKey().length() > 2000){
        		sb.append(elem.getKey().substring(0, 2000)+ "...");
        	}else{
        		sb.append(elem.getKey());
        	}
        	
        	if(elem.getValue()>1)
        		sb.append("("+elem.getValue()+")");
        	
        	result.add(sb.toString());
        }
        return result;
	}

	private void getIpDBEntity(List<AssetIpDBEntity> assetIpDBEntities, String data) {
		AssetIpDBEntity assetIpDBEntity = new AssetIpDBEntity();
		String[] ifs = StringUtil.split(data, "^");

		if (data.contains("None") && ifs.length == 1) {
			return;
		}

		if (ifs.length > 0) {
			assetIpDBEntity.setIfNm(ifs[0]);
		}
		if (ifs.length > 1) {
			assetIpDBEntity.setIpAddress(ifs[1]);
			deleteExistIp(assetIpDBEntities, ifs[1]);
		}
		assetIpDBEntity.setIpVersion(1);
		if (ifs.length > 2) {
			assetIpDBEntity.setIpV6Address(ifs[2]);
			deleteExistIp(assetIpDBEntities, ifs[2]);
		}
		if (ifs.length > 3) {
			assetIpDBEntity.setMacAddress(ifs[3]);
		}
		if (ifs.length > 4) {
			assetIpDBEntity.setDefaultGw(ifs[4]);
		}

		if (StringUtil.isEmpty(assetIpDBEntity.getIfNm())) {
			assetIpDBEntity.setIfNm("-");
		}
		if (StringUtil.isEmpty(assetIpDBEntity.getIpAddress())) {
			assetIpDBEntity.setIpAddress("-");
		}
		if (StringUtil.isEmpty(assetIpDBEntity.getIpV6Address())) {
			assetIpDBEntity.setIpV6Address("-");
		}
		if (StringUtil.isEmpty(assetIpDBEntity.getMacAddress())) {
			assetIpDBEntity.setMacAddress("-");
		}
		if (StringUtil.isEmpty(assetIpDBEntity.getDefaultGw())) {
			assetIpDBEntity.setDefaultGw("-");
		}

		assetIpDBEntities.add(assetIpDBEntity);
	}

	// LOG4J 관련 CVE-2021-44228 취약점
	private SwAuditDayDBEntity getLog4JInfo(String data) {
		SwAuditDayDBEntity dayDBEntity = new SwAuditDayDBEntity();
		String[] log4jInfo = StringUtil.split(data, "^");

		try {
			dayDBEntity.setSwType("LOG4J");

			if (log4jInfo.length > 0) {
				dayDBEntity.setSwNm(log4jInfo[0]);
			}
			if (log4jInfo.length > 1) {
				dayDBEntity.setSwInfo(log4jInfo[1]);
			}
			if (log4jInfo.length > 2) {
				dayDBEntity.setSwDir(log4jInfo[2]);
			}

			if(StringUtil.isEmpty(dayDBEntity.getSwNm())) {
				dayDBEntity.setSwNm("-");
			}
			if(StringUtil.isEmpty(dayDBEntity.getSwInfo())) {
				dayDBEntity.setSwInfo("-");
			}
			if(StringUtil.isEmpty(dayDBEntity.getSwDir())) {
				dayDBEntity.setSwDir("-");
			}

		} catch (ArrayIndexOutOfBoundsException e) {

			logger.error(" please recheck asset info <log4j> :  " + data);
		}

		return dayDBEntity;
	}

	private void deleteExistIp(List<AssetIpDBEntity> assetIpDBEntities, String ipAddr) {

		for (AssetIpDBEntity ipDBEntity : assetIpDBEntities) {
			if (!StringUtil.isEmpty(ipDBEntity.getIpAddress()) && ipDBEntity.getIpAddress().equals(ipAddr)) {
				assetIpDBEntities.remove(ipDBEntity);
				break;
			}
		}
	}

	private boolean checkExistIp(List<AssetIpDBEntity> assetIpDBEntities, String ipAddr) {
		boolean exist = false;
		for (AssetIpDBEntity ipDBEntity : assetIpDBEntities) {
			if (!StringUtil.isEmpty(ipDBEntity.getIpAddress()) && ipDBEntity.getIpAddress().equals(ipAddr)) {
				exist = true;
				break;
			}
		}

		return exist;
	}

	// 머트리얼즈 전용 자산
	private void setExctraAssetInfo(SwAuditDayDBEntity dayDBEntity, String data) {

		String[] swDbEntity = null;

		try {
			if (data.startsWith("WARN:")) {
				String info = "";
				String notIns = data.replaceAll("WARN:", "").trim();
				swDbEntity = notIns.split(" ");
				for(int i = 1; i < swDbEntity.length; i++) {
					info += swDbEntity[i] + " ";
				}
				swDbEntity[1] = info.trim();
				dayDBEntity.setSwNm(swDbEntity[0]);
				dayDBEntity.setSwInfo(swDbEntity[1]);
			} else {
				if (data.contains(":")) {
					swDbEntity = StringUtil.split(data, ":");
					dayDBEntity.setSwNm(swDbEntity[0]);
					dayDBEntity.setSwInfo(swDbEntity[1]);
				} else {
					swDbEntity = StringUtil.split(data, "^");
					dayDBEntity.setSwNm(swDbEntity[1]);
					dayDBEntity.setSwInfo(swDbEntity[0]);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {

			logger.error(" please recheck asset info :  " + data);
		}
	}
}
