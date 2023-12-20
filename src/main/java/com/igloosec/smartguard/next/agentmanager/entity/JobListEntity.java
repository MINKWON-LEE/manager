package com.igloosec.smartguard.next.agentmanager.entity;

import org.springframework.core.io.Resource;

public class JobListEntity {
    private AgentJobHistory agentJobHistory;
    private SnetConfigAuditFile snetConfigAuditFile;

    public AgentJobHistory getAgentJobHistory() {
        return agentJobHistory;
    }

    public void setAgentJobHistory(AgentJobHistory agentJobHistory) {
        this.agentJobHistory = agentJobHistory;
    }

    public SnetConfigAuditFile getSnetConfigAuditFile() {
        return snetConfigAuditFile;
    }

    public void setSnetConfigAuditFile(SnetConfigAuditFile snetConfigAuditFile) {
        this.snetConfigAuditFile = snetConfigAuditFile;
    }

    public Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
