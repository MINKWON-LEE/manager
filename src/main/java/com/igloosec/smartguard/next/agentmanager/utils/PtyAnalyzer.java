/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.PtyAnalyzer.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 18.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class PtyAnalyzer {

	/*** PTY Filtering Keys **/
//	public static String[] ptyIntPassPrase = {"207 200 163 58", "148 237 152 184 58" , "115 119 111 114 100 58 32"};
//	public static String[] ptyPassPhrase = {"Password: ","Password:","Password:", "password for","Password for"};
//	public static String[] ptyPromptPhrase = { "$ ","$", "# ","#", "> ",">", "% ", "%" ,"]# ","]#","]$ ","]$" ,"]","] "};
//	public static String[] ptyIntPromptPhrase = { "36 32","36", "35 32","35", "62 32","62", "37 32", "37" ,"93 35 32","93 35","93 36 32","93 36" ,"93","93 32"};
//	public static String[] ptyLoginPhrase = {"login: ","login:"};
//	public static String[] ptyLoginExclude = {"Last login:" , "incorrect", "fail" };
//	public static String[] ptySslConfirmPhrase = {"CONTINUE"};//CONTINUE CONNECTION (YES/NO)?  continue connecting (yes/no)?
//	public static String[] ptyIncorrect = {"incorrect" , "refuse" ,"can not","sorry", "Authentication failure", "unknown character"}; 
//	public static String[] ptyIntIncorrect = {"237 151 136 234 176 128 32 234 177 176 235 182 128 13 10"};//[허가 거부]
//	public static String[] ptyIncorrectExclude;
	
	public static String[] ptyPromptPhrase = INMEMORYDB.ptyPromptPhrase;
	public static String[] ptyIntPromptPhrase = INMEMORYDB.ptyIntPromptPhrase;
	public static String[] ptyPassPhrase = INMEMORYDB.ptyPassPhrase;
	public static String[] ptyIntPassPrase = INMEMORYDB.ptyIntPassPrase;
	public static String[] ptyLoginPhrase = INMEMORYDB.ptyLoginPhrase;
	public static String[] ptyLoginExclude = INMEMORYDB.ptyLoginExclude;
	public static String[] ptySslConfirmPhrase = INMEMORYDB.ptySslConfirmPhrase;
	public static String[] ptyIncorrect  = INMEMORYDB.ptyIncorrect;
	public static String[] ptyIntIncorrect = INMEMORYDB.ptyIntIncorrect;
	public static String[] ptyIncorrectExclude = INMEMORYDB.ptyIncorrectExclude;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	

	public String checkPty(String checkStr, String checkStrByLine, String checkIntStr, boolean promptChk){
		Random rand = new Random();
		int checkID = rand.nextInt(50) + 1;
		
	if(promptChk){//Only for the case of CMD : no need to check for the pty prompts
//		logger.debug("promptChk:"+promptChk);
		if(isPrompt(checkStr ,checkStrByLine,checkIntStr, checkID,promptChk)){			
			return "PROMPT";
		}	
	}
	else{
//		logger.debug("promptChk:"+promptChk);
		if(isLoginPty(checkStr , checkID)){			
			return "LOGIN";		
		}
		else if(isPassPty(checkStr , checkIntStr, checkID)){			
			return "PASSWORD";		
		}
		
		else if(isSSLConfirmPty(checkStr, checkID)){			
			return "SSLCONFRM";
		}
		else if (isPassIncorrectPty(checkStr, checkIntStr , checkID)) 
		{					
			return "INCORRECT";
		}
		else if(isPrompt(checkStr , checkStrByLine,checkIntStr, checkID,promptChk)){			
			return "PROMPT";
		}
		
	}
	return null;
}
	
	
	boolean isPassIncorrectPty(String phrase, String intPhrase, int checkID){
		boolean yn = false;
		phrase = phrase.toUpperCase();
		
		for(String chkStr : ptyIncorrect){
			if(phrase.contains(chkStr.toUpperCase())){
				logger.debug("["+checkID+"] FOUND INCORRECT PASSWORD/ID WITHIN :\n ["+ phrase +"]\n");
				
				//Check for Exceptional case
				for(String exStr : ptyIncorrectExclude){
					if(phrase.contains(exStr.toUpperCase())){
						logger.debug("["+checkID+"] FOUND INCORRECT BUT TO IGNORE :\n ["+ phrase +"]\n");
						yn = false;
					}
					else{
						yn = true;
						break;
					}					
				}
				if(yn)break;
				
			}	
		}
		//Check for Non-English Messages
		if(!yn){
			for(String chkStr : ptyIntIncorrect){
				if(intPhrase.contains(chkStr)){
					yn = true;
					logger.debug("["+checkID+"]FOUND INCORRECT PHRASE INT WITHIN :\n["+ phrase+"] \n");
					break;
				}	
			}	
		}
		
		return yn;
	}
	
	boolean isSSLConfirmPty(String phrase, int checkID){
		boolean yn = false;
		phrase = phrase.toUpperCase();
		for(String chkStr : ptySslConfirmPhrase){
			if(phrase.contains(chkStr.toUpperCase()) && phrase.contains("?")){				
				logger.debug("["+checkID+"] FOUND SSL CONFIRM WITHIN : \n ["+ phrase +"] \n" );  				
				yn = true;
				break;
			}	
		}
		if(!yn){
//			logger.debug("["+checkID+"][checked SSLConfirm]");
		}
		return yn;
	}
	
	boolean isLoginPty(String phrase, int checkID){
		boolean yn = false;
		phrase = phrase.toUpperCase();
		
		//logger.debug("Checking Login Phrase for '"+ phrase+"'");
		for(String chkStr : ptyLoginPhrase){
			if(phrase.endsWith(chkStr.toUpperCase())){ 
				   for(String excld:ptyLoginExclude){
					   if(phrase.toUpperCase().contains(excld.toUpperCase())){
						yn = false;
					    return false;
					   	}
				   	}					
				yn = true;
				logger.debug("["+checkID+"] FOUND LOGIN WITHIN : \n["+ phrase+"] \n");
				break;
			}	
		}
		if(!yn){
//			logger.debug("["+checkID+"][checked login]");
		}
		
		return yn;
	}
	
	boolean isPassPty(String phrase , String intPhrase, int checkID){
		boolean yn = false;
		phrase = phrase.toUpperCase();
		//logger.debug("Checking Pass Phrase for '"+ phrase+"'");
		for(String chkStr : ptyPassPhrase){
			if(phrase.endsWith(chkStr.toUpperCase())){
				yn = true;
				logger.debug("["+checkID+"]FOUND PASS_STR WITHIN :\n["+ phrase+"] \n");
				break;
			}	
		}
		if(!yn){
			for(String chkStr : ptyIntPassPrase){
				if(intPhrase.contains(chkStr)){
					yn = true;
					logger.debug("["+checkID+"]FOUND PASS_INT WITHIN :\n["+ phrase+"] \n");
					break;
				}	
			}	
		}
		if(!yn){
//			logger.debug("["+checkID+"][checked Password]");
		}
		return yn;
	}
	
	boolean isPrompt(String phrase, String checkStrByLine, String checkIntStr, int checkID, boolean promptChk){
		boolean yn = false;	
//		logger.debug(">>>>===================================================");
//		logger.debug("RECV prompt Str :==="+ phrase+"===");
//		logger.debug("RECV prompt StrByLine :==="+checkStrByLine+"===");
//		logger.debug("RECV prompt StrINT :===" + checkIntStr+"===");
//		logger.debug("===================================================<<<<");
//		if(phrase.endsWith(" ")){phrase = phrase.replaceAll(" ", ""); logger.debug("replaced."+phrase);}
//		phrase = phrase.replaceAll("\n", "");
//		phrase = phrase.replaceAll("\r", "");
		for(String chkStr : ptyPromptPhrase){
			boolean mPhr = phrase.endsWith(chkStr);
			boolean mSbl = checkStrByLine.endsWith(chkStr);
			if(mPhr){
				yn = true;
				logger.debug("FOUND [PROMPT "+chkStr+"] WITHIN :'"+ phrase+"'");
				break;
			}else if(mSbl){
				yn = true;
				logger.debug("FOUND [PROMPT BYLINE "+chkStr+"] WITHIN :'"+checkStrByLine+"'");				
				break;
			}
		}
		if(!yn && !promptChk){			
			checkIntStr = handleEscp(checkIntStr);
			checkIntStr = handleLowBytes(checkIntStr);
				for(String chkStr : ptyIntPromptPhrase){
					if(checkIntStr.endsWith(chkStr)){
						yn = true;
						logger.debug("["+checkID+"]FOUND PROMPT_INT WITHIN :\n["+ phrase+"] \n");
						break;
					}	
				}
		}
		return yn;
	}
	
	public String handleLowBytes(String intStrT){
//		"32 13 10 27 91 49 109 69 83 68 66 58 126 32 35 32 27 91 109 15 "
		
		String returnStr = "";
		String[] temp =  StringUtil.split(intStrT, " ");
		for(String c :temp){
			try{				
			int	k = Integer.parseInt(c);
			if(k <= 31){				
			}else{
				returnStr=returnStr+" "+c;	
			}
			}catch(Exception e){}
		}
		return returnStr;
	}
	
	public String handleEscp(String intStrT){
//		String intStrT = "13 10 27 91 49 59 51 49 109 47 114 111 111 116 62 27 91 48 109 ";
		try{
		
			int lastIdxs = intStrT.lastIndexOf("27");
			int lastIdxe = intStrT.lastIndexOf("109");
			String igStr = "";
//			logger.debug("27 start idx:"+lastIdxs +"  109 end idx(lastIdxe)"+lastIdxe);
			if(lastIdxe > lastIdxs){
			  igStr = intStrT.substring(lastIdxs, lastIdxe+3);
//			  logger.debug("igStr "+igStr);
			  intStrT = intStrT.replaceAll(igStr, "");
			  intStrT = intStrT.replaceAll("  ", " ");
			}
			logger.debug("FOUND ESCAPE TO CURR END: "+intStrT);
//			if(lastIdxs >0 ){
//				logger.debug("FOUND ESCAPE TO CURR END: "+intStrT.substring(lastIdxs));
//			}
			
			
			
		}catch(Exception e){
			logger.debug("DID NOT PERFORMED ESCAPE HANDLE." + e.getMessage());
		}
		
				
		
		return intStrT.trim();
	}
	
	
//	public static void main(String args[]){
//		PtyAnalyzer pt = new PtyAnalyzer();
//		String input = "32 13 10 27 91 49 109 69 83 68 66 58 126 32 35 32 27 91 109 15 ";
//		System.out.println("'"+input+"'");
//		input = pt.handleEscp(input);
//		System.out.println("'"+input+"'");
//		input = pt.handleLowBytes(input);
//		System.out.println("'"+input+"'");
		
//		
//		PtyAnalyzer pt = new PtyAnalyzer();
//		Logger logger = LoggerFactory.getLogger(pt.getClass());
//		int[] arr = {32,13,10,27,93,48,59,114,111,111,116,64,73,66,67,95,84,66,95,67,77,48,49,58,126,7,27,91,63,49,48,51,52,104,27,91,49,59,51,50,109,91,73,66,67,95,84,66,95,67,77,48,49,95,64,114,111,111,116,58,126,93,36,27,91,48,59,51,55,109,32};
//		
//		StringBuffer appendBuffStr = new StringBuffer();
//		
//		for(int i : arr){
//			char c = (char)i;
//			System.out.println(i+" "+c);
//			appendBuffStr.append(c);
//		}
//		
//		logger.debug(appendBuffStr.toString());
//		
//		String phrase = "";
//		
//		System.out.println("ssssssss");
//		String ss = pt.handleEscp("32 13 10 13 10 35 35 35 32 32 35 35 35 35 35 35 32 32 32 32 35 35 35 35 35 32 32 32 35 35 35 35 35 32 32 32 35 35 35 32 32 32 32 32 32 32 32 32 32 32 32 35 35 35 35 35 35 35 32 32 35 32 32         32 32 32 35 32 32 35 35 35 35 35 35 32 32 32 13 10 32 35 32 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 32 35 32 32 32 35 32 32 32 32 32 32 32 32 32 32 32         35 32 32 32 32 32 35 32 32 35 35 32 32 32 35 35 32 32 35 32 32 32 32 32 35 32 32 13 10 32 35 32 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 32 32 32 35 32 32 32 32 32 32 35 32 32 32         32 32 32 32 32 32 32 32 32 32 32 32 35 32 32 32 32 32 35 32 32 35 32 35 32 35 32 35 32 32 35 32 32 32 32 32 35 32 32 13 10 32 35 32 32 32 35 35 35 35 35 35 32 32 32 35 32 32 32 32 32 32 32         32 35 35 35 35 35 32 32 35 35 35 35 32 32 32 35 35 35 35 35 35 35 32 32 35 32 32 32 32 32 35 32 32 35 32 32 35 32 32 35 32 32 35 35 35 35 35 35 32 32 32 13 10 32 35 32 32 32 35 32 32 32 32         32 35 32 32 35 32 32 32 32 32 32 32 32 32 32 32 32 35 32 32 35 32 32 32 35 32 32 32 32 32 32 32 32 32 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 32 32 32         13 10 32 35 32 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 35 32 32 32 32 32 32 35 32 32 35 32 32 32 35 32 32 32 32 32 32 32 32 32 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32         35 32 32 35 32 32 32 32 32 32 32 32 13 10 35 35 35 32 32 35 35 35 35 35 35 32 32 32 32 35 35 35 35 35 32 32 32 35 35 35 35 35 32 32 32 35 35 35 32 32 32 32 32 32 32 32 32 32 32 32 35 35 35         35 35 35 35 32 32 35 32 32 32 32 32 35 32 32 35 32 32 32 32 32 32 32 32 13 10 13 10 27 93 48 59 114 111 111 116 64 73 66 67 53 54 45 79 77 80 58 126 7 27 91 63 49 48 51 52 104 27 91 49 59         51 50 109 91 73 66 67 53 54 45 79 77 80 95 64 114 111 111 116 58 126 93 36 27 91 48 59 51 55 109 32 ");
//		System.out.println("+"+ss+"+");
//				 
//		
//		
//		
//	}
	
			
	
}
