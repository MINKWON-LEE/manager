/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.PtyAnalyzer.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 18.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.utils;


import com.sk.snet.manipulates.PatternMaker;

public class PatterBuilder {
	
	public PatternMaker patternMaker;
	
	public PatterBuilder(){		
		patternMaker = new PatternMaker();
	}
	
	
	public static String getTypeVal(){
		String retStr = "";
		
		String[] VARIDX = {"A","B","D","Q","S","R","C","E","T","F"};
		
		
		String mon = DateUtil.getMonth(DateUtil.getCurrDateByHour());
		
		int monInt = Integer.parseInt(mon);
		
		int monIntMod = monInt%10;
		
		System.out.println(monInt+" "+ monIntMod +" "+VARIDX[monIntMod]);
		
		retStr = VARIDX[monIntMod];
		
		return retStr;
	}
	
	public static String getKSPatterns(String type){
		String retStr = "";
		switch(type){
		case "A" :
			retStr = PatternMaker.KS_PMANNERA;
			break;
		case "B" :
			retStr = PatternMaker.KS_PMANNERB;
			break;
		case "D" :
			retStr = PatternMaker.KS_PMANNERDG;
			break;
		case "E" :
			retStr = PatternMaker.KS_PMANNERET;
			break;
		case "S" :
			retStr = PatternMaker.KS_PMANNERBS;
			break;
		default :
			retStr = PatternMaker.KS_PMANNER;
			
		}		
		return retStr;
		
		
	}
	
	
	public static String getEncPatterns(String type){
		String retStr = "";
		switch(type){
		case "R" :
			retStr = PatternMaker.ENCRPTION_PMANNERR;
			break;
		case "A" :
			retStr = PatternMaker.ENCRPTION_PMANNERA;
			break;
		case "B" :
			retStr = PatternMaker.ENCRPTION_PMANNERB;
			break;
		case "D" :
			retStr = PatternMaker.ENCRPTION_PMANNERD;
			break;
		case "C" :
			retStr = PatternMaker.ENCRPTION_PMANNERC;
			break;
		case "Q" :
			retStr = PatternMaker.ENCRPTION_PMANNERQ;
			break;
		default :
			retStr = PatternMaker.ENCRPTION_PMANNER;
			
		}		
		return retStr;
		
		
	}
	
	public static String getDecPatterns(String type){
		String retStr = "";
		switch(type){
		case "T" :
			retStr = PatternMaker.DEC_PMANNERTU;
			break;
		case "A" :
			retStr = PatternMaker.DEC_PMANNERAS;
			break;
		case "F" :
			retStr = PatternMaker.DEC_PMANNERDF;
			break;
		case "D" :
			retStr = PatternMaker.DEC_PMANNERDC;
			break;
		case "C" :
			retStr = PatternMaker.DEC_PMANNERDC;
			break;	
			
		default :
			retStr = PatternMaker.DEC_PMANNER;
			
		}		
		return retStr;
		
		
	}
	
	
	public static void main(String[] args){
		String argVar0 = "";
		String argVar1 = "";
		
		
		
		if(args.length == 1){
		    argVar0 = args[0];
		    argVar1 = args[1];
		 System.out.println("argVar0 :"+argVar0+", argVar1:"+argVar1);
		}
		
		PatterBuilder pb = new PatterBuilder();
		
		getTypeVal();
		
		System.out.println("KS Pattern "+argVar1+": "+ getKSPatterns(argVar1));
		
		System.out.println("KS Pattern A: "+ getKSPatterns("A"));
		System.out.println("KS Pattern B: "+ getKSPatterns("B"));
		System.out.println("KS Pattern E: "+ getKSPatterns("E"));
		System.out.println("KS Pattern D: "+ getKSPatterns("S"));
		System.out.println("KS Pattern !: "+ getKSPatterns("!"));
		
		
		System.out.println("Enc Pattern "+argVar1+": "+ getEncPatterns(argVar1));
		System.out.println("Enc Pattern A: "+ getEncPatterns("A"));
		System.out.println("Enc Pattern B: "+ getEncPatterns("B"));
		System.out.println("Enc Pattern C: "+ getEncPatterns("C"));
		System.out.println("Enc Pattern D: "+ getEncPatterns("D"));
		System.out.println("Enc Pattern Q: "+ getEncPatterns("Q"));
		
		
		System.out.println("Dec Pattern "+argVar1+": "+ getDecPatterns(argVar1));
		System.out.println("Dec Pattern T: "+ getDecPatterns("T"));
		System.out.println("Dec Pattern A: "+ getDecPatterns("A"));
		System.out.println("Dec Pattern C: "+ getDecPatterns("C"));
		System.out.println("Dec Pattern D: "+ getDecPatterns("D"));
		System.out.println("Dec Pattern F: "+ getDecPatterns("F"));
		
		
		
	}
	
	

}
