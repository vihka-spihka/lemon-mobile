package ru.example.telepuzik.gif;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Telepuzik on 31.03.15.
 */
public class DocsLoader extends AsyncTask<String, Integer, String[]> {

    private static final String DEFAULT_URL = "https://docs.google.com/feeds/default/private/full";
    private final static String HEADER_PREFIX_OAUTH = "OAuth ";

    private ProgressDialog progressDialog;
    private Activity act;

    public DocsLoader(Activity act) {
        Log.d("VkDemoApp", "DOCs DocsLoader");
        this.act = act;
    }

    @Override
    protected void onPreExecute() {
        Log.d("VkDemoApp", "DOCs OnPreExecute");
        progressDialog = new ProgressDialog(act);
        progressDialog.setMessage("Downloading docs list...");
        progressDialog.show();
    }

    @Override
    protected String[] doInBackground(String... params) {
        Log.d("VkDemoApp", "DOCs doInBackground");
        String rez[] = null;
        try {
            String resp = request(DEFAULT_URL, params[0]);
            Log.d("VkDemoApp", "resp "+resp);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(resp)));
            NodeList titles = doc.getElementsByTagName("title");
            rez = new String[titles.getLength()];
            Log.d("VkDemoApp", "length "+titles.getLength());
            for (int i=0; i<titles.getLength(); i++) {
                Node n = titles.item(i);
                rez[i] = n.getTextContent();
                Log.d("VkDemoApp", "Node n "+n);
                //Log.d("VkDemoApp", "rez[i] "+rez[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("VkDemoApp", "rez "+rez);
        return rez;
    }

    @Override
    protected void onPostExecute(String[] result) {
        Log.d("VkDemoApp", "DOCs onPostExecute");
        progressDialog.dismiss();
    }

    private String request(String url, String token) {
        Log.d("VkDemoApp", "=====>DOCs request<====");
        Log.d("VkDemoApp", "token " + token);
        Log.d("VkDemoApp", "url " + url);
        StringBuilder sb = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            Log.d("VkDemoApp", "conn " + conn);
            if (conn instanceof HttpsURLConnection) {
                Log.d("VkDemoApp", "yes");
                ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {

                    public boolean verify(String hostname, SSLSession session) {
                        Log.d("VkDemoApp", "true");
                        return true;
                    }
                });
            }
            Log.d("VkDemoApp", "========>");
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("GData-Version", "3.0");
            conn.setRequestProperty("Authorization", HEADER_PREFIX_OAUTH + token);
            Log.d("VkDemoApp", "token " + token);
            Log.d("VkDemoApp", "conn " + conn);
            Log.d("VkDemoApp", "conn.getResponseCode() " + conn.getResponseCode());//201
            Log.d("VkDemoApp", "HttpURLConnection.HTTP_OK " + HttpURLConnection.HTTP_OK);//200
            int status = conn.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    //StringBuilder sb = new StringBuilder();
                    String nextLine;
                    while ((nextLine = in.readLine()) != null) {
                        Log.d("VkDemoApp", "next line "+nextLine );
                        sb.append(nextLine);
                        Log.d("VkDemoApp", "sb if"+sb.toString());
                    }
                    in.close();
                    conn.disconnect();
                    Log.d("VkDemoApp", "sb exit"+sb.toString());
                    return sb.toString();
            }

/*
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("VkDemoApp", "EXCEPTION ");
                throw new Exception("http error " + conn.getResponseCode());
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String nextLine = null;
            Log.d("VkDemoApp", "null" + in.readLine());
            while ((nextLine = in.readLine()) != null) {
                Log.d("VkDemoApp", "next line " + nextLine);
                sb.append(nextLine);
                Log.d("VkDemoApp", "sb" + sb.toString());
            }
            in.close();
            conn.disconnect();
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("VkDemoApp", "<========");
        Log.d("VkDemoApp", "sb" + sb.toString());
        //return sb.toString();
        return null;
    }
}