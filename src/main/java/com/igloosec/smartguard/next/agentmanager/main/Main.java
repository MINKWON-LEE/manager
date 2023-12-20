/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.main.main.java
 * company : Mobigen
 *
 * @author : Je Joong Lee
 * created at : 2016. 2. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.main;


import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.services.NotificationListener;
import com.igloosec.smartguard.next.agentmanager.utils.MoniterMemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 2020-06-15
 * AgnetManager의 현재 메모리 상태 보여주는 코드
 * 9876포트로 Listening
 * 이쪽으로 메세지를 보내는 서버는 없어보임. (WAS, SupportManager 모두 사용안함).
 */
public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);

    @SuppressWarnings("resource")
    public static void main(String args[]) {

        /**
         * [GENERAL NOTIFICATION LISTENER]
         * /usr/local/snetManager/java/bin/java -cp ../manager/libs/AgentManager.jar:../manager/libs/* com.mobigen.snet.agentmanager.main
         *
         * [WITHOUT NOTIFICATION LISTENER]
         * /usr/local/snetManager/java/bin/java -cp ../manager/libs/AgentManager.jar:../manager/libs/* com.mobigen.snet.agentmanager.main DEV
         *
         * ["10029" port NOTIFICATION LISTENER]
         * /usr/local/snetManager/java/bin/java -cp ../manager/libs/AgentManager.jar:../manager/libs/* com.mobigen.snet.agentmanager.main 10029
         *
         * **/

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:applicationContext.xml");

        if (args != null && args.length > 0 && "DEV".equals(args[0])) {
            INMEMORYDB.MONITER_PORT = "9877";
            logger.info("NO NOTI-LISTENER INIT.");
            new MoniterMemUtil().moniterMem();
            logger.debug("Init Memory Monitor ....");
        } else if (args != null && args.length > 0 && args[0].startsWith("1002")) {
            NotificationListener notificationListener = (NotificationListener) ctx.getBean("notificationListener");
            logger.info("START NOTI-LISTENER WITH PORT :" + args[0]);
            INMEMORYDB.MONITER_PORT = "9877";
            INMEMORYDB.LISTENER_PORT = args[0];
            notificationListener.initNotificationListener(Integer.parseInt(args[0]));

            //INMEMORYDB moniter util.

            new MoniterMemUtil().moniterMem();
            logger.debug("Init Memory Monitor ....");
        } else {
            NotificationListener notificationListener = (NotificationListener) ctx.getBean("notificationListener");
            notificationListener.initNotificationListener();
            new MoniterMemUtil().moniterMem();
            logger.debug("Init Memory Monitor ....");
        }
        logger.info("AgentManager Started.!");
    }

}
