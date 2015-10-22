package ua.cn.palexp.testtask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends Activity {

    ListView lv;
    TextView tv;

    private static String url = "http://echo.jsontest.com/key1/value1/key2/value2/key3/value3/key4/value4";

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String KEY4 = "key4";

    ArrayList<HashMap<String, String>> keyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView)findViewById(R.id.listView);
        tv = (TextView)findViewById(R.id.textView);
        new TestTask().execute(url);
    }

    public class TestTask extends AsyncTask<String,Void, String> {

        private ProgressDialog pDialog;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Получение данных ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                int status = urlConnection.getResponseCode();
                if (status == 200) {

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    result = buffer.toString();
                } else {
                    result = "E";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == "E") {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                tv.setText("Сервер временно не доступен");
            } else {
                JSONObject json = null;

                try {
                    json = new JSONObject(result);
                    keyList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> str = new HashMap<String, String>();
                    Iterator itr = json.keys();
                    while (itr.hasNext()) {
                        String key = itr.next().toString();

                        str.put(key, (String) json.get(key));
                        keyList.add(str);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pDialog.isShowing())
                    pDialog.dismiss();

                ListAdapter adapter = new SimpleAdapter(
                        MainActivity.this, keyList,
                        R.layout.items, new String[]{KEY4, KEY3, KEY2, KEY1}, new int[]{R.id.tvKey4, R.id.tvKey3, R.id.tvKey2, R.id.tvKey1});

                lv.setAdapter(adapter);
            }
        }
    }

}
