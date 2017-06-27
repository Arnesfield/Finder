package com.arnesfield.school.finder;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arnesfield.school.finder.tasks.SignUpUserTask;
import com.arnesfield.school.mytoolslib.RequestStringCreator;
import com.arnesfield.school.mytoolslib.SnackBarCreator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SignUpActivity extends AppCompatActivity implements SignUpUserTask.OnSignUpListener {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etEmail;
    private Button btnSignup;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilEmail;

    private int resultCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // references
        etUsername = (EditText) findViewById(R.id.signup_et_username);
        etPassword = (EditText) findViewById(R.id.signup_et_password);
        etConfirmPassword = (EditText) findViewById(R.id.signup_et_confirm_password);
        etEmail = (EditText) findViewById(R.id.signup_et_email);

        tilUsername = (TextInputLayout) findViewById(R.id.signup_username_container);
        tilPassword = (TextInputLayout) findViewById(R.id.signup_password_container);
        tilConfirmPassword = (TextInputLayout) findViewById(R.id.signup_confirm_password_container);
        tilEmail = (TextInputLayout) findViewById(R.id.signup_email_container);

        btnSignup = (Button) findViewById(R.id.signup_btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultCode = 2;
                if (verifiedInputValues())
                    SignUpUserTask.execute(SignUpActivity.this);
            }
        });
    }

    private boolean verifiedInputValues() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        boolean verified = true;

        if (username.isEmpty()) {
            tilUsername.setError(getResources().getString(R.string.snackbar_fail_empty_field));
            verified = false;
        }
        else tilUsername.setError(null);

        if (password.isEmpty()){
            tilPassword.setError(getResources().getString(R.string.snackbar_fail_empty_field));
            verified = false;
        }
        else if (!password.equals(confirmPassword)) {
            tilPassword.setError(getResources().getString(R.string.snackbar_fail_password_mismatch));
            verified = false;
        }
        else tilPassword.setError(null);

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getResources().getString(R.string.snackbar_fail_empty_field));
            verified = false;
        }
        else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getResources().getString(R.string.snackbar_fail_password_mismatch));
            verified = false;
        }
        else tilConfirmPassword.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError(getResources().getString(R.string.snackbar_fail_empty_field));
            verified = false;
        }
        else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            tilEmail.setError(getResources().getString(R.string.snackbar_fail_email_invalid));
            verified = false;
        }
        else tilEmail.setError(null);

        return verified;
    }

    private void checkResultCode() {
        // invalid signup
        boolean errorDuplicateUsername = resultCode == -1;
        boolean errorDuplicateEmail = resultCode == -2;
        // boolean errorOther = resultCode == 0;

        if (errorDuplicateUsername) {
            Toast.makeText(this, R.string.snackbar_fail_signup_duplicate_username, Toast.LENGTH_SHORT).show();
        }
        else if (errorDuplicateEmail) {
            Toast.makeText(this, R.string.snackbar_fail_signup_duplicate_email, Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == 1) {
            Toast.makeText(this, R.string.snackbar_success_signup, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, R.string.snackbar_success_signup_verify_msg, Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            Toast.makeText(this, R.string.snackbar_fail_signup_unable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void parseJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            resultCode = jsonObject.getInt("signup");
        } catch (JSONException ignored) {}

        checkResultCode();
    }

    @Override
    public String createSignUpPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        contentValues.put("signup", true);
        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("email", email);

        return RequestStringCreator.create(contentValues);
    }
}
