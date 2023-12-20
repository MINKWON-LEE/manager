package com.igloosec.smartguard.next.agentmanager.property;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

/**
 * AgentManager agent.context.properties
 */
@Component
@ConfigurationProperties(prefix = "smartguard.v3.context")
@Data
@ToString
public class AgentContextProperties {
    @NotEmpty
    private String getForDiag;

    @NotEmpty
    private String diagForEvent;

    @NotEmpty
    private String passWordType;

    @NotEmpty
    private String passWordOptionType;

    @NotEmpty
    private String agentPatchFiles;

    @NotEmpty
    private int defaultAgentCnt;

    @NotEmpty
    private int sizePerDelayTime;

    // job queueing 타임아웃 설정.
    private String jobDGTimeOut;
    private String jobGSTimeOut;
    private String jobNWTimeOut;
    private String jobSetupTimeOut;
    private String jobControlTimeOut;
    private String JobApiTimeOut;
    private String JobLogTimeOut;

    // 매니저 모듈이 멀티 인스턴스로 동작하는지 확인
    private String multiServices;

    // snet_asset_sw_audit_report에 진단기준 및 진단 제외 항목 추가
    private String diagExcept;

    private String useNotification;

    private String autoUrlTypeUse;

    private String autoUrlSwName;
}
