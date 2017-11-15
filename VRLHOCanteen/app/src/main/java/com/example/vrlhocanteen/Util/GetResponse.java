package com.example.vrlhocanteen.Util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anirudh on 10/01/17.
 */

public class GetResponse {

    public JSONArray getResponse(String url){
        JSONArray array= null;
        String responseText;
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("PERSON_NAME", ""));
            /*nameValuePairs.add(new BasicNameValuePair("APP_VERSION", AppUtil.AppDetails.APP_VERSION));
            nameValuePairs.add(new BasicNameValuePair("APP_TYPE", AppUtil.AppDetails.APP_TYPE));*/
            nameValuePairs.add(new BasicNameValuePair("OS", "1"));
            nameValuePairs.add(new BasicNameValuePair("SORT_TYPE", "1"));
            nameValuePairs.add(new BasicNameValuePair("14092016", "1"));
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            responseText = EntityUtils.toString(httpEntity);
            array = new JSONArray(responseText);
        }catch (Exception ee){
            Log.e("GetHttpResponse",ee.toString());
        }
        return array;
    }

    public JSONArray getResponse(String url, List<NameValuePair> nameValuePairs ){
        JSONArray array;
        String responseText;
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            responseText = EntityUtils.toString(httpEntity);
            array = new JSONArray(responseText);
        }catch (Exception ee) {
            array = new JSONArray();
            Log.e("GetHttpResponse",ee.toString());
        }
        return array;
    }

    public String getResponse(String url, List<NameValuePair> nameValuePairs, String id){
        String responseText;
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            responseText = EntityUtils.toString(httpEntity);
        }catch (Exception ee){
            responseText = ee.toString();
            Log.e("GetHttpResponse str",ee.toString());
        }

        return responseText;
    }
}
