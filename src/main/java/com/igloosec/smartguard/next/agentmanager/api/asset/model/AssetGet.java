package com.igloosec.smartguard.next.agentmanager.api.asset.model;

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
public class AssetGet {
    private String assetCd;

    private String sessionUserId;
    private String osType;
    private int osBit;
    private String connectIpAddress;
    private String relayAssetCd;
    private String relay2AssetCd;
    private String relay3AssetCd;
    private String relayIpAddress;
    private String relay2IpAddress;
    private String relay3IpAddress;
    private String relaySubIpAddress;
    private String relayPort;
    private String userId;
    private String password;
    private String socketFilePath;
    private String userIdOs;
    private String passwordOs;
    private String userIdRoot;
    private String passwordRoot;
    private int portSsh;
    private int portSftp;
    private int portTelnet;
    private int portFtp;
    private String connectShellOs;
    private String connectShellRoot;
    private String promptUserIdOs;
    private String promptUserIdRoot;
    private String agentInstallPath;
    private String agentType;

    private int useFtpPort;
    private int usePort;
    private String nwGetConfigCmd;
    private String loginType;
    private String loginTypeInt;
    private String useSudo;

}
