package com.igloosec.smartguard.next.agentmanager.services;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.property.AgentContextProperties;
import com.igloosec.smartguard.next.agentmanager.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class AgentJobScheduleManager {

    private int delayTimeCnt;
    private int defaultJobSchedule;
    private int sizePerDelayTime;
    private int scheduleCnt;


    private AgentContextProperties agentContextProperties;

    public AgentJobScheduleManager(AgentContextProperties agentContextProperties) {
        this.agentContextProperties = agentContextProperties;
    }

    public void initAgentJobScheduleList() {

        // 에이전트 하나당 5분마다 호출.  관리자가 주기를 변경할 수 있지만 기본적으로 에이전트 만대당 동접 100대, 최대 에이전트 3초마다 호출 가능.
        defaultJobSchedule = INMEMORYDB.DefaultJobSchedule;
        // 같은 시간에 호출 가능한 에이전트 개수
        sizePerDelayTime = agentContextProperties.getSizePerDelayTime();
        // 같은 시간에 호출 가능한 에이전트끼리 묶음 개수.
        // 허용가능한 총 에이전트 개수 /  같은 시간에 호출 가능한 에이전트 개수
        delayTimeCnt = agentContextProperties.getDefaultAgentCnt() / sizePerDelayTime;
        // 시간 분할 개수
        scheduleCnt = defaultJobSchedule / delayTimeCnt;

        log.debug("delayTimeCnt - {}, defaultJobSchedule - {}, sizePerDelayTime - {}, scheduleCnt - {}",
                delayTimeCnt, defaultJobSchedule, sizePerDelayTime, scheduleCnt);

        for (int i = 0; i < defaultJobSchedule; i = i + scheduleCnt) {
            CopyOnWriteArrayList<String> assetCdList = new CopyOnWriteArrayList<>();

            INMEMORYDB.AGENTSCHEDULELIST.put(i, assetCdList);
        }

        log.debug("INMEMORYDB.AGENTSCHEDULELIST size - {}", INMEMORYDB.AGENTSCHEDULELIST.size());
    }

    public String getDelayTime(String assetCd, String agentCd) {

        String delayTime = "";
        defaultJobSchedule = INMEMORYDB.DefaultJobSchedule;

        for (int i = 0; i < defaultJobSchedule; i = i + scheduleCnt) {
            if (INMEMORYDB.AGENTSCHEDULEASSETLIST.containsKey(assetCd)) {
                Integer sched = INMEMORYDB.AGENTSCHEDULEASSETLIST.get(assetCd);
                delayTime = Integer.toString(sched + defaultJobSchedule);
                break;
            }

            CopyOnWriteArrayList<String> assetCdList = INMEMORYDB.AGENTSCHEDULELIST.get(i);
            if (assetCdList.size() < sizePerDelayTime){
                assetCdList.add(assetCd);
                INMEMORYDB.AGENTSCHEDULELIST.putIfAbsent(i, assetCdList);
                INMEMORYDB.AGENTSCHEDULEASSETLIST.putIfAbsent(assetCd, i);
                delayTime = Integer.toString(i + defaultJobSchedule);
                break;
            }
        }

        // job Api 를 호출한 시간을 체크.
        INMEMORYDB.AGENTSCHEDULEASSETCHECKLIST.put(assetCd, DateUtil.getCurrDateBySecond());

        return delayTime;
    }
}
