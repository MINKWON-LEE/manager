/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.ErrCodeUtil.java
 * company : Mobigen
 * @author : Oh su jin
 * created at : 2016. 5. 3.
 * description :
 */

package com.igloosec.smartguard.next.agentmanager.utils;

public class ErrCodeUtil {
    public static String parseMessage(String message, String...args) {
        if (message == null || message.trim().length() <= 0)
            return message;

        if (args == null || args.length <= 0) return message;

        String[] splitMsgs = message.split("%");
        if (splitMsgs == null || splitMsgs.length <= 1)
            return message;

        for (int i = 0; i < args.length; i++) {
            String replaceChar = "%" + (i + 1);
            message = message.replaceFirst(replaceChar, args[i]);
        }
        return message;
    }
}
