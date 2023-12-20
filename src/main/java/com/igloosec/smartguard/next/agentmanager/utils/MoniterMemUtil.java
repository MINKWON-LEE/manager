/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.MoniterMemUtil.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 4. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import com.igloosec.smartguard.next.agentmanager.concurrents.OneTimeThread;
import com.igloosec.smartguard.next.agentmanager.entity.RunnigJobEntity;
import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.services.AbstractManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;

/**
 * 2020-06-15
 * AgnetManager의 현재 메모리 상태 보여주는 코드
 * 9876포트로 Listening
 * 이쪽으로 메세지를 보내는 서버는 없어보임. (WAS, SupportManager 모두 사용안함).
 */
public class MoniterMemUtil extends AbstractManager {

    BufferedReader br;
    BufferedWriter bw;


    public void moniterMem(){
        OneTimeThread worker = new OneTimeThread() {
            @Override
            public void task() throws Exception {
                ServerSocket serverSocket = null;

                try {

                    serverSocket = new ServerSocket(Integer.parseInt(INMEMORYDB.MONITER_PORT));

                } catch (IOException e) {
                    logger.error(CommonUtils.printError(e));

                }
                while (true) {
                    Socket socket = null;
                    try {

                        socket = serverSocket.accept();

                    } catch (IOException e) {
                        System.out.println("I/O error: " + e);
                        serverSocket.close();
                    }
                    

                    Socket finalSocket = socket;
                    OneTimeThread worker = new OneTimeThread() {
                        @Override
                        public void task() throws Exception {
                            try {
                                logger.debug("Welcome INMEMORYDB Moniter connected "+finalSocket.getInetAddress().toString());
                                while (true) {

                                    br = new BufferedReader(new InputStreamReader(finalSocket.getInputStream()));
                                    bw = new BufferedWriter(new OutputStreamWriter(finalSocket.getOutputStream()));

                                    String[] commnad = br.readLine().split(" ");

                                    RunnigJobEntity runnigJobEntity;
                                    String jobDate;
                                    String request;
                                    String msg = "";

                                    switch (commnad[0]){
                                        case "print":
                                            runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(commnad[1]);
                                            jobDate = runnigJobEntity.getJobDate();
                                            request = runnigJobEntity.getMsg();

                                            msg = "["+commnad[1]+"] ("+jobDate+") : "+request;
                                            sendMsg(msg);

                                            /** 객체로 전송 할 때**/
//                                    ObjectOutputStream oos = new ObjectOutputStream(finalSocket.getOutputStream());
//                                    oos.writeObject(runnigJobEntity);
                                            /***/
                                            break;

                                        case "printall":

                                            Set keyset = INMEMORYDB.RUNNINGDGJOBLIST.keySet();
                                            for(Iterator iter = keyset.iterator(); iter.hasNext();){
                                                String key = iter.next().toString();
                                                runnigJobEntity = INMEMORYDB.RUNNINGDGJOBLIST.get(key);

                                                jobDate = runnigJobEntity.getJobDate();
                                                request = runnigJobEntity.getMsg();

                                                msg = "["+key+"]("+jobDate+") : "+request;
                                                sendMsg(msg);
                                            }
                                            sendMsg("total = "+keyset.size());

                                            break;

                                        case "printallgs":

                                            Set keysetgs = INMEMORYDB.RUNNINGGSJOBLIST.keySet();
                                            for(Iterator iter = keysetgs.iterator(); iter.hasNext();){
                                                String key = iter.next().toString();
                                                runnigJobEntity = INMEMORYDB.RUNNINGGSJOBLIST.get(key);

                                                jobDate = runnigJobEntity.getJobDate();
                                                request = runnigJobEntity.getMsg();

                                                msg = "["+key+"]("+jobDate+") : "+request;
                                                sendMsg(msg);
                                            }
                                            sendMsg("total = "+keysetgs.size());

                                            break;

                                        case "printallst":

                                            Set keysetst = INMEMORYDB.RUNNINGSETUPJOBLIST.keySet();
                                            for(Iterator iter = keysetst.iterator(); iter.hasNext();){
                                                String key = iter.next().toString();
                                                runnigJobEntity = INMEMORYDB.RUNNINGSETUPJOBLIST.get(key);

                                                jobDate = runnigJobEntity.getJobDate();
                                                request = runnigJobEntity.getMsg();

                                                msg = "["+key+"]("+jobDate+") : "+request;
                                                sendMsg(msg);
                                            }
                                            sendMsg("total = "+keysetst.size());
                                            break;

                                        case "del":
                                            INMEMORYDB.RUNNINGDGJOBLIST.remove(commnad[1]);
                                            msg = "Success " + commnad[1] + " is DG remove jobList.";
                                            sendMsg(msg);
                                            break;

                                        case "delgs":
                                            INMEMORYDB.RUNNINGGSJOBLIST.remove(commnad[1]);
                                            msg = "Success " + commnad[1] + " is GS remove jobList.";
                                            sendMsg(msg);
                                            break;

                                        case "clear":
                                            INMEMORYDB.RUNNINGDGJOBLIST.clear();
                                            INMEMORYDB.RUNNINGGSJOBLIST.clear();
                                            INMEMORYDB.DIAGNOSISQUEUE.clear();

                                            msg = "All INMEMORYDB Delete Success.";
                                            sendMsg(msg);
                                            break;
                                        default:
                                            sendMsg("miss command type");
                                            break;
                                    }

                                    sendMsg("exit");
                                }

                            } catch (IOException e) {
//                        e.printStackTrace();
                            }finally {
                                logger.debug("connection close.");
                            }
                        }
                    };
                    worker.start();
                }                
            }
        };
        worker.start();

    }


    private void sendMsg(String msg){

        try {
            bw.write(msg);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            logger.error("INMEMORYDB moniter utill error...",e);
        }

    }

    public void finish() throws InterruptedException {


    }

}
