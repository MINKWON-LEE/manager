package com.igloosec.smartguard.next.agentmanager.api.model;

import jodd.util.StringUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * REST_API 응답 처리
 * 기본적으로 모든 응답의 HTTP_STATUS : 200 처리(INSERT 시 201)
 * InternalServerError는 실제 ERROR 와 INSERT / DELETE / UPDATE 등의 처리에 실패 했을 경우 500 값 리턴
 */
public class ResponseEntityUtil {

    public static ResponseEntity<ApiResult> returnSuccess(){
        return ResponseEntity.ok(ApiResult.builder().build());
    }

    public static ResponseEntity<ApiResult> returnCreate(){
        ApiResult apiResult = ApiResult.builder().result(200).build();
        return new ResponseEntity<>(apiResult, HttpStatus.CREATED);
    }

    public static ResponseEntity<ApiResult> returnNotFound(){
        return ResponseEntity.ok(ApiResult.builder().result(404).build());
    }

    public static ResponseEntity<ApiResult> returnForBidden(){
        return ResponseEntity.ok(ApiResult.builder().result(403).build());
    }

    public static ResponseEntity<ApiResult> returnConflct(){
        return ResponseEntity.ok(ApiResult.builder().result(409).build());
    }

    public static ResponseEntity<ApiResult> returnConflct(boolean isConflict){
        if(isConflict) return new ResponseEntity<>(ApiResult.builder().result(409).build(), HttpStatus.CONFLICT);
        return ResponseEntity.ok(ApiResult.builder().result(409).build());
    }

    public static ResponseEntity<ApiResult> returnBadRequest(){
        return ResponseEntity.ok(ApiResult.builder().result(400).build());
    }

    public static ResponseEntity<ApiResult> returnBadRequest(String message){
        return ResponseEntity.ok(ApiResult.builder().result(400).message(message).build());
    }

    public static ResponseEntity<ApiResult> returnInternalServerError(){
        return ResponseEntity.ok(ApiResult.builder().result(500).message("internal error.").build());
    }

    /**
     * httpStatu
     * @return
     */
    public static ResponseEntity<ApiResult> returnUnAuthentication(String message){
        return ResponseEntity.ok(ApiResult.builder().result(401).message(message).build());
    }

    /**
     * Object 형태의 리턴 데이터 처리
     * @param obj
     * @return
     */
    public static ResponseEntity<ApiResult> returnData(Object obj){
        if(obj == null){
            ApiResult apiResult = ApiResult.builder().result(404).build();
            return new ResponseEntity<>(apiResult, HttpStatus.OK);
        }

        ApiResult apiResult = ApiResult.builder().resultData(obj).build();
        return new ResponseEntity<>(apiResult, HttpStatus.OK);
    }

    /**
     * LIST 형태의 데이터 응답 처리
     * @param list
     * @return
     */
    public static ResponseEntity<ApiResult> returnList(List list){
        if(list == null || list.isEmpty()){
            ApiResult apiResult = ApiResult.builder().result(404).resultData(Collections.EMPTY_LIST).build();
            return ResponseEntity.ok(apiResult);
        }

        ApiResult apiResult = ApiResult.builder().resultData(list).build();
        return ResponseEntity.ok(apiResult);
    }

    /**
     * 파일리소스 리턴
     * @param resource
     * @param request
     * @return
     */
    public static ResponseEntity<Resource> returnResource(Resource resource, HttpServletRequest request){

    	if(resource == null) {
    	    return ResponseEntity.status(200).body(null);
        }

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(contentType == null){
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"" )
                .body(resource);
    }

    /**
     * 파일리소스 리턴 파일명 변경
     * @param resource
     * @param request
     * @return
     */
    public static ResponseEntity<Resource> returnResource(Resource resource, HttpServletRequest request, String fileOrgNm){
    	if(resource == null) return null;
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(contentType == null){
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String fileNm = StringUtil.isEmpty(fileOrgNm) ? resource.getFilename() : fileOrgNm;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileNm + "\"" )
                .body(resource);
    }

    public static ResponseEntity<ApiResult> returnApiResult(ApiResult apiResult) {

        return ResponseEntity.ok(apiResult);
    }
}
