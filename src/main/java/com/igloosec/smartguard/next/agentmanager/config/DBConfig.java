/**
 * com.igloosec.smartguard.microservices.servers.web.common.config .
 * 패키지 위치.
 */
package com.igloosec.smartguard.next.agentmanager.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * DBConfig .
 * DBConfig 위한 Class.
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class DBConfig {

	@Value("${spring.datasource.hikari.username}")
	private String username;
	@Value("${spring.datasource.hikari.password}")
	private String password;
	@Value("${spring.datasource.hikari.jdbc-url}")
	private String jdbcUrl;
	@Value("${spring.datasource.hikari.driver-class-name}")
	private String driver;

	@Value("${spring.profiles.active}")
	private String activeProfile;

	/**
	 * applicationContext 생성
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * dataSource 생성
	 */
	@Bean(name = "dataSource")
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public DataSource dataSource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

	/**
	 * sqlSessionFactory 생성 주입
	 */
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

		if(activeProfile != null && !(activeProfile.equals("prod") || activeProfile.equals("skt"))){
			log.info("=================");
			log.info(dataSource.toString());
			log.info(jdbcUrl);
			log.info(username);
			log.info(password);
			log.info(driver);
			log.info("=================");
		}

		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setVfs(SpringBootVFS.class);//spring boot
		sessionFactory.setTypeAliasesPackage("com.igloosec.smartguard.next.agentmanager");
		sessionFactory.setConfigLocation(applicationContext.getResource("classpath:mybatis/sqlMapConfig.xml"));
		sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mybatis/mapper/**/*.xml"));
		return sessionFactory.getObject();
	}

	/**
	 * sqlSession 생성 주입
	 */
	@Bean(name = "sqlSession")
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	/**
	 * transactionManager 생성 주입
	 */
	@Bean(name = "transactionManager")
	public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) throws Exception {
		return new DataSourceTransactionManager(dataSource);
	}

}
