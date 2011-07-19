package com.gintellect.temp;


import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class ResourceTask implements Runnable {
    private static final String TAG = "ResourceTask";
    private final Resource resource;
    private final TaskType task;

    public enum TaskType {
        index,
        create
    };

    ResourceTask(Resource resource, TaskType task)  {
        this.resource = resource;
        this.task = task;
    }

    public void run() {
        String result;
        switch (task) {
            case index:
                result = doGetResource();
                resource.setLabel(result);
                break;
            case create:
                result = doPostResource();
                resource.setLabel(result);
                break;
        }
    }

    private String doGetResource() {
        String result = "error";
        HttpURLConnection con = null;
        Log.d(TAG,"doGetResources");

        try {
            if (Thread.interrupted())
                throw new InterruptedException();
            //Build restful query
            URL url = new URL("http://ba4541d8.dotcloud.com/resource/index.json");
            con = (HttpURLConnection)url.openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(15000);
            con.setRequestMethod("GET");
            con.addRequestProperty("Referer","http://www.360scheduling.com/android-aws");
            con.setDoInput(true);
            //Start the Query
            con.connect();
            //Check if task has been interrupted
            if(Thread.interrupted())
                throw new InterruptedException();
            //Read the results from the query
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String payload = reader.readLine();
            Log.d(TAG, payload);
            reader.close();
            //Parse to get translated text
            JSONArray jsonArray = new JSONArray(payload);
            String temp = "";
            for (int i = 0; i < jsonArray.length(); i++)
            {
                temp += jsonArray.getJSONObject(i).getString("first_name") + "\n";
            }

            result = temp;
        }
        catch(IOException e) {
            Log.e(TAG,"IOException", e);
        }
        catch (JSONException e) {
            Log.e(TAG,"JSONException",e);
        }
        catch (InterruptedException e) {
            Log.d(TAG,"InterruptedException",e);
            result = "Interrupted";
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
        }

        Log.d(TAG, " => returned " + result);
        return result;
    }

    private String doPostResource() {
        try
        {
            int TIMEOUT_MILLISEC = 10000;

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,TIMEOUT_MILLISEC );
            HttpClient client = new DefaultHttpClient(httpParams);

            HttpPost request = new HttpPost("http://ba4541d8.dotcloud.com/resource.json");


            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("first_name", "blah");
            jsonObj.put("surname", "blah1");
            jsonArray.put(jsonObj);

            jsonObj = new JSONObject();
            jsonObj.put("first_name", "aaablah");
            jsonObj.put("surname", "aaaablah1");
            jsonArray.put(jsonObj);

            request.setEntity(new ByteArrayEntity(jsonArray.toString().getBytes("UTF8")));
            HttpResponse response = client.execute(request);

        }
        catch (JSONException e)
        {
            Log.e(TAG,"JSONException",e);
            return "ERROR";
        }
        catch (IOException e)
        {
            Log.e(TAG,"IOException",e);
            return "ERROR";
        }
        return "OK";
    }

}
