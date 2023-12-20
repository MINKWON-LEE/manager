/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.NwConnectClient.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 4. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.entity.AgentInfo;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;
import com.igloosec.smartguard.next.agentmanager.exception.SnetCommonErrCode;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.services.NetworkSwitchManager;
import com.jcraft.jsch.JSchException;

import com.sshtools.ssh.SshSession;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NwConnectClient implements Runnable
{
    Logger logger = LoggerFactory.getLogger(getClass());

    TelnetClient tc = null;
    SSHCommon sshCommon = null;
    private SshSession session_sshtools = null;
    static String readLine = "";
    boolean end_loop = false;

    private String remoteip;
    private int remoteport;
    private String id;
    private String pw;
    private String ENTER = "\n";
    private AgentInfo agentInfo;
    private JobEntity jobEntity;
    private String channelType;

    @Autowired
    public NetworkSwitchManager networkswitch;

    public NwConnectClient(JobEntity jobEntity) {
        this.agentInfo = jobEntity.getAgentInfo();
        this.jobEntity = jobEntity;
        this.remoteip = agentInfo.getConnectIpAddress();
        this.remoteport = agentInfo.getUsePort();
        this.id = agentInfo.getUserIdRoot();
        this.pw = agentInfo.getPasswordRoot();
        this.channelType = agentInfo.getChannelType();
        this.tc = new TelnetClient();
    }

    public NwConnectClient(JobEntity jobEntity,SshSession session_sshtools) {
        this.agentInfo = jobEntity.getAgentInfo();
        this.jobEntity = jobEntity;
        this.remoteip = agentInfo.getConnectIpAddress();
        this.remoteport = agentInfo.getUsePort();
        this.id = agentInfo.getUserIdOs();
        this.pw = agentInfo.getPasswordOs();
        this.channelType = agentInfo.getChannelType();
        this.session_sshtools = session_sshtools;
    }

    public NwConnectClient(JobEntity jobEntity,TelnetClient telnetClient) {
        this.agentInfo = jobEntity.getAgentInfo();
        this.jobEntity = jobEntity;
        this.remoteip = agentInfo.getConnectIpAddress();
        this.remoteport = agentInfo.getUsePort();
        this.id = agentInfo.getUserIdOs();
        this.pw = agentInfo.getPasswordOs();
        this.channelType = agentInfo.getChannelType();
        this.tc = telnetClient;
    }

    @SuppressWarnings({ "static-access" })
	public boolean nwTelnetHandler() throws Exception {
        logger.debug("NW_TELNET_CONECTED TRY");

        try
        {
            tc.connect(remoteip, remoteport);

            Thread reader = new Thread(new NwConnectClient(jobEntity,tc));

            OutputStream outstr = tc.getOutputStream();
            int ret_read = 0;

            List<byte[]> command = makeNwCommand(agentInfo.getOsType());
            command.add(0, (id+ENTER).getBytes());
            command.add(1, (pw+ENTER).getBytes());
            try
            {
                reader.start();
                new Thread().sleep(1500);
                for (byte[] buff : command){
                    logger.debug("[send To Cisco] : "+new String(buff));
                    ret_read = buff.length;
                    outstr.write(buff, 0 , ret_read);
                    outstr.flush();
                    new Thread().sleep(5000);
                }

                end_loop = true;
            }
            catch (Exception e)
            {
                end_loop = true;
            }

            try
            {
                while (end_loop == false){}
                tc.disconnect();
                return true;
            }
            catch (Exception e)
            {
                logger.error(CommonUtils.printError(e));
                throw new Exception(SnetCommonErrCode.ERR_0005.getMessage());
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            CommonUtils.printError(e);
            throw new Exception(SnetCommonErrCode.ERR_0005.getMessage());
        }
    }

    @SuppressWarnings("static-access")
	public boolean nwSshHandler() throws Exception {

        sshCommon = new SSHCommon(id,pw,remoteip,remoteport,"sshtools","nw");

        session_sshtools = sshCommon.session_sshtools;

        logger.debug("NW_SSH_CONECTED TRY");
        Thread reader = new Thread(new NwConnectClient(jobEntity,session_sshtools));

        try {

            reader.start();

            OutputStream outstr = session_sshtools.getOutputStream();
            int ret_read = 0;
            List<byte[]> command = makeNwCommand(agentInfo.getOsType());

            try
            {
                new Thread().sleep(1500);
                for (byte[] buff : command){
                    logger.debug("[send To Cisco] : "+new String(buff));
                    ret_read = buff.length;
                    outstr.write(buff, 0 , ret_read);
                    outstr.flush();
                    new Thread().sleep(5000);
                }

                end_loop = true;
            }
            catch (Exception e)
            {
                end_loop = true;
            }

            try
            {
                while (end_loop == false){}
                sshCommon.session_sshtools.close();
                sshCommon.ssh.disconnect();
                return true;
            }
            catch (Exception e)
            {
                logger.error(CommonUtils.printError(e));
                throw new Exception(SnetCommonErrCode.ERR_0005.getMessage());
            }

        } catch (JSchException e) {
            logger.error(CommonUtils.printError(e));
            throw new Exception(SnetCommonErrCode.ERR_0005.getMessage());
        }

    }

    private List<byte[]> makeNwCommand(String vendor){

        String[] cmdList;
        List<byte[]> command = new ArrayList<>();


        switch (vendor) {
            case "Accedian":
                return command;
            case "Alcatel_Lucent":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Brocade":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Cisco":
//                command.add((id+ENTER).getBytes());
//                command.add((pw+ENTER).getBytes());
//                command.add("en\n".getBytes());
//                command.add((pw+ENTER).getBytes());
                command.add("terminal length 0\n".getBytes());

                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());

                for (String cmd : cmdList){
                    command.add((cmd+ENTER).getBytes());
                }

                command.add("exit\n".getBytes());

                return command;

            case "Cisco_NX_OS":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Cisco_IOS_XR":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Ddos_Peakflow":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Ddos_Pravail":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Extreme":
//                command.add((id+ENTER).getBytes());
//                command.add((pw+ENTER).getBytes());
                command.add(("disable clipaging"+ENTER).getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());

                for (String cmd : cmdList){
                    command.add((cmd+ENTER).getBytes());
                }

                command.add("exit\n".getBytes());
                command.add("N\n".getBytes());
                return command;

            case "FortiOS_5.6":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;


            case "FortiOS":
                command.add("a\n".getBytes());
                command.add("config global\n".getBytes());
                command.add("config system console\n".getBytes());
                command.add("set output standard\n".getBytes());
                command.add("end\n".getBytes());

                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Huawei":
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Juniper":
                command.add("set console page 0\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Juniper_SRX":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Netscreen":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Netgear_GS110TP":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Netgear_GSM7212":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            case "Passport":
                command.add("environment no more\n".getBytes());
                cmdList = splitCmd(agentInfo.getNwGetConfigCmd());
                for (String cmd : cmdList)
                    command.add((cmd+ENTER).getBytes());

                command.add("exit\n".getBytes());
                return command;

            default:
                return command;
        }
    }

    private String[] splitCmd(String cmdList){
        String[] ret = cmdList.split("\\^");
        ArrayList<String> arr = new ArrayList<>();
        boolean append = false;
        for(int i = 0; i < ret.length; i++){
            if ("".equals(ret[i])){
                append = true;
            }else if (append){
                append = false;
                arr.set(i-2, arr.get(i-2) + "^" + ret[i].trim());
            }else {
                arr.add(ret[i]);
            }
        }

        return arr.toArray(new String[0]);
    }

    public void run()
    {
        FileWriter fw = null;
        try {
            fw = new FileWriter(INMEMORYDB.NWCFG_RESULT_FILE_MANAGER_DIR + jobEntity.getFileName() + INMEMORYDB.CFG);
            BufferedWriter bw = new BufferedWriter(fw);
            InputStream instr = null;

            if (channelType.startsWith("T")){
                //Telnet 일때 input 받는것
                instr = tc.getInputStream();
            }else {
                //SSH 일때 input 받는것
                try {
                    instr = session_sshtools.getInputStream();
                } catch (IOException e) {
                    logger.error(CommonUtils.printError(e));
                } catch (Exception e) {
                    logger.error(CommonUtils.printError(e));
                }
            }


            byte[] buff = new byte[1024];
            int ret_read = 0;

            do
            {
                ret_read = instr.read(buff);
                if(ret_read > 0)
                {
                    readLine = new String(buff, 0, ret_read);
                    logger.debug("Recieve from NW : "+readLine);

                    bw.write(readLine);
                }
            }
            while (ret_read >= 0);

            if(bw != null) try{bw.close();}catch(IOException e){}
            if(fw != null) try{fw.close();}catch(IOException e){}

            end_loop = true;

        } catch (IOException e) {
            logger.error(CommonUtils.printError(e));
        }

    }

    public static void main(String[] args) throws Exception {
        @SuppressWarnings({ "unused", "resource" })
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:applicationContext.xml");

        JobEntity jobEntity = new JobEntity();
        jobEntity.setFileName("Extreme");
        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setConnectIpAddress("127.0.0.1");
        agentInfo.setUsePort(23);
        agentInfo.setOsType("Extreme");
        agentInfo.setUserIdOs("sdn");
        agentInfo.setPasswordOs("######");
        agentInfo.setNwGetConfigCmd("show version^show radius^show tacacs^show banner^show ssl^show snmpv3 community^show management^show policy detail^show edp^show iparp permanent^show iparp proxy^show bootprelay^show configuration detail");
        jobEntity.setAgentInfo(agentInfo);
    }


}
