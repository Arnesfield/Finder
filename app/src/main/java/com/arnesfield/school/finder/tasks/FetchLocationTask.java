package com.arnesfield.school.finder.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.arnesfield.school.finder.config.TaskConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 06/07.
 */

public final class FetchLocationTask extends AsyncTask<String, Integer, String> {

    public static void execute(Context context) {
        new FetchLocationTask(context).execute();
    }

    public interface OnPostExecuteListener {
        void parseJSONString(String jsonString);
    }

    private final Context context;
    private final ProgressDialog progressDialog;

    private FetchLocationTask(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(TaskConfig.FETCH_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);

            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            return stringBuilder.toString();
        } catch (Exception ignored) {}

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading location data...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);
        progressDialog.dismiss();
        ((OnPostExecuteListener)context).parseJSONString(jsonString);
    }
}
