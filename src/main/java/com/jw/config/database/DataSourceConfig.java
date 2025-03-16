package com.jw.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource oracleDataSource(DataSourceProperties oracleDataSourceProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(oracleDataSourceProperties.getUrl());
        config.setDriverClassName(oracleDataSourceProperties.getDriverClassName());
        config.setUsername(oracleDataSourceProperties.getUsername());
        config.setPassword(oracleDataSourceProperties.getPassword());
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource oracleProxyDataSource(DataSource oracleDataSource) {
        return ProxyDataSourceBuilder.create(oracleDataSource)
                .name("ORACLE-LOGGER")
//                .logQueryBySlf4j(log.getName())
                .asJson()
                .countQuery()
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource mysqlDataSource(DataSourceProperties mysqlDataSourceProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlDataSourceProperties.getUrl());
        config.setUsername(mysqlDataSourceProperties.getUsername());
        config.setPassword(mysqlDataSourceProperties.getPassword());
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource mysqlProxyDataSource(DataSource mysqlDataSource) {
        return ProxyDataSourceBuilder.create(mysqlDataSource)
                .name("MYSQL-LOGGER")
                .logQueryBySlf4j(log.getName())
                .asJson()
                .countQuery()
                .build();
    }


}
