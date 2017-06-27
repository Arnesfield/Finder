package com.arnesfield.school.finder.config;

/**
 * Created by User on 06/07.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://192.168.1.10/";
    public static final String DIR_URL = "sites/school/android/finder/web/";
    // public static final String HTTP_HOST = "http://arnesfield.pe.hu/";
    // public static final String DIR_URL = "android/";
    public static final String DIR_ACTION_URL = DIR_URL + "action/";
    public static final String FETCH_URL = HTTP_HOST + DIR_ACTION_URL + "get-location.php";
    public static final String UPDATE_LOCATION_URL = HTTP_HOST + DIR_ACTION_URL + "update-location.php";
    public static final String ADD_USER_URL = HTTP_HOST + DIR_ACTION_URL + "add-user.php";
    public static final String LOGIN_URL = HTTP_HOST + DIR_ACTION_URL + "login.php";
    public static final String LOGOUT_URL = HTTP_HOST + DIR_ACTION_URL + "logout.php";
    public static final String SEND_NOTIF_URL = HTTP_HOST + DIR_ACTION_URL + "send-notif.php";
    public static final String CHECK_FOR_NOTIFS_URL = HTTP_HOST + DIR_ACTION_URL + "check-notif.php";
}
