package cef.messeinfo.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import cef.messeinfo.MesseInfo;

public class Church implements BaseColumns {
    public static final String CODE = "code";
    public static final String NOM = "nom";
    public static final String COMMUNE = "commune";
    public static final String PAROISSE = "paroisse";
    public static final String ADRESSE = "adresse";
    public static final String CP = "cp";
    public static final String TEL = "tel";
    public static final String FAX = "fax";
    public static final String EMAIL = "email";
    public static final String INTERNET = "internet";
    public static final String LIBRE = "libre";
    public static final String LON = "lon";
    public static final String LAT = "lat";
    public static final String FAVORITE = "favorite";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.messeinfo.church_favorite";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.messeinfo.church_favorite";
    public static final String CONTENT_URI_NAME = "church_favorite";
    public static final Uri CONTENT_URI = Uri.parse("content://" + MesseInfo.AUTHORITY + "/" + CONTENT_URI_NAME);
}
