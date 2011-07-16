package com.gintellect.temp;


import android.content.ComponentName;
import android.util.Log;
import org.apache.http.client.methods.HttpPost;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PrivateKey;

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
            URL url = new URL("http://10.0.2.2:9000/resource/index.json");
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
        String result = "error";
        HttpURLConnection con = null;
        Log.d(TAG,"doPostResources");

        try {
            if (Thread.interrupted())
                throw new InterruptedException();
            //Build restful query
            URL url = new URL("http://10.0.2.2:9000/resource.json");
            con = (HttpURLConnection)url.openConnection();

/*            con.setReadTimeout(10000);
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.addRequestProperty("Referer","http://www.360scheduling.com/android-aws");
            con.setDoInput(true);
            //Start the Query
            con.connect();*/

            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.addRequestProperty("Referer","http://www.360scheduling.com/android-aws");
            con.setDoOutput(true);
            con.setChunkedStreamingMode(0);
            con.addRequestProperty("Content-Type", "application/json");
            con.connect();

            OutputStream output = null;
            try {
              output = con.getOutputStream();

                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("first_name", "blah");
                jsonObj.put("surname", "blah1");
                jsonArray.put(jsonObj);

                jsonObj = new JSONObject();
                jsonObj.put("first_name", "aaablah");
                jsonObj.put("surname", "aaaablah1");
                jsonArray.put(jsonObj);

                output.write(jsonArray.toString().getBytes());
            } finally {
              if (output != null) { output.close(); }
            }

            int status = con.getResponseCode();
            System.out.println("" + status);

            con.disconnect();


            //Check if task has been interrupted
            if(Thread.interrupted())
                throw new InterruptedException();
            //Read the results from the query
/*            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String payload = reader.readLine();
            Log.d(TAG,payload);
            reader.close();
            //Parse to get translated text
            JSONArray jsonArray = new JSONArray(payload);
            String temp = "";
            for (int i = 0; i < jsonArray.length(); i++)
            {
                temp += jsonArray.getJSONObject(i).getString("first_name") + "\n";
            }

            result = temp;   */
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
}
