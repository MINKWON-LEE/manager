package com.igloosec.smartguard.next.agentmanager.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "smartguard.v3.debug")
@Getter
@Setter
@ToString
public class OptionProperties {

    private String async;

    private String diagnosis;
}
