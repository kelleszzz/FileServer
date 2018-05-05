package com.kelles.fileservercloud.service;

import com.kelles.fileserversdk.setting.Setting;
import com.kelles.fileserversdk.data.*;
import com.kelles.fileserversdk.setting.*;
import com.kelles.fileservercloud.component.BaseComponent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Service
public class DatabaseService extends BaseComponent{
    DriverManager driverManager;

    @PostConstruct
    void init(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            logger.info("Connecting to MySQL, database = {}, user = {}, password = {}", Setting.MYSQL_REPO_NAME,
                    Setting.MYSQL_USER, Setting.MYSQL_PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("Initializing JDBC error");
        }
    }

    @PreDestroy
    void destroy(){
        logger.info("Closing MySQL connections, database = {}, user = {}, password = {}", Setting.MYSQL_REPO_NAME,
                Setting.MYSQL_USER, Setting.MYSQL_PASSWORD);
    }

    protected Connection getConnection(String repoName){
        if (repoName==null || "".equals(repoName)) return null;
        try {
            Properties properties=new Properties();
            properties.setProperty("user", Setting.MYSQL_USER);
            properties.setProperty("password", Setting.MYSQL_PASSWORD);
            properties.setProperty("serverTimezone","UTC");
            properties.setProperty("useSSL","true");
            return DriverManager.getConnection(Setting.MYSQL_URL+repoName, properties);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Connecting to MySQL error, database = {}, user = {}, password = {}", Setting.MYSQL_REPO_NAME,
                    Setting.MYSQL_USER, Setting.MYSQL_PASSWORD);
            return null;
        }
    }

}
