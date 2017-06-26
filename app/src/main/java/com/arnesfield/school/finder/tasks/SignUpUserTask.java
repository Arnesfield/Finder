package com.arnesfield.school.finder.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.arnesfield.school.finder.R;
import com.arnesfield.school.finder.config.TaskConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 06/25.
 */

public final class SignUpUserTask extends AsyncTask<Void, Void, String> {

    public static void execute(Context context) {
        new SignUpUserTask(context).execute();
    }

    public interface OnSignUpListener {
        void parseJSONString(String jsonString);
        String createSignUpPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;
    private final ProgressDialog progressDialog;

    public SignUpUserTask(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // set form information
            URL url = new URL(TaskConfig.ADD_USER_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((OnSignUpListener)context).createSignUpPostString(new ContentValues());
            bufferedWriter.write(postString);

            // clear
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);

            // clear
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();

            return stringBuilder.toString();
        } catch (Exception e) {}
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setTitle(R.string.progress_signup_title);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_signup_msg));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }


    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);
        progressDialog.dismiss();
        ((OnSignUpListener)context).parseJSONString(jsonString);
    }
}
