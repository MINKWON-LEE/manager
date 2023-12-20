
package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by osujin12 on 2016. 7. 12..
 */
public class FileUtils {

    public static void fileCopy(String inFileName, String outFileName) throws Exception {
    	FileInputStream fis=null;
    	FileOutputStream fos = null;
        try {
            fis = new FileInputStream(inFileName);
            fos = new FileOutputStream(outFileName);

            int data = 0;
            while((data=fis.read())!=-1) {
                fos.write(data);
            }
            

        } catch (IOException e) {
            throw new Exception(SnetCommonErrCode.ERR_0010.getMessage());
        } finally {
        	fis.close();
            fos.close();
        }
    }
    
    public static void fileWrite(String outFileName, String outData) throws Exception {
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8");
            w.write(outData);
            w.close();
        } catch (IOException e) {
            throw new Exception(SnetCommonErrCode.ERR_0010.getMessage());
        }
    }
}
