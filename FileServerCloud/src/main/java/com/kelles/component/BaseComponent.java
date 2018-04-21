package com.kelles.component;

import com.google.gson.Gson;
import com.kelles.sdk.data.*;
import com.kelles.sdk.setting.*;
import com.kelles.util.Util;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseComponent {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected Gson gson;

    @Autowired
    protected Util util;

    @Autowired
    protected Charset defaultCharset;

    protected void logSQLMessage(String msg, String sql, boolean errorLevel) {
        String logMsg;
        if (sql == null) {
            logMsg = String.format("%s, database = %s, user = %s", msg, Setting.MYSQL_REPO_NAME, Setting.MYSQL_USER);
        } else {
            logMsg = String.format("%s, SQL = \n%s\n, database = %s, user = %s", msg, sql, Setting.MYSQL_REPO_NAME, Setting.MYSQL_USER);
        }
        if (errorLevel) {
            logger.error(logMsg);
        } else {
            logger.info(logMsg);
        }
    }

    protected void logSQLMessage(String msg, String sql) {
        logSQLMessage(msg, sql, false);
    }

    protected void logSQLMessage(String msg, boolean errorLevel) {
        logSQLMessage(msg, null, errorLevel);
    }

    protected void logSQLMessage(String msg) {
        logSQLMessage(msg, null, false);
    }

    protected void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                logSQLMessage("Close PreparedStatement Error", true);
                e.printStackTrace();
            }
        }
    }

    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logSQLMessage("Close Connection Error", true);
                e.printStackTrace();
            }
        }
    }

    public boolean securityCheck(String id, String access_code, FileDTO accessFileDTO) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(access_code) || accessFileDTO == null
                || StringUtils.isEmpty(accessFileDTO.getId()) || StringUtils.isEmpty(accessFileDTO.getAccess_code())) {
            return false;
        }
        return id.equals(accessFileDTO.getId()) && access_code.equals(accessFileDTO.getAccess_code());
    }

    public boolean securityCheck(FileDTO fileDTO) {
        if (fileDTO == null || StringUtils.isEmpty(fileDTO.getId()) || StringUtils.isEmpty(fileDTO.getAccess_code()))
            return false;
        return true;
    }
}
