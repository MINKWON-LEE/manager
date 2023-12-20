/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.ZipUtil.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 4. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.utils;


import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.sk.snet.manipulates.PatternMaker;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.util.ArrayList;

public class ZipUtil {

    private String zipFile;
    private String targetDir;
    PatterBuilder patterBuilder = new PatterBuilder();
    private String pENC_MAN = PatternMaker.ENCRPTION_PMANNER;

    
    public ZipUtil(){}

    public ZipUtil(String zipFile, String targetDir){
        this.zipFile = zipFile;
        this.targetDir = targetDir;
    }

    public void unzip() throws ZipException {
        ZipFile zipFile = new ZipFile(this.zipFile);
        zipFile.setPassword(pENC_MAN);
        zipFile.extractAll(targetDir);
    }

    public static void makeZip(ArrayList files , JobEntity jobEntity) throws Exception {

        String zipFileName =  INMEMORYDB.jobTypeAbsolutePath(INMEMORYDB.SEND, jobEntity.getJobType()) + jobEntity.getSOTP() + INMEMORYDB.ZIP;

        execute(files, zipFileName);

//        try {
//            //This is name and path of zip file to be created
//            ZipFile zipFile = new ZipFile(zipFileName);
//
//            //Add files to be archived into zip file
//
//
//            //Initiate Zip Parameters which define various properties
//            ZipParameters parameters = new ZipParameters();
//            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
//
//            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
//
//            //Set the encryption flag to true
//            parameters.setEncryptFiles(true);
//
//            //Set the encryption method to AES Zip Encryption
//            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
//
//            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
//
//            //Set password
//            parameters.setPassword(PatternMaker.ENCRPTION_PMANNER);
//
//            //Now add files to the zip file
//            zipFile.addFiles(files, parameters);
//        }
//        catch (ZipException e)
//        {
//            e.printStackTrace();
//        }
    }

    public static void makeZip(ArrayList files , String zipFileName) throws Exception {
        execute(files, zipFileName);
    }

    public static void execute(ArrayList files , String zipFileName){
        try {
            //This is name and path of zip file to be created
            ZipFile zipFile = new ZipFile(zipFileName);

            //Add files to be archived into zip file


            //Initiate Zip Parameters which define various properties
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            //Set the encryption flag to true
            parameters.setEncryptFiles(true);

            //Set the encryption method to AES Zip Encryption
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

            //Set password
            parameters.setPassword(PatternMaker.ENCRPTION_PMANNER);

            //Now add files to the zip file
            zipFile.addFiles(files, parameters);
        }
        catch (ZipException e)
        {
            e.printStackTrace();
        }
    }

    public static void makeZipWithoutPwd(ArrayList files , String zipFileName){
        try {
            //This is name and path of zip file to be created
            ZipFile zipFile = new ZipFile(zipFileName);

            //Add files to be archived into zip file


            //Initiate Zip Parameters which define various properties
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            //Now add files to the zip file
            zipFile.addFiles(files, parameters);
        }
        catch (ZipException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//        ArrayList<File> file = new ArrayList<File>();
//        file.add(new File("/usr/local/snetManager/AC20191002133134.dat"));
//        PatterBuilder patterBuilder = new PatterBuilder();
//        String pENC_MAN = PatternMaker.ENCRPTION_PMANNER;
//
//        execute(file, "/usr/local/snetManager/2616699760090165132.zip");
    }

}
