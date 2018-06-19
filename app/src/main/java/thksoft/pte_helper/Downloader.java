package thksoft.pte_helper;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by TrNguyen on 22/02/2018.
 */

public class Downloader extends AsyncTask<String, String, String> {

    protected String doInBackground(String... urls) {
        String text = "";
        try {
            URL url = new URL(urls[0]);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                text += Utilities.ExtractContent(str) + "\n";
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    @Override
    protected void onPostExecute(String result) {

    }
}
