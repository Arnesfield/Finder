package com.arnesfield.school.finder;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.arnesfield.school.finder.tasks.LoginUserTask;
import com.arnesfield.school.mytoolslib.RequestStringCreator;
import com.arnesfield.school.mytoolslib.SnackBarCreator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements LoginUserTask.LoginListener {

    private static final String LOGIN_PREF = "login_pref";
    private static final String LOGIN_ID = "login_id";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_login);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // references
        etUsername = (EditText) findViewById(R.id.login_et_username);
        etPassword = (EditText) findViewById(R.id.login_et_password);
        btnLogin = (Button) findViewById(R.id.login_btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUserTask.execute(LoginActivity.this);
            }
        });

        // if shared pref not -1
        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREF, MODE_PRIVATE);
        int loggedOut = getIntent().getIntExtra("logout", -1);
        int loginId = sharedPreferences.getInt(LOGIN_ID, -1);

        // if logged out
        if (loggedOut != -1) {
            // edit shared pref
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(LOGIN_ID, -1);
            editor.apply();

            // show message
            SnackBarCreator.toast(this, R.string.snackbar_success_logout);
        }

        if (loginId != -1 && loggedOut == -1) {
            successfulLogin(loginId, false);
        }
    }

    private void successfulLogin(int id, boolean showLoginMessage) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("show_message", showLoginMessage);
        intent.putExtra("uid", id);

        startActivity(intent);
        finish();
    }

    @Override
    public void parseJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            int id = jsonObject.getInt("login");

            // invalid login
            if (id == 0) {
                SnackBarCreator.toast(this, R.string.snackbar_fail_login);
                return;
            }

            // save id to sharedpref
            SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(LOGIN_ID, id);
            editor.apply();

            // successful
            successfulLogin(id, true);

            // setListView();
        } catch (Exception ignored) {}
    }

    @Override
    public String createLoginPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || username.matches("[\\s]+")) {
            SnackBarCreator.toast(this, R.string.snackbar_fail_empty_username);
            return null;
        }

        if (password.isEmpty() || password.matches("[\\s]+")) {
            SnackBarCreator.toast(this, R.string.snackbar_fail_empty_password);
            return null;
        }

        contentValues.put("login", true);
        contentValues.put("username", username);
        contentValues.put("password", password);

        return RequestStringCreator.create(contentValues);
    }
}
