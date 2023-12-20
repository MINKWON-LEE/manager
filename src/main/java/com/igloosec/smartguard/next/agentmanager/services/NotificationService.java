package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.SnetNotificationDataModel;
import com.igloosec.smartguard.next.agentmanager.entity.SnetNotificationModel;
import com.igloosec.smartguard.next.agentmanager.exception.SnetException;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("notificationService")
public class NotificationService extends AbstractManager {

    @Autowired
    Dao dao;

    /**
     * 알림 저장
     */
    public void insertNotification(SnetNotificationModel snetNotificationModel, SnetNotificationDataModel snetNotificationDataModel){

        if(snetNotificationModel == null) return;
        if(StringUtil.isEmpty(snetNotificationModel.getNotiUserId())) return;
        if(StringUtil.isEmpty(snetNotificationModel.getNotiType())) return;

        try {
            //notiseq 체크
            long notiSeq = 0;
            notiSeq = dao.getDuplicateNotiSeq(snetNotificationModel);
            snetNotificationModel.setNotiFlag("1");
            snetNotificationModel.setUseYn("Y");
            snetNotificationModel.setNotiSeq(notiSeq);
            if(notiSeq == 0){                        //insert
                dao.insertNotification(snetNotificationModel);
            }

            if(snetNotificationDataModel == null) return;

            snetNotificationDataModel.setNotiSeq(snetNotificationModel.getNotiSeq());
            //data insert
            if("Y".equalsIgnoreCase(snetNotificationModel.getNotiDataYn())){
                dao.insertNotificationData(snetNotificationDataModel);
            }
        } catch (SnetException e) {}
    }
}
