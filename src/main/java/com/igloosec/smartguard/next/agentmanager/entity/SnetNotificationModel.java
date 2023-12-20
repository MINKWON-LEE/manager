package com.igloosec.smartguard.next.agentmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("SnetNotificationModel")
public class SnetNotificationModel {
    private long notiSeq;
    private String notiType;
    private String notiTitle;
    private String notiLinkUrl;
    private String notiDataYn;
    private String useYn;
    private String createDate;
    private String updateDate;
    private int notiTypeCnt;

    private String notiUserId;
    private String notiUserNm;
    private String notiFlag;
    private String readDate;
    private String deleteDate;

    private String reqUserId;
}
