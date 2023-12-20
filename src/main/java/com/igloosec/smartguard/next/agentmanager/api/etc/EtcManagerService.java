package com.igloosec.smartguard.next.agentmanager.api.etc;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.property.FileStorageProperties;
import com.igloosec.smartguard.next.agentmanager.services.AgentVersionManager;
import com.igloosec.smartguard.next.agentmanager.services.AssetUpdateManager;
import com.igloosec.smartguard.next.agentmanager.services.DeployManager;
import com.igloosec.smartguard.next.agentmanager.services.JobHandleManager;
import com.igloosec.smartguard.next.agentmanager.services.RemoteControlManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EtcManagerService {

    @Autowired
    private Dao dao;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private JobHandleManager jobHandleManager;

    @Autowired
    private DeployManager deployManager;

    @Autowired
    private RemoteControlManager remoteControlManager;

    @Autowired
    private AgentVersionManager agentVersionManager;

    @Autowired
    private AssetUpdateManager assetUpdateManager;
}
