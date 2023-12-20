/**
 * com.igloosec.smartguard.microservices.servers.web.common.config .
 * 패키지 위치.
 */
package com.igloosec.smartguard.next.agentmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igloosec.smartguard.next.agentmanager.interceptor.AuthCheckInterceptor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;

/**
 * WebConfig .
 * WebConfig를 위한 Class.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private AuthCheckInterceptor authCheckInterceptor;

	public WebMvcConfig(AuthCheckInterceptor authCheckInterceptor) {
		this.authCheckInterceptor = authCheckInterceptor;
	}

	@Bean
	public CorsFilter corsFilter(){
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		config.addExposedHeader("Content-Disposition");
		config.setMaxAge(3600L);
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper();
	}

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authCheckInterceptor)
					.addPathPatterns("/**")
					.excludePathPatterns("/ping")
					.excludePathPatterns("/manager/v3/sga-api/agent/auth");
	}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
//		//스레드 풀을 해당 개수까지 기본적으로 생성함.
//		//처음 요청이 들어올 때 pool size 만큼 생성한다
//		te.setCorePoolSize(20);
//		//지금 당장은 core 스레드를 모두 사용중일때, 큐에 만들어 대기시킨다.
//		te.setQueueCapacity(20);
//		//대기하는 작업이 큐에 꽉 찰 경우, 풀을 해당 개수까지 더 생성한다.
//		te.setMaxPoolSize(50);
//		te.setKeepAliveSeconds(60);
//		te.setThreadNamePrefix("manager-async");
//		te.initialize();
		configurer.setDefaultTimeout((60*1000) * 3);
		configurer.setTaskExecutor(taskExecutor());
	}

	@Bean(name = "taskExecutor")
	public ThreadPoolTaskExecutor taskExecutor(){
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		//스레드 풀을 해당 개수까지 기본적으로 생성함.
		//처음 요청이 들어올 때 pool size 만큼 생성한다
		te.setCorePoolSize(20);
		//지금 당장은 core 스레드를 모두 사용중일때, 큐에 만들어 대기시킨다.
		te.setQueueCapacity(20);
		//대기하는 작업이 큐에 꽉 찰 경우, 풀을 해당 개수까지 더 생성한다.
		te.setMaxPoolSize(50);
		te.setKeepAliveSeconds(60);
		te.setThreadNamePrefix("manager-async");
		te.initialize();
		return te;
	}

	@Bean
	public FilterRegistrationBean requestLoggingFilter() {
		CommonsRequestLoggingFilter loggingFilter = new SgCommonsRequestLoggingFilter();
		loggingFilter.setIncludeClientInfo(false);
		loggingFilter.setIncludeQueryString(true);
		loggingFilter.setIncludePayload(true);
		loggingFilter.setIncludeHeaders(false);
		loggingFilter.setMaxPayloadLength(1024 * 1024);
		FilterRegistrationBean bean = new FilterRegistrationBean(loggingFilter);
		bean.setOrder(Integer.MIN_VALUE);
		return bean;

	}

}
