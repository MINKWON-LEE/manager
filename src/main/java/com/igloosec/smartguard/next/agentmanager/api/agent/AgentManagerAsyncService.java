package com.igloosec.smartguard.next.agentmanager.api.agent;

import com.igloosec.smartguard.next.agentmanager.api.agent.model.ControlAgentReq;
import com.igloosec.smartguard.next.agentmanager.api.model.ApiResult;
import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EnableAsync
@Slf4j
@Service
public class AgentManagerAsyncService {

    private Dao dao;

    public AgentManagerAsyncService(Dao dao) {

        this.dao = dao;
    }

    @Async
    public CompletableFuture<ApiResult> controlAgent(List<ControlAgentReq> controlAgentReqS) throws Exception {


        return null;
//        return CompletableFuture.completedFuture(this.handleResultFileForAsync(assetCd, jobType, uploadPath, jobEntity))
//                .exceptionally(ex -> {
//                    log.error(ex.getMessage());
//                    return ApiResult.getFailResult();
//                } );
    }
}
