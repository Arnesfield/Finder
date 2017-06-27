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

public final class CheckForNotifsTask extends AsyncTask<Void, Void, String> {

    public static void execute(Context context) {
        new CheckForNotifsTask(context).execute();
    }

    public interface OnCheckForNotifsListener {
        void parseCheckNotifsJSONString(String jsonString);
        String createCheckNotifsPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;

    public CheckForNotifsTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // set form information
            URL url = new URL(TaskConfig.CHECK_FOR_NOTIFS_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((OnCheckForNotifsListener)context).createCheckNotifsPostString(new ContentValues());
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
    }


    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);
        try {
            ((OnCheckForNotifsListener)context).parseCheckNotifsJSONString(jsonString);
        } catch (Exception e) {}
    }
}
