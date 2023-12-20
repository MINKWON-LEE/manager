package com.igloosec.smartguard.next.agentmanager.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "smartguard.v3.pty")
@Getter
@Setter
@ToString
public class PtyAanlyzerProperties {

    @NotEmpty
    private String ptyPromptPhraseVals;
    @NotEmpty
    private String ptyIntPromptPhraseVals;
    @NotEmpty
    private String ptyPassPhraseVals;
    @NotEmpty
    private String ptyIntPassPraseVals;
    @NotEmpty
    private String ptyLoginPhraseVals;
    @NotEmpty
    private String ptyLoginExcludeVals;
    @NotEmpty
    private String ptySslConfirmPhraseVals;
    @NotEmpty
    private String ptyIncorrectVals;
    @NotEmpty
    private String ptyIncorrectExcludeVals;
    @NotEmpty
    private String ptyIntIncorrectVals;
    @NotEmpty
    private String SHADOW_MANAGER_DIR;
    @NotEmpty
    private String JTR_SHELL;
}
