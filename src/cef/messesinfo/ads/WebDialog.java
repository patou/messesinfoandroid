//package cef.messesinfo.ads;
//
//import android.content.res.Resources;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.Random;
//
//public class WebDialog extends Dialog
//{
//
//    static final int                      BLUE                  = 0xFF6D84B4;
//    static final float[]                  DIMENSIONS_DIFF_LANDSCAPE =
//                                                                    { 20, 60 };
//    static final float[]                  DIMENSIONS_DIFF_PORTRAIT  =
//                                                                    { 40, 60 };
//    static final FrameLayout.LayoutParams   FILL                    = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//    static final int                      MARGIN                    = 4;
//    static final int                      PADDING                   = 2;
//    static final String                   DISPLAY_STRING            = "touch";
//
//    private String                        mUrl;
////  private DialogListener                mListener;
//    private WebView webView;
//    private LinearLayout                  mContent;
//
//    private static final String HTML_DOCUMENT_TEMPLATE = "<html><head><style>* {padding: 0; margin: 0; background-color: transparent;}</style></head>\n"
//            + "<body>%s</body></html>";
//
//    private static final String IMG_TAG = "<a href='%1$s/ck.php?n=a186f406&amp;cb=%4$d' target='_blank'>\n" +
//            "<img src='%1$s/avw.php?zoneid=%2$d&amp;cb=%4$d&amp;n=a186f406&amp;charset=UTF-8' border='0' alt='' />\n" +
//            "</a>";
//
//    private String deliveryURL = "http://pub.cef.fr/script/www/delivery";
//
//    private String jsTagURL = "ajs.php";
//
//    private Integer zoneID;
//
//    private boolean hasHTTPS = false;
//
//    private String source;
//
//    private Random prng = new Random();
//
//    private Resources res;
//
//    public WebDialog(Context context, String url)
//    {
//        super(context);
//        mUrl = url;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        mContent = new LinearLayout(getContext());
//        mContent.setOrientation(LinearLayout.VERTICAL);
//        setUpWebView();
//        Display display = getWindow().getWindowManager().getDefaultDisplay();
//        final float scale = getContext().getResources().getDisplayMetrics().density;
//        int orientation = getContext().getResources().getConfiguration().orientation;
//        float[] dimensions = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? DIMENSIONS_DIFF_LANDSCAPE : DIMENSIONS_DIFF_PORTRAIT;
//        addContentView(mContent, new LinearLayout.LayoutParams(display.getWidth() - ((int) (dimensions[0] * scale + 0.5f)), display.getHeight() - ((int) (dimensions[1] * scale + 0.5f))));
//    }
//
//    private void setUpWebView()
//    {
//        webView = new WebView(getContext());
//        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setPluginsEnabled(true);
//        settings.setAllowFileAccess(false);
//        settings.setPluginState(WebSettings.PluginState.ON);
//
//        webView.setBackgroundColor(0x00000000); // transparent
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setWebChromeClient(new OpenXAdWebChromeClient());
//        webView.setWebViewClient(new WebDialog.DialogWebViewClient());
//
//        System.out.println(" mURL = " + mUrl);
//
//        webView.loadUrl(mUrl);
//        webView.setLayoutParams(FILL);
//        mContent.addView(webView);
//    }
//
//    private class DialogWebViewClient extends WebViewClient
//    {
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url)
//        {
//            view.loadUrl(url);
//
//            return true;
//        }
//
//        @Override
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
//        {
//            super.onReceivedError(view, errorCode, description, failingUrl);
//            WebDialog.this.dismiss();
//        }
//
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon)
//        {
//            super.onPageStarted(view, url, favicon);
//            mSpinner.show();
//        }
//
//        @Override
//        public void onPageFinished(WebView view, String url)
//        {
//            super.onPageFinished(view, url);
//            String title = webView.getTitle();
//            if (title != null && title.length() > 0)
//            {
//                mTitle.setText(title);
//            }
//            mSpinner.dismiss();
//        }
//
//    }
//
//    private void initWebView() {
//        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setPluginsEnabled(true);
//        settings.setAllowFileAccess(false);
//        settings.setPluginState(WebSettings.PluginState.ON);
//
//        webView.setBackgroundColor(0x00000000); // transparent
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setWebChromeClient(new OpenXAdWebChromeClient());
//
//        addView(webView);
//    }
//
//    protected String getZoneTemplate(int zoneID) {
//        try {
//            String zoneTag = String.format(IMG_TAG,
//                    (hasHTTPS ? "https://" : "http://") + deliveryURL + '/' + jsTagURL,
//                    zoneID,
//                    source == null ? "" : URLEncoder.encode(source, "utf-8"),
//                    prng.nextLong());
//            String raw = String.format(HTML_DOCUMENT_TEMPLATE, zoneTag);
//            return raw;
//        }
//        catch (UnsupportedEncodingException e) {
//            Log.wtf(LOGTAG, "UTF-8 not supported?!", e);
//        }
//
//        return null;
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right,
//                            int bottom) {
//        webView.layout(left, top, right, bottom);
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        load();
//    }
//
//    /**
//     * Load ad from OpenX server using the parameters that were set previously.
//     * This will not work if the following minimum required parameters were not
//     * set: delivery_url and zone_id.
//     */
//    public void load() {
//        if (zoneID != null) {
//            load(zoneID);
//        }
//        else {
//            Log.w(LOGTAG, "zoneID is empty");
//        }
//    }
//
//    /**
//     * Load ad from OpenX server using the parameters that were set previously
//     * and the supplied zoneID. This will not work if the required parameter
//     * delivery_url was not set.
//     *
//     * @see #load()
//     * @param zoneID ID of OpenX zone to load ads from.
//     */
//    public void load(int zoneID) {
//        // check required parameters
//        if (deliveryURL != null) {
//            webView.loadDataWithBaseURL(null, getZoneTemplate(zoneID), "text/html", "utf-8", null);
//        }
//        else {
//            Log.w(LOGTAG, "deliveryURL is empty");
//        }
//    }
//
//    public String getDeliveryURL() {
//        return deliveryURL;
//    }
//
//    /**
//     * The path to server and directory containing OpenX delivery scripts in the
//     * form servername/path. This parameter is required. Example:
//     * openx.example.com/delivery.
//     *
//     * @param deliveryURL
//     */
//    public void setDeliveryURL(String deliveryURL) {
//        this.deliveryURL = deliveryURL;
//    }
//
//    private void setDeliveryURL(AttributeSet attrs) {
//        int delivery_url = attrs.getAttributeResourceValue(ATTRS_NS, PARAMETER_DELIVERY_URL, -1);
//        if (delivery_url != -1) {
//            this.deliveryURL = res.getString(delivery_url);
//        }
//        else {
//            this.deliveryURL = attrs.getAttributeValue(ATTRS_NS, PARAMETER_DELIVERY_URL);
//        }
//    }
//
//    public String getJsTagURL() {
//        return jsTagURL;
//    }
//
//    /**
//     * The name of OpenX script that serves ad code for simple JavaScript type
//     * tag. Default: ajs.php. This parameter usually does not need to be
//     * changed.
//     *
//     * @param jsTagURL
//     */
//    public void setJsTagURL(String jsTagURL) {
//        this.jsTagURL = jsTagURL;
//    }
//
//    private void setJsTagURL(AttributeSet attrs) {
//        int js_tag_url_id = attrs.getAttributeResourceValue(ATTRS_NS, PARAMETER_JS_TAG_URL, -1);
//        if (js_tag_url_id != -1) {
//            this.jsTagURL = res.getString(js_tag_url_id);
//        }
//        else {
//            String js_tag_url = attrs.getAttributeValue(ATTRS_NS, PARAMETER_JS_TAG_URL);
//            if (js_tag_url != null) {
//                this.jsTagURL = js_tag_url;
//            }
//        }
//    }
//
//    public Integer getZoneID() {
//        return zoneID;
//    }
//
//    /**
//     * The ID of OpenX zone from which ads should be selected to display inside
//     * the widget. This parameter is required unless you use load(int) method.
//     *
//     * @param zoneID
//     */
//    public void setZoneID(Integer zoneID) {
//        this.zoneID = zoneID;
//    }
//
//    private void setZoneID(AttributeSet attrs) {
//        int zone_id_rs = attrs.getAttributeResourceValue(ATTRS_NS, PARAMETER_ZONE_ID, -1);
//        if (zone_id_rs != -1) {
//            this.zoneID = new Integer(res.getInteger(zone_id_rs));
//        }
//        else {
//            int zone_id = attrs.getAttributeIntValue(ATTRS_NS, PARAMETER_ZONE_ID, -1);
//            if (zone_id != -1) {
//                this.zoneID = new Integer(zone_id);
//            }
//        }
//    }
//
//    public boolean hasHTTPS() {
//        return hasHTTPS;
//    }
//
//    /**
//     * Set this to true if ads should be served over HTTPS protocol. Default:
//     * false.
//     *
//     * @param hasHTTPS
//     */
//    public void setHasHTTPS(boolean hasHTTPS) {
//        this.hasHTTPS = hasHTTPS;
//    }
//
//    private void setHasHTTPS(AttributeSet attrs) {
//        int has_https = attrs.getAttributeResourceValue(ATTRS_NS, PARAMETER_HAS_HTTPS, -1);
//        if (has_https != -1) {
//            this.hasHTTPS = res.getBoolean(has_https);
//        }
//        else {
//            this.hasHTTPS = attrs.getAttributeBooleanValue(ATTRS_NS, PARAMETER_HAS_HTTPS, false);
//        }
//    }
//
//    public String getSource() {
//        return source;
//    }
//
//    /**
//     * This parameter can be used to target ads by its value. It is optional.
//     *
//     * @param source
//     */
//    public void setSource(String source) {
//        this.source = source;
//    }
//
//    private void setSource(AttributeSet attrs) {
//        int source_id = attrs.getAttributeResourceValue(ATTRS_NS, PARAMETER_SOURCE, -1);
//        if (source_id != -1) {
//            this.source = res.getString(source_id);
//        }
//        else {
//            this.source = attrs.getAttributeValue(ATTRS_NS, PARAMETER_SOURCE);
//        }
//    }
//}