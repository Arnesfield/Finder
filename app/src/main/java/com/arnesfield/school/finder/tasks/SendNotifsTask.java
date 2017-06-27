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

public final class SendNotifsTask extends AsyncTask<Void, Void, Integer> {
    public static void execute(Context context) {
        new SendNotifsTask(context).execute();
    }

    public interface OnSendNotifsListener {
        void onSentNotif();
        String createNotifsPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;
    private final ProgressDialog progressDialog;

    public SendNotifsTask(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            URL url = new URL(TaskConfig.SEND_NOTIF_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((OnSendNotifsListener)context).createNotifsPostString(new ContentValues());
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
        progressDialog.setTitle(R.string.progress_notifs_send_title);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_notifs_send_msg));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        progressDialog.dismiss();
        ((OnSendNotifsListener)context).onSentNotif();
    }
}
