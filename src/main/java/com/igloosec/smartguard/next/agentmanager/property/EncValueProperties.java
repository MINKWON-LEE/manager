package com.igloosec.smartguard.next.agentmanager.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sgn.enc")
@Getter
@Setter
@ToString
public class EncValueProperties {
    private int keySize;
    private int iterationCount;
    private String key;
    private String salt;
}
