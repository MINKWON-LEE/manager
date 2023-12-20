package com.igloosec.smartguard.next.agentmanager.api.model;

import com.igloosec.smartguard.next.agentmanager.dao.Dao;
import com.igloosec.smartguard.next.agentmanager.entity.ConfigUserViewDBEntity;
import com.igloosec.smartguard.next.agentmanager.entity.GscriptResultEntity;
import com.sk.snet.manipulates.EncryptUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class SmartGuardToken {

    private Dao dao;

    private String authorization; //origin data;
    private String issuer;        // Issuer of the Token. WAS, AGENT
    private String managerCd;     // manager code
    private String userId;        // user Id
    private String agentCd;       // agentCd
    private String hostName;      // hostName
    private String ip;            // ip
    private String version;       // version
    private long   exp;           // expiry date; Unix Time Epoch
    private EncryptUtil encryptUtil;

    public SmartGuardToken() throws Exception {
        encryptUtil = new EncryptUtil();
    }

    public void decodeSgToken(String sgToken) throws Exception {

        String decAuth =  encryptUtil.aes_decrypt2(sgToken);
        String[] authArr = decAuth.split("@@");

        this.issuer = authArr[0];
        if ((authArr.length == 3) && issuer.equals("WAS")) {

            this.managerCd = authArr[1];
            this.userId = authArr[2];

            log.debug("decode auth : {} - {} - {}",
                    this.issuer, this.managerCd, this.userId);
        } else if ((authArr.length == 6) && issuer.equals("AGENT")) {

            this.agentCd = authArr[1];
            this.hostName = authArr[2];
            this.ip = authArr[3];
            this.version = authArr[4];
            this.exp = System.currentTimeMillis();

            log.debug("decode auth : {} - {} - {} - {} - {} - {}",
                    this.issuer, this.agentCd, this.hostName, this.ip, this.version, this.exp);
        } else {

            throw new Exception("authoriztion token is invalid.");
        }
    }

    public boolean isValid(String sgToken) throws Exception {

        String decAuth = encryptUtil.aes_decrypt3(sgToken);

        String[] authArr = decAuth.split("@@");

        this.issuer = authArr[0];
        if ((authArr.length == 3) && issuer.equals("WAS")) {

            this.userId = authArr[1];
            this.managerCd = authArr[2];

//            log.debug("decode auth : {} - {} - {}",
//                    this.issuer, this.managerCd, this.userId);
            return true;
        } else if ((authArr.length == 4) && issuer.equals("AGENT")) {

            this.hostName = authArr[1];
            this.ip = authArr[2];
            this.version = authArr[3];

//            log.debug("decode auth : {} - {} - {} - {}",
//                    this.issuer, this.hostName, this.ip, this.version);
            return true;
        }

        return false;
    }

    /**
     * 에이전트 사용 안함
     * 추후 에이전트 사용 고려시 참고.
     */
    public boolean isValid2() throws Exception {

        if (issuer.equals("WAS")) {
            //was -> manager
            //manager_cd | user_id check
            GscriptResultEntity resultEntity = new GscriptResultEntity();
            resultEntity.setManagerCd(this.managerCd);

            ConfigUserViewDBEntity userViewEntity = dao.selectConfigUserView(resultEntity);
            if (userViewEntity == null) {
                return false;
            }

            if (!userViewEntity.getUserId().equals(this.userId)) {
                return false;
            }
        } else if (issuer.equals("AGENT")) {
            //agent -> manager
            //agent master check
            int cnt = dao.selectAgentMasterCd(this.agentCd);
            if(cnt == 0){
                //insert
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
