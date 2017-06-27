package com.arnesfield.school.finder.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.arnesfield.school.finder.R;
import com.arnesfield.school.finder.config.TaskConfig;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 06/26.
 */

public final class LogoutUserTask extends AsyncTask<Void, Void, Integer> {
    public static void execute(Context context, boolean doPostExecute) {
        new LogoutUserTask(context, doPostExecute).execute();
    }

    public interface OnLogoutListener {
        void onLoggedOut();
        String createLogoutPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;
    private final ProgressDialog progressDialog;
    private final boolean doPostExecute;

    public LogoutUserTask(Context context, boolean doPostExecute) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        this.doPostExecute = doPostExecute;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            URL url = new URL(TaskConfig.LOGOUT_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((OnLogoutListener)context).createLogoutPostString(new ContentValues());
            bufferedWriter.write(postString);

            // clear
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            int responseCode = httpURLConnection.getResponseCode();
            httpURLConnection.disconnect();

            return responseCode;
        } catch (Exception e) {}
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (doPostExecute) {
            progressDialog.setTitle(R.string.progress_logout_title);
            progressDialog.setMessage(context.getResources().getString(R.string.progress_logout_msg));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (doPostExecute)
            ((OnLogoutListener)context).onLoggedOut();
    }
}
