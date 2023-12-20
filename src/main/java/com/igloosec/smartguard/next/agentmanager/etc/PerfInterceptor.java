/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils
 * company : Mobigen
 * @author : Hyeon-sik Jung
 * created at : 2016. 4. 1.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.etc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PerfInterceptor implements MethodInterceptor {

    Logger logger = LoggerFactory.getLogger("HealthCheckLog");


    /*
     * (non-Javadoc)
     *
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.
     * intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation method) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            started(method.getMethod().getName(), start);
            return method.proceed();
        } finally {
            terminated(method.getMethod().getName(), start);
        }
    }

    private void started(String methodName, long start) {
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String str = dayTime.format(new Date(start));

        long end = System.currentTimeMillis();
        String endStr = dayTime.format(new Date(end));
        long duration = end - start;

        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================================================================\n")
                .append("Started Method:: {},  ")
                .append("Started:: {}")
                .append("\n====================================================================================================================================================\n");

        logger.debug(sb.toString(), methodName, str, (double) duration / 1000, duration, endStr);
    }

    private void terminated(String methodName, long start) {
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String str = dayTime.format(new Date(start));

        long end = System.currentTimeMillis();
        String endStr = dayTime.format(new Date(end));
        long duration = end - start;

        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================================================================\n")
                .append("Terminated Method:: {}, ")
                .append("Started :: {}, ")
                .append("Duration :: {}s ({} ms), ")
                .append("Terminated ::{}")
                .append("\n====================================================================================================================================================\n");

        logger.debug(sb.toString(), methodName, str, (double) duration / 1000, duration, endStr);
    }


}
