import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterRequest extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String data = "";
        String USER_CREATE_POST = "http://35.231.79.120:8000/api/auth/users/create";

        int status = 0;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(USER_CREATE_POST).openConnection();
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setDoInput (true); //Do I?
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content-Type","application/json");

            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            wr.writeBytes(params[0]);
            wr.flush();
            wr.close();

            status = httpURLConnection.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        Log.d("STATUS", String.valueOf(status));
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.e("POST_EXECUTE", result); // this is expecting a response code to be sent from your server upon receiving the POST data
    }

    /*
    @Override
    protected void onCancelled() {
        mAuthTask = null;
        showProgress(false);
    }
    */
}
