package cef.messeinfo.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cef.messeinfo.R;

public class WebViewActivity extends Activity {
    private WebView webView;
    private ProgressDialog mProgressDialog;

    public static void startActivity(Context context, Map<String,String> nameValuePairs) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.setData(Uri.parse("http://messesinfo.catholique.fr/public/recherche.php?mode=complement&rub=0"));
        intent.putExtra("postData", (Serializable) nameValuePairs);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.search_mass_loading_title);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.search_mass_loading));
        mProgressDialog.show();
        setContentView(R.layout.web_view);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollTo(10, 408);
                
            };
        });
        Map<String,String> postData = (Map<String,String>) intent.getSerializableExtra("postData");
        String uri = intent.getDataString();
        loadUrlPostData(uri, postData);
    }

    protected void loadUrlPostData(final String uri, final Map<String,String> postData) {
        Log.e("messeinfo", postData.toString());
        new Thread(new Runnable() {
            private HttpClient client;
            private HttpPost postMethod;

            @Override
            public void run() {
                try {
                    client = new DefaultHttpClient();
                    postMethod = new HttpPost(uri);
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
                    for (String key : postData.keySet()) {
                        nameValuePairs.add(new BasicNameValuePair(key, postData.get(key)));  
                    }
                    postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
                    //postMethod.setEntity(new StringEntity(postData));
                    // execute HTTP POST request
                    HttpResponse response = client.execute(postMethod);
                    HttpEntity entity = response.getEntity();
                    final String html = new String(convertStreamToByteArray(entity.getContent()), "ISO-8859-1");
                    // check status code
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadDataWithBaseURL(uri, html, "text/html", "utf-8", null);
                                mProgressDialog.hide();
                            }
                        });
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public byte[] convertStreamToByteArray(InputStream in) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int bytesRead = 0;
                int bytesToRead = 1024;
                byte[] input = new byte[bytesToRead];
                try {
                    while (true) {
                        int result = in.read(input);//, bytesRead, bytesToRead);
                        if (result == -1)
                            break;
                        out.write(input, 0, result);
                        bytesRead += result;
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return out.toByteArray();
            }
        }).start();
    }

    private Handler handler = new Handler();
}
