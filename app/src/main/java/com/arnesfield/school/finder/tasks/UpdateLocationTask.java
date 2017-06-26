package com.arnesfield.school.finder.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

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

public final class UpdateLocationTask extends AsyncTask<Void, Void, Integer> {
    public static void execute(Context context) {
        new UpdateLocationTask(context).execute();
    }

    public interface OnUpdateLocationListener {
        String createLocationPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;

    public UpdateLocationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            URL url = new URL(TaskConfig.UPDATE_LOCATION_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((OnUpdateLocationListener)context).createLocationPostString(new ContentValues());
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

}
