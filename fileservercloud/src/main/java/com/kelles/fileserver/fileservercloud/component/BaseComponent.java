package com.kelles.fileserver.fileservercloud.component;

import com.google.gson.Gson;
import com.kelles.fileserver.fileserversdk.data.FileDTO;
import com.kelles.fileserver.fileserversdk.setting.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

public abstract class BaseComponent {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected Gson gson;

    @Autowired
    protected Charset defaultCharset;

    protected void logSQLMessage(String msg, String sql, boolean errorLevel) {
        String logMsg;
        if (sql == null) {
            logMsg = String.format("%s, database = %s, user = %s", msg, Setting.MYSQL_REPO_NAME_FILESERVER, Setting.MYSQL_USER);
        } else {
            logMsg = String.format("%s, SQL = \n%s\n, database = %s, user = %s", msg, sql, Setting.MYSQL_REPO_NAME_FILESERVER, Setting.MYSQL_USER);
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

    protected boolean securityCheck(String id, String access_code, FileDTO accessFileDTO) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(access_code) || accessFileDTO == null
                || StringUtils.isEmpty(accessFileDTO.getId()) || StringUtils.isEmpty(accessFileDTO.getAccess_code())) {
            return false;
        }
        return id.equals(accessFileDTO.getId()) && access_code.equals(accessFileDTO.getAccess_code());
    }

    protected boolean securityCheck(FileDTO fileDTO) {
        if (fileDTO == null || StringUtils.isEmpty(fileDTO.getId()) || StringUtils.isEmpty(fileDTO.getAccess_code()))
            return false;
        return true;
    }

    protected String getHeaderValues(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getHeaderNames();
        StringBuilder sb = new StringBuilder();
        for (; enumeration.hasMoreElements(); ) {
            String header = enumeration.nextElement();
            sb.append(header + " = " + request.getHeader(header) + "\n");
        }
        return sb.toString();
    }
}
