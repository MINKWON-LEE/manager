package com.igloosec.smartguard.next.agentmanager.schedule.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchGet {

    private String assetCd;

    private String agentCd;

    private String getType;

    private String getDayList;

    private String getMonthList;

    private String createUserId;  // -> personManagerCd

    private String userNm;

    private String managerCd;     // -> managerCd
}
