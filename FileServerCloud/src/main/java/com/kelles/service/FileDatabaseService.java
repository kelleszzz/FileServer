package com.kelles.service;

import com.kelles.sdk.data.*;
import com.kelles.sdk.setting.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class FileDatabaseService extends DatabaseService {
    @PostConstruct
    void init() {
        super.init();
        Connection conn = getConnection();
        try {
            //建表
            try {
                PreparedStatement ps = conn.prepareStatement(SQL.CREATE_TABLE);
                int rowsAffected = ps.executeUpdate();
                logSQLMessage("Create table " + rowsAffected);
                ps.close();
            } catch (SQLException e) {
                logSQLMessage("Create table Error", SQL.CREATE_TABLE);
                e.printStackTrace();
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logSQLMessage("Close connection Error", true);
                    e.printStackTrace();
                }
            }
        }
    }

    public Connection getConnection() {
        return getConnection(Setting.MYSQL_REPO_NAME);
    }

    /**
     * @param fileDTO
     * @param infoFileDTO 传入时,不再执行一次SELECT
     * @param conn
     * @return
     */
    public int updateFileDTO(FileDTO fileDTO, FileDTO infoFileDTO, Connection conn) {
        if (fileDTO == null || StringUtils.isEmpty(fileDTO.getId()) || conn == null)
            return Setting.STATUS_INVALID_PARAMETER;
        boolean success = false;
        PreparedStatement psUpdateInfo = null, psUpdateContent = null;
        int rowsAffected = Setting.STATUS_ERROR;
        try {
            //查找原文件
            if (infoFileDTO == null || StringUtils.isEmpty(infoFileDTO.getId()) || !infoFileDTO.getId().equals(fileDTO.getId())) {
                infoFileDTO = getFileDTO(fileDTO.getId(), false, conn);
            }
            if (infoFileDTO == null) return Setting.STATUS_FILE_NOT_FOUND;
            if (!StringUtils.isEmpty(fileDTO.getAccess_code()))
                infoFileDTO.setAccess_code(fileDTO.getAccess_code());
            if (fileDTO.getCreate_time() != null)
                infoFileDTO.setCreate_time(fileDTO.getCreate_time());
            if (fileDTO.getSize() != null) infoFileDTO.setSize(fileDTO.getSize());
            if (!StringUtils.isEmpty(fileDTO.getFile_name())) infoFileDTO.setFile_name(fileDTO.getFile_name());
            //更新信息
            psUpdateInfo = conn.prepareStatement(SQL.UPDATE_INFO);
            psUpdateInfo.setString(1, infoFileDTO.getAccess_code());
            psUpdateInfo.setLong(2, infoFileDTO.getCreate_time());
            psUpdateInfo.setLong(3, infoFileDTO.getSize());
            psUpdateInfo.setString(4, infoFileDTO.getFile_name());
            psUpdateInfo.setString(5, infoFileDTO.getId());
            rowsAffected = psUpdateInfo.executeUpdate();
            //更新文件内容
            if (fileDTO.getInputStream() != null) {
                psUpdateContent = conn.prepareStatement(SQL.UPDATE_CONTENT);
                psUpdateContent.setBinaryStream(1, fileDTO.getInputStream());
                psUpdateContent.setString(2, fileDTO.getId());
                rowsAffected = psUpdateContent.executeUpdate();
            }
            logSQLMessage("Update " + gson.toJson(Util.fileDTOInfo(fileDTO)), SQL.UPDATE_INFO);
            return rowsAffected;
        } catch (SQLException e) {
            logSQLMessage("Update Error, fileDTO = " + gson.toJson(Util.fileDTOInfo(fileDTO)), SQL.INSERT, true);
            e.printStackTrace();
            return Setting.STATUS_ERROR;
        } finally {
            closePreparedStatement(psUpdateInfo);
            closePreparedStatement(psUpdateContent);
        }
    }

    public int insertFileDTO(FileDTO fileDTO, Connection conn) {
        if (!securityCheck(fileDTO) || conn == null) return Setting.STATUS_INVALID_PARAMETER;
        boolean success = false;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(SQL.INSERT);
            ps.setString(1, fileDTO.getId());
            ps.setString(2, fileDTO.getAccess_code());
            if (fileDTO.getInputStream() != null) {
                ps.setBinaryStream(3, fileDTO.getInputStream());
            }
            ps.setLong(4, System.currentTimeMillis());
            ps.setLong(5, fileDTO.getSize());
            ps.setString(6, fileDTO.getFile_name());
            int rowsAffected = ps.executeUpdate();
            logSQLMessage("Insert " + gson.toJson(Util.fileDTOInfo(fileDTO)), SQL.INSERT);
            return rowsAffected;
        } catch (SQLException e) {
            //当id重复或文件过大时,抛出此异常
            logSQLMessage("Insert Error, fileDTO = " + gson.toJson(Util.fileDTOInfo(fileDTO)), SQL.INSERT, true);
//            e.printStackTrace();
//            return Setting.STATUS_ERROR;
            return Setting.STATUS_FILE_ALREADY_EXISTS;
        } finally {
            closePreparedStatement(ps);
        }
    }

    public FileDTO getFileDTO(String id, boolean getContent, Connection conn) {
        if (StringUtils.isEmpty(id) || conn == null) return null;
        PreparedStatement ps = null;
        try {
            if (getContent) ps = conn.prepareStatement(SQL.SELECT);
            else ps = conn.prepareStatement(SQL.SELECT_NO_CONTENT);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FileDTO fileDTO = new FileDTO();
                fileDTO.setId(id);
                fileDTO.setAccess_code(rs.getString("access_code"));
                fileDTO.setCreate_time(rs.getLong("create_time"));
                fileDTO.setSize(rs.getLong("size"));
                fileDTO.setFile_name(rs.getString("file_name"));
                if (getContent) {
                    fileDTO.setInputStream(rs.getBinaryStream("content"));
                }
                return fileDTO;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            logSQLMessage("Select Error, id = " + id, getContent ? SQL.SELECT : SQL.SELECT_NO_CONTENT, true);
            return null;
        } finally {
            closePreparedStatement(ps);
        }
    }

    public int removeFileDTO(String id, Connection conn) {
        if (StringUtils.isEmpty(id) || conn == null) return Setting.STATUS_ERROR;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(SQL.DELETE);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            logSQLMessage("Remove Error, id = " + id, SQL.DELETE, true);
            return Setting.STATUS_ERROR;
        } finally {
            closePreparedStatement(ps);
        }
    }

}
