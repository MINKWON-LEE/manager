/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.exception.SnetException.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 3. 23.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.exception;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.JobEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SnetException extends AbstractSnetException {
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public SnetException(Dao dao, String msg, JobEntity jobEntity, String flag) throws Exception {
		super(dao,msg,jobEntity,flag);
	}

	public SnetException(Dao dao, SnetCommonErrCode errCodable, String args, JobEntity jobEntity, String flag) throws Exception {
		super(dao,errCodable,args,jobEntity,flag);
	}

	public SnetException(Dao dao, SnetCommonErrCode errCodable, JobEntity jobEntity, String flag, String... args) throws Exception {
		super(dao,errCodable,jobEntity,flag,args);
	}

	public SnetException(String message) {
		super(message,true);
	}


}
