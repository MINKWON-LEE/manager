package com.igloosec.smartguard.next.agentmanager.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.igloosec.smartguard.next.agentmanager.memory.ApiResultMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * status 200 이 외는 사용자 에러처리
 * API 결과 값이 정상처리 될 경우만 200
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {
    @Builder.Default private int result = 200;
    @Builder.Default private String message = "success";

    @JsonProperty("installed")
    private String installed;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("delaytime")
    private String delaytime;

    @JsonProperty("assetCd")
    private String assetCd;

    @JsonProperty("agentCd")
    private String agentCd;

    @JsonProperty("managerCd")
    private String managerCd;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("jobType")
    private String jobType;

    @JsonProperty("jobData")
    private T resultData;

    public static ApiResult failResult = ApiResult.builder().result(500).message(ApiResultMessage.APIFAILED).build();

    public static ApiResult badRequestResult = ApiResult.builder().result(400).message(ApiResultMessage.BADREQUEST).build();

    public static ApiResult notFoundResult = ApiResult.builder().result(404).message(ApiResultMessage.NOTFOUND).build();

    public static ApiResult unAuthorizedResult = ApiResult.builder().result(401).message(ApiResultMessage.UNAUTHORIZED).build();

    public static ApiResult getFailResult() { return failResult; }

    public static ApiResult getUnAuthorizedResult() { return unAuthorizedResult; }

    public static ApiResult getBadRequestResult() { return badRequestResult; }

    public static ApiResult getNotFoundResult() { return notFoundResult; }
}
