/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.exception.ErrCodable.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 5. 3.
 * description :
 */
package com.igloosec.smartguard.next.agentmanager.exception;

public interface ErrCodable {
    String getErrCode();
    String getMessage(String... args);
}
