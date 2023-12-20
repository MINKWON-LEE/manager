/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.entity.RunnigJobEntity.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 4. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RunnigJobEntity implements Serializable {

    String msg;
    String jobDate;
    JobEntity jobEntity;



    public RunnigJobEntity(String jobDate){
        this.jobDate = jobDate;
    }


    public JobEntity getJobEntity() {
        return jobEntity;
    }

    public void setJobEntity(JobEntity jobEntity) {
        this.jobEntity = jobEntity;
    }

    public void setJobDate(String jobDate) {
        this.jobDate = jobDate;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getJobDate() {
        return jobDate;
    }
}
