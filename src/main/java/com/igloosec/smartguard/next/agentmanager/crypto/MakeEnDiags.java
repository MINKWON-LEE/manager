/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.concurrents.NoSleepThread.java
 * @author : Je Joong Lee
 * created at : 2016. 1. 5.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.crypto;

import java.io.IOException;

/**
 * Created by osujin12 on 2016. 9. 8..
 */
public class MakeEnDiags {

    public static void main(String[] args) throws IOException {

        /**         
         * com.mobigen.snet.agentmanager.crypto.MakeEnDiags
         * 
         *
         * Run : /usr/local/snetManager/java/bin/java -cp  /usr/local/snetManager/manager/libs/AgentManager.jar:/usr/local/snetManager/manager/libs/* $srcFileName $dstPath
         * example : /usr/local/snetManager/java/bin/java -cp  /usr/local/snetManager/manager/libs/AgentManager.jar:/usr/local/snetManager/manager/libs/* com.mobigen.snet.agentmanager.crypto.MakeEnDiags /Users/osujin12/Documents/mobigen/S-Net/workspace/AgentManager/OutboundPrograms/Diags/unix/infra_solaris.class /Users/osujin12/Downloads
         */
        String src = args[0];
        String dstpath = args[1]+"/";
        try {
            new AESCryptography().e_program(src,dstpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
