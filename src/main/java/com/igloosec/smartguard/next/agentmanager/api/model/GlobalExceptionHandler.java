package com.igloosec.smartguard.next.agentmanager.api.model;

import com.igloosec.smartguard.next.agentmanager.exception.UnAuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.*;

import java.net.BindException;

@ControllerAdvice
@Slf4j
@RestController
public class GlobalExceptionHandler {

	// Unknown
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult> unknownExceptionHandler(Exception e) {
		log.info("unknownException {}", e.getMessage());
		return ResponseEntityUtil.returnInternalServerError();
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResult> httpMessageBindExceptionHandler(Exception e) {
		log.error("httpMessageBindExceptionHandler - " + e.getMessage());
		return ResponseEntityUtil.returnBadRequest("httpMessageBindExceptionHandler");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResult> httpMessageNotReadableExceptionHandler(Exception e) {
		log.error("HttpMessageNotReadableException - " + e.getMessage());
		return ResponseEntityUtil.returnBadRequest("HttpMessageNotReadableException");
	}

	@ExceptionHandler(HttpMessageNotWritableException.class)
	public ResponseEntity<ApiResult> httpMessageNotWritableExceptionHandler(Exception e) {
		log.error("httpMessageNotWritableExceptionHandler - " + e.getMessage());
		return ResponseEntityUtil.returnBadRequest("HttpMessageNotWritableException");
	}

	@ExceptionHandler(value = UnAuthorizationException.class)
	public ResponseEntity<ApiResult> handleBaseException(UnAuthorizationException e) {
		log.error("handleBaseException - " + e.getMessage());
		return ResponseEntityUtil.returnUnAuthentication(e.getMessage());
	}

	@GetMapping("/errors/{code}")
	public ResponseEntity<?> error(@PathVariable String code){
		int status = Integer.parseInt(code);

		if(status == 401 || status == 403 || status == 404 || status == 500) {
			return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(status).build());
		}else{
			return ResponseEntityUtil.returnApiResult(ApiResult.builder().result(400).build());
		}
	}
}
