package com.igloosec.smartguard.next.agentmanager.property;

import com.igloosec.smartguard.next.agentmanager.memory.INMEMORYDB;
import com.igloosec.smartguard.next.agentmanager.memory.ManagerJobType;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.io.File;

@Component
@ConfigurationProperties(prefix = "smartguard.v3.file")
@Data
@ToString
public class FileStorageProperties {
    @NotEmpty
    private String getUploadDir;

    @NotEmpty
    private String getDownloadDir;

    @NotEmpty
    private String diagnosisUploadDir;

    @NotEmpty
    private String diagnosisDownloadDir;

    @NotEmpty
    private String getManualUploadDir;

    @NotEmpty
    private String diagnosisManualUploadDir;

    @NotEmpty
    private String logFileManagerDir;

    @NotEmpty
    private String initAgentDir;

    @NotEmpty
    private String doDiagLastLog;

    public String getUploadPath(String jobType) {
        String uploadPath = "";
        if (jobType.equals(ManagerJobType.AJ100.toString()) || jobType.equals(ManagerJobType.AJ101.toString())) {
            uploadPath = StringUtil.replace(diagnosisUploadDir , "[MANAGER_SYS_ROOT_DIR]", INMEMORYDB.MANAGER_SYS_ROOT_DIR);
        } else if (jobType.equals(ManagerJobType.AJ200.toString()) || jobType.equals(ManagerJobType.AJ201.toString())) {
            uploadPath = StringUtil.replace(getUploadDir , "[MANAGER_SYS_ROOT_DIR]", INMEMORYDB.MANAGER_SYS_ROOT_DIR);
        } else if (jobType.equals(ManagerJobType.AJ300.toString())) {
            uploadPath = StringUtil.replace(logFileManagerDir , "[MANAGER_SYS_ROOT_DIR]", INMEMORYDB.MANAGER_SYS_ROOT_DIR);
        } else if (jobType.equals(ManagerJobType.AJ154.toString())) {
            uploadPath = StringUtil.replace(doDiagLastLog , "[MANAGER_SYS_ROOT_DIR]", INMEMORYDB.MANAGER_SYS_ROOT_DIR);
        }

        uploadPath = StringUtil.replace(uploadPath, "[SLASH]", File.separator);

        return uploadPath;
    }
}
