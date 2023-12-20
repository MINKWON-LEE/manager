/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.exception.AbstractSnetException.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 5. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.exception;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;

import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class AbstractSnetException extends Exception {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String code;
    private String message;
    private Dao dao;
    private JobEntity jobEntity;
    private String flag;

    private AbstractSnetException(String message) {
        super(message);
        this.message = message;
    }

    public AbstractSnetException(String message, boolean isErr) {
        super(message);
        this.message = message;
    }

    protected AbstractSnetException(String code, String message) {
        this(message);
        this.code = code;
    }

    private AbstractSnetException(String message, Throwable err) {
        super(message, err);
        this.message = message;
    }

    protected AbstractSnetException(String code, String message, Throwable err) {
        this(message, err);
        this.code = code;
    }
    protected AbstractSnetException(ErrCodable errCodable, String...args) {
        this(errCodable.getErrCode(), errCodable.getMessage(args));
    }

    protected AbstractSnetException(Dao dao, String args, JobEntity jobEntity, String flag) throws Exception {
        this(args);
        this.dao = dao;
        this.jobEntity = jobEntity;
        this.flag = flag;
        excuteException(this.message);
    }

    protected AbstractSnetException(Dao dao, ErrCodable errCodable, String args, JobEntity jobEntity, String flag) throws Exception {
        this(errCodable.getErrCode(), errCodable.getMessage(args));
        this.dao = dao;
        this.jobEntity = jobEntity;
        this.flag = flag;

        excuteException(this.message);
    }

    protected AbstractSnetException(Dao dao, ErrCodable errCodable, JobEntity jobEntity, String flag, String... args) throws Exception {
        this(errCodable.getErrCode(), errCodable.getMessage(args));
        this.dao = dao;
        this.jobEntity = jobEntity;
        this.flag = flag;

        excuteException(this.message);
    }

    private void excuteException(String msg) throws Exception {
        if("G".equals(flag)){
            jobEntity.getAgentInfo().setConnectLog(msg+" ["+ DateUtil.getCurrDateBySecondFmt()+"]");
            updateConnectMaster(jobEntity);
            jobEntity.setAgentJobFlag(4);
            jobEntity.setAgentJobDesc(msg);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
            dao.updateAgentGetJobHistory(jobEntity);

            logger.debug("snet GS Expection 처리 완료 ==> " +msg);

        } else if ("D".equals(flag)) {
            jobEntity.setAgentJobFlag(4);
            jobEntity.setAgentJobDesc(msg);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
            updateAgentJobHistory(jobEntity);
            logger.debug("snet DG Expection 처리 완료 ==> "+msg);
        } else if ("I".equals(flag)) {
            jobEntity.getAgentInfo().setAgentRegiFlag(3);
            jobEntity.getAgentInfo().setSetupStatus(msg+" ["+ DateUtil.getCurrDateBySecondFmt()+"]");

            dao.updateAgentSetupStatus(jobEntity.getAgentInfo());

            logger.debug("snet Setup Expection 처리 완료 ==> "+msg);
        } else if ("C".equals(flag)) {
            jobEntity.getAgentInfo().setSetupStatus(msg+" ["+ DateUtil.getCurrDateBySecondFmt()+"]");

            dao.updateAgentSetupStatus(jobEntity.getAgentInfo());

            logger.debug("snet Setup Expection 처리 완료 ==> "+msg);
        } else if ("E".equals(flag)){
            jobEntity.setAgentJobFlag(3);
            jobEntity.setAgentJobDesc(msg);
            jobEntity.setAgentJobEDate(DateUtil.getCurrDateBySecond());
            dao.updateEventAgentJobHistory(jobEntity);
            
            logger.debug("snet EVENT 진단  Expection 처리 완료 ==> "+msg);
        }

    }

    // GetScript exception to snet_connect_master : connect_log
    private void updateConnectMaster(JobEntity jobEntity) throws Exception {
        dao.updateConnectMaster(jobEntity);

    }

    // 진단 exception  to snet_agent_job_history : flag 4 : agent_job_desc
    private void updateAgentJobHistory(JobEntity jobEntity) throws SnetException {
        dao.updateAgentJobHistory(jobEntity);

    }

    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}