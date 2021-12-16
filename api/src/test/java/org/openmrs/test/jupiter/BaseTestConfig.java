package org.openmrs.test.jupiter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
@EnableTransactionManagement
public class BaseTestConfig {
	
	@Autowired
	private DataSource dataSource;
	
	private static final String PASSWORD = "admin";
	
	private static final String USER = "postgres";
	
	private static final String DB_NAME = "openmrs";
	
	private static LocalContainerEntityManagerFactoryBean emf;
	
	private static JpaTransactionManager transactionManager;
	
	private static PostgreSQLContainer postgreSqlContainer = new PostgreSQLContainer("postgres:13");
	
	public SpringLiquibase testLiquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog("classpath:liquibase/liquibase-test-insert-with-uuid.xml");
		liquibase.setDataSource(dataSource);
		return liquibase;
	}
	
	public DataSource createDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl(postgreSqlContainer.getJdbcUrl());
		dataSource.setUsername(postgreSqlContainer.getUsername());
		dataSource.setPassword(postgreSqlContainer.getPassword());
		System.setProperty("db.url", postgreSqlContainer.getJdbcUrl());
		System.setProperty("db.user", postgreSqlContainer.getUsername());
		System.setProperty("db.pass", postgreSqlContainer.getPassword());
		startPostgreSql();
		return dataSource;
	}
	
	@Bean("transactionManager")
	@Primary
	@Profile("test")
	public PlatformTransactionManager getTransactionManager(EntityManagerFactory entityManagerFactory) {
		if (transactionManager == null) {
			transactionManager = new JpaTransactionManager();
			transactionManager.setEntityManagerFactory(entityManagerFactory);
		}
		return transactionManager;
	}
	
	@Bean
	@DependsOn("liquibase")
	@Profile("test")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		if (emf == null) {
			emf = new LocalContainerEntityManagerFactoryBean();
			emf.setPersistenceXmlLocation("classpath:resources/hibernate.cfg.xml");
		}
		return emf;
	}
	
	private void startPostgreSql() {
		postgreSqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("postgre-db-init"),
		    "/docker-entrypoint-initdb.d");
		postgreSqlContainer.withEnv("POSTGRES_INITDB_ARGS", "--auth-host=md5");
		postgreSqlContainer.withDatabaseName(DB_NAME);
		postgreSqlContainer.withUsername(USER);
		postgreSqlContainer.withPassword(PASSWORD);
		postgreSqlContainer.start();
	}
}
