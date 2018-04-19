package com.kelles.util;

public class Setting {
    public final static String MYSQL_USER ="root";
    public final static String MYSQL_PASSWORD="tom44123";
    public final static String MYSQL_REPO_NAME="fileserver";
    public final static String MYSQL_TABLE_NAME="files";
    public final static String MYSQL_URL="jdbc:mysql://localhost:3306/";

    public final static String TYPE_MESSAGE="TYPE_MESSAGE";

    public final static int STATUS_ERROR=-1;
    public final static int STATUS_FILE_NOT_FOUND=-2;
    public final static int STATUS_INVALID_PARAMETER=-3;
    public final static int STATUS_ACCESS_DENIED=-4;
    public final static int STATUS_FILE_ALREADY_EXISTS=-5;

    public final static String MESSAGE_FILE_NOT_FOUND="File Not Found";
    public final static String MESSAGE_ACCESS_DENIED="Access Denied";
    public final static String MESSAGE_FILE_ALREADY_EXISTS="File Already Exists";
}
