/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 14.
 * description : 
 */
package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Hyeon-sik Jung
 *
 */
public class InitMemoryDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sysMsg = "Input agent.context.properites path :: ex) /usr/local/snetManager/";
		System.out.println(sysMsg);
		String path = "";
		String in =readTypeIn();
		
		if(in.length() >0 && in != null){
			path = in;
			System.out.println("init path :: "+in);
		}
		else{
			System.out.println("init path :: "+ path);
		}
		
//		reflection(loadPropertie(path));
	}
	
	static String readTypeIn(){
		BufferedReader br = null;
		String strIn = "";
		br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strIn= br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return strIn;
	}
	
	
	public static Properties loadPropertie(String path){
		Properties prop = null;
		String proFileName = "agent.context.properites";
		
		String propertie = "";
		
		if(path!=null&& path.length()>0)
			propertie = path + File.separator + proFileName;
		else
			propertie = proFileName;
		
		File file = new File(propertie);
		
		try{
			if(file.isFile()){
				System.out.println("Load Propertie File :: "+file.getAbsolutePath());
				prop = new Properties();
				InputStream is = new FileInputStream(file);
				// load a properties file
				prop.load(is);
				
			    Enumeration e = prop.propertyNames();

			    System.out.println("=========================[[Load properties]]=========================");
			    while (e.hasMoreElements()) {
			      String key = (String) e.nextElement();
			      System.out.println(key + "=" + prop.getProperty(key));
			    }
			    System.out.println("=========================[[Load properties]]=========================");
			}else{
				System.out.println("Not Exist!! Propertie File..");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return prop;
	}

	
	public static void reflection(INMEMORYDB memory, InputStream is){
		Field[] field = INMEMORYDB.class.getFields();
		try{
			Properties prop = new Properties();
			
			if(prop!=null){
				prop.load(is);
				
				for(Field f : field){
					if(f.getType().getTypeName().equals("java.lang.String") && prop.getProperty(f.getName()) !=null){
						f.set(memory, prop.getProperty(f.getName()));
					}
					if(f.getType().getTypeName().equals("int") && prop.getProperty(f.getName()) !=null){
						f.set(memory, Integer.parseInt(prop.getProperty(f.getName())));
					}
					if(f.getType().getTypeName().equals("boolean") && prop.getProperty(f.getName()) !=null){
						f.set(memory, Boolean.parseBoolean(prop.getProperty(f.getName())));
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
