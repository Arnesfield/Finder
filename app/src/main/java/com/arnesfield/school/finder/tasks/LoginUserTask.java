package com.arnesfield.school.finder.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

public final class LoginUserTask extends AsyncTask<Void, Void, String> {

    public static void execute(Context context) {
        new LoginUserTask(context).execute();
    }

    public interface LoginListener {
        void parseJSONString(String jsonString);
        String createLoginPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;
    private final ProgressDialog progressDialog;

    public LoginUserTask(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // set form information
            URL url = new URL(TaskConfig.LOGIN_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((LoginListener)context).createLoginPostString(new ContentValues());
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
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("Loading login information...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }


    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);
        progressDialog.dismiss();
        ((LoginListener)context).parseJSONString(jsonString);
    }
}
