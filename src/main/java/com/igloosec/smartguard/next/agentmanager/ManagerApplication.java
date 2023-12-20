package com.igloosec.smartguard.next.agentmanager;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.utils.CommonUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.igloosec.smartguard.next"})
@EnableConfigurationProperties
public class ManagerApplication {
    public static void main(String [] args){

        String confPath = INMEMORYDB.CONF_DIR + "/" + INMEMORYDB.CONF_PROPERTIES_FILE;

        new SpringApplicationBuilder(ManagerApplication.class)
                .properties("spring.config.location=" + "classpath:/application.yml" +
                        ", classpath:/application-${spring.profiles.active}.yml" +
                        ", optional:" + CommonUtils.getWinPath(confPath))
                .run(args);
    }
}


