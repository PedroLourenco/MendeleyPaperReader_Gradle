package com.mendeleypaperreader.parser;

import android.util.Log;

import com.mendeleypaperreader.db.Data;
import com.mendeleypaperreader.util.Globalconstant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class JSONParser {

    public static String POST = "POST";
    public static String GET = "GET";
    public static String DELETE = "DELETE";



    List<InputStream> jacksonArray = new ArrayList<InputStream>();


    public String header(org.apache.http.Header[] header) {

        String aux = null;

        if (header.length > 0) {

            if (header[0].toString().contains("rel=\"next\"")) {

                Pattern pattern = Pattern.compile("\\<(.+?)\\>");
                Matcher matcher = pattern.matcher(header[0].toString());


                while (matcher.find()) {

                    aux = matcher.group(1);
                }
                return aux;
            }
        }

        return "finish";

    }


    public List<InputStream> getJACKSONFromUrl(String url, String method,
                                               List<NameValuePair> params, boolean with_header) {
        // Making HTTP request

        if (url.equals("finish")) {
            return jacksonArray;
        }

        //InputStream content = null;
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();


        HttpGet httpGet = new HttpGet(url);
        try {


            // check for request method
            if (method.equals(POST)) {
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                //httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpGet.setHeader("Content-Type", "application/json");
                httpGet.setHeader("X-Mendeley-Trace-Id", "FdKj-Sb_ud4");
                httpGet.setHeader("Access-Control-Expose-Headers ", "Date,Content-Type,Transfer-Encoding,X-Mendeley-Trace-Id");
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                //if(httpEntity.getContent() != null)


                //   jacksonArray.add(httpEntity.getContent());


            }else if(method.equals(DELETE)){
                Log.d("TAG", "DELETE");
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpDelete httpDelete = new HttpDelete(url);
                //httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpGet.setHeader("Content-Type", "application/json");
                httpGet.setHeader("X-Mendeley-Trace-Id", "SjC87-R8vac");
                httpGet.setHeader("Access-Control-Expose-Headers ", "Date,Content-Type,Transfer-Encoding,X-Mendeley-Trace-Id");
                HttpResponse httpResponse = httpClient.execute(httpDelete);
                HttpEntity httpEntity = httpResponse.getEntity();

                //if(httpEntity.getContent() != null)


                  // jacksonArray.add(httpEntity.getContent());



            } else {


                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    jacksonArray.add(entity.getContent());

                    if (with_header) {
                        String link = header(response.getHeaders("Link"));
                        getJACKSONFromUrl(link, GET, null, true);
                    }

                } else {
                    if (Globalconstant.LOG)
                        Log.e(Globalconstant.TAG, "Failed to download file");
                }

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Globalconstant.LOG)
            Log.e(Globalconstant.TAG, "builder.toString(): " + builder.toString());

        return jacksonArray;
    }



    public int getHTTPRequestError(String url, String method, List<NameValuePair> params, boolean with_header) {

        //InputStream content = null;
        int statusCode = 0;
        HttpClient client = new DefaultHttpClient();


        HttpGet httpGet = new HttpGet(url);
        try {


            // check for request method
            if (method.equals(POST)) {
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                //httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpGet.setHeader("Content-Type", "application/json");
                httpGet.setHeader("X-Mendeley-Trace-Id", "FdKj-Sb_ud4");
                httpGet.setHeader("Access-Control-Expose-Headers ", "Date,Content-Type,Transfer-Encoding,X-Mendeley-Trace-Id");
                HttpResponse httpResponse = httpClient.execute(httpPost);
                StatusLine statusLine = httpResponse.getStatusLine();

                statusCode = statusLine.getStatusCode();


            }else if(method.equals(DELETE)){
                Log.d("TAG", "DELETE");
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpDelete httpDelete = new HttpDelete(url);
                //httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpGet.setHeader("Content-Type", "application/json");
                httpGet.setHeader("X-Mendeley-Trace-Id", "SjC87-R8vac");
                httpGet.setHeader("Access-Control-Expose-Headers ", "Date,Content-Type,Transfer-Encoding,X-Mendeley-Trace-Id");
                HttpResponse httpResponse = httpClient.execute(httpDelete);
                StatusLine statusLine = httpResponse.getStatusLine();

                statusCode = statusLine.getStatusCode();



            } else {


                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                statusCode = statusLine.getStatusCode();


            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return statusCode;
    }




}