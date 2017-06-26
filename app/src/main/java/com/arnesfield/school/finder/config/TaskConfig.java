package com.arnesfield.school.finder.config;

/**
 * Created by User on 06/07.
 */

public final class TaskConfig {
    public static final String HTTP_HOST = "http://192.168.1.10/";
    public static final String DIR_URL = "sites/school/android/finder/web/";
    public static final String DIR_ACTION_URL = DIR_URL + "action/";
    public static final String FETCH_URL = HTTP_HOST + DIR_ACTION_URL + "get-data.php";
    public static final String ADD_LOCATION_URL = HTTP_HOST + DIR_ACTION_URL + "add-location.php";
    public static final String ADD_USER_URL = HTTP_HOST + DIR_ACTION_URL + "add-user.php";
    public static final String LOGIN_URL = HTTP_HOST + DIR_ACTION_URL + "login.php";
}
