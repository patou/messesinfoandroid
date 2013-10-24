package cef.messesinfo.ads;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cef.messesinfo.R;

/**
 * Created by sfeir on 19/10/13.
 */
public class AdsWebDialog extends Dialog {
    private String deliveryURL = "http://pub.cef.fr/script/www/delivery/afr.php?zoneid=23&cb=";
    private WebView webView;
    private Random prng = new Random();
    final Timer t = new Timer();
    public AdsWebDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setContentView(R.layout.ads_webview);
        setUpWebView();
        ImageButton closeAds = (ImageButton) findViewById(R.id.closeAds);
        closeAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseAdsClick(v);
            }
        });
    }

    private String createAdsHtml() {
//        return "<html><head><style>* {padding: 0; margin: 0; background-color: transparent;}</style></head>\n"
//                + "<body>%s</body></html>";
        return deliveryURL + prng.nextLong();
    }

    private void setUpWebView()
    {
        webView = (WebView) findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);

        //webView.setBackgroundColor(0x00000000); // transparent
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
       // webView.setWebChromeClient(new OpenXAdWebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                displayAdd();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("afr.php")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    getContext().startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        String mUrl = createAdsHtml();
        System.out.println(" mURL = " + mUrl);

        webView.loadUrl(mUrl);
    }

    public void onCloseAdsClick(View v) {
       dismiss();
       t.cancel();
    }

    public void displayAdd() {
        show();
        t.schedule(new TimerTask() {
            public void run() {
                dismiss();
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 10000);
    }
}
