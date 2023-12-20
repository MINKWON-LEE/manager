
/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.concurrents.NoSleepThread.java
 * @author : Je Joong Lee
 * created at : 2016. 1. 5.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.concurrents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SleepThread extends Thread {
    protected volatile boolean stop;
    Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean isFinishRequested() {
        return stop;
    }

    public void finish() throws InterruptedException {
        stop = true;
        interrupt();
        join();
    }

    @Override
    public void run() {
        while (!isFinishRequested()) {
            try {
                task();
                Thread.currentThread();
				Thread.sleep(1000);
            } catch (InterruptedException ie) {
                logger.warn("This NoSleepThread worker has been interrupted and asked to stop.", ie);
            } catch (Throwable e) {
                logger.error("The worker thread has thrown an Exception",e);
            }
        }
    }

    public abstract void task() throws Exception;

}