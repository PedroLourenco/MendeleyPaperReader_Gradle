package com.mendeleypaperreader.sessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.mendeleypaperreader.utl.Globalconstant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Classname: GetAccessToken 
 * 	 
 * 
 * @date July 8, 2014
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class GetAccessToken {
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public GetAccessToken() {
    }

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    DefaultHttpClient httpClient;
    HttpPost httpPost;

    public JSONObject getToken(String address, String token, String client_id,
	    String client_secret, String redirect_uri, String grant_type) {
	// Making HTTP request
	try {
	    // DefaultHttpClient
	    httpClient = new DefaultHttpClient();
	    httpPost = new HttpPost(address);
	    params.add(new BasicNameValuePair("code", token));
	    params.add(new BasicNameValuePair("client_id", client_id));
	    params.add(new BasicNameValuePair("client_secret", client_secret));
	    params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
	    params.add(new BasicNameValuePair("grant_type", grant_type));
	    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
	    httpPost.setEntity(new UrlEncodedFormEntity(params));
	    
	    HttpResponse httpResponse = httpClient.execute(httpPost);
	    HttpEntity httpEntity = httpResponse.getEntity();
	    is = httpEntity.getContent();
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    is, "iso-8859-1"), 8);
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		sb.append(line + "n");
	    }
	    is.close();
	    json = sb.toString();
	    if (Globalconstant.LOG)
		Log.e(Globalconstant.TAG, json);
	} catch (Exception e) {
	    e.getMessage();
	    if (Globalconstant.LOG)
	    	Log.e(Globalconstant.TAG, "Error converting result " + e.toString());
	}
	// Parse the String to a JSON Object
	try {
	    jObj = new JSONObject(json);
	} catch (JSONException e) {
	    if (Globalconstant.LOG)
	    	Log.e(Globalconstant.TAG, "Error parsing data " + e.toString());
	}
	// Return JSON String
	return jObj;
    }
    
    
    public JSONObject refresh_token(String address, String token, String client_id,
	    String client_secret, String redirect_uri, String grant_type, String refresh_token) {
	// Making HTTP request
	try {
	    // DefaultHttpClient
	    httpClient = new DefaultHttpClient();
	    httpPost = new HttpPost(address);
	    params.add(new BasicNameValuePair("code", token));
	    params.add(new BasicNameValuePair("client_id", client_id));
	    params.add(new BasicNameValuePair("client_secret", client_secret));
	    params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
	    params.add(new BasicNameValuePair("grant_type", grant_type));
	    params.add(new BasicNameValuePair("refresh_token", refresh_token));
	    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
	    httpPost.setEntity(new UrlEncodedFormEntity(params));
	    
	    HttpResponse httpResponse = httpClient.execute(httpPost);
	    HttpEntity httpEntity = httpResponse.getEntity();
	    is = httpEntity.getContent();
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    is, "iso-8859-1"), 8);
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		sb.append(line + "n");
	    }
	    is.close();
	    json = sb.toString();
	    if (Globalconstant.LOG)
		Log.e(Globalconstant.TAG, json);
	} catch (Exception e) {
	    e.getMessage();
	    if (Globalconstant.LOG)
		Log.e(Globalconstant.TAG, "Error converting result " + e.toString());
	}
	// Parse the String to a JSON Object
	try {
	    jObj = new JSONObject(json);
	} catch (JSONException e) {
	    if (Globalconstant.LOG)
		Log.e(Globalconstant.TAG, "Error parsing data " + e.toString());
	}
	// Return JSON String
	return jObj;
    }
    
    

    public void savePreferences(Context context, String key, String value,String file_name) {

	SharedPreferences sharedPreferences = context.getSharedPreferences(file_name, Context.MODE_PRIVATE);
	Editor editor = sharedPreferences.edit();
	editor.putString(key, value);
	editor.apply();

    }

    public String LoadPreference(Context context, String Key, String file_name) {

	SharedPreferences sharedPreferences = context.getSharedPreferences(file_name, Context.MODE_PRIVATE);

	return sharedPreferences.getString(Key, "");

    }


    public void deletePreferences(Context context, String Key, String file_name){
	
	SharedPreferences sharedPreferences = context.getSharedPreferences(file_name, Context.MODE_PRIVATE);
	sharedPreferences.edit().remove(Key).commit();

    }

}