package com.igloosec.smartguard.next.agentmanager.entity;

import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class AgentResource {

    private String assetCd;

    private String cpuUseRate;

    private String memTotal;

    private String memFree;

    private String memUse;

    private String memUseRate;

    private String cDate;

    public AgentResource(String assetCd, String cpuUseRate, String memTotal, String memFree, String memUse, String memUseRate) {
        this.assetCd = assetCd;
        if (StringUtils.isEmpty(cpuUseRate)) {
            this.cpuUseRate = "0";
        } else {
            this.cpuUseRate = cpuUseRate;
        }
        if (StringUtils.isEmpty(memTotal)) {
            this.memTotal = "0";
        } else {
            this.memTotal = memTotal;
        }

        if (StringUtils.isEmpty(memFree)) {
            this.memFree = "0";
        } else {
            this.memFree = memFree;
        }

        if (StringUtils.isEmpty(memUse)) {
            this.memUse = "0";
        } else {
            this.memUse = memUse;
        }

        if (StringUtils.isEmpty(memUseRate)) {
            this.memUseRate = "0";
        } else {
            this.memUseRate = memUseRate;
        }

        if (memUseRate.trim().equals("12341234")) {
            this.memUseRate = "-";
            this.memTotal = "-";
            this.memFree = "-";
            this.memUse = "-";
            this.cpuUseRate = "-";
        }

        this.cDate = DateUtil.getCurrDateByminute();
    }
}
