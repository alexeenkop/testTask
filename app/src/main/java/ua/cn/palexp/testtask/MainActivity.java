package ua.cn.palexp.testtask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


public class MainActivity extends Activity {

    ListView lv;
    TextView tv;

    private static String url = "http://echo.jsontest.com/key1/value1/key2/value2/key3/value3/key4/value4";

    int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView);
        tv = (TextView) findViewById(R.id.textView);

        if (isOnline()) {
            new TestTask().execute(url);
        } else {
            tv.setText("Пожалуйста, включите интернет и повторите попытку!");
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            return true; // есть соединение
        } else {
            return false; // нет соединения
        }
    }

    public class TestTask extends AsyncTask<String, Void, String> {

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
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                status = urlConnection.getResponseCode();
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
            Set<Entry<String, String>> keyList = null;

            if (result == "E") {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                tv.setText("Сервер временно не доступен" + "\n" + "Статус сервера: " + status);
            } else {
                JSONObject json = null;

                try {
                    json = new JSONObject(result);
                    HashMap<String, String> str = new HashMap<String, String>();
                    Iterator itr = json.keys();
                    while (itr.hasNext()) {
                        String key = itr.next().toString();
                        str.put(key, (String) json.get(key));
                    }
                    keyList = str.entrySet();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pDialog.isShowing())
                    pDialog.dismiss();


                Comparator<Entry<String, String>> valueComparator = new Comparator<Entry<String, String>>() {
                    @Override
                    public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                        String v1 = e1.getValue();
                        String v2 = e2.getValue();
                        return v1.compareTo(v2);
                    }
                };
                List<Entry<String, String>> listOfKeys = new ArrayList<Entry<String, String>>(keyList);
                Collections.sort(listOfKeys, valueComparator);

                List<String> str2 = new ArrayList<String>();

                for (Entry<String, String> entry : listOfKeys) {
                    str2.add(entry.getValue());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.items, str2);

                lv.setAdapter(adapter);

            }
        }
    }

}
