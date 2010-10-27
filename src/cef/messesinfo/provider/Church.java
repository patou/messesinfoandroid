package cef.messesinfo.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import cef.messesinfo.MessesInfo;

public class Church implements BaseColumns {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String CITY = "city";
    public static final String COMMUNITY = "community";
    public static final String ADDRESS = "address";
    public static final String ZIPCODE = "zipcode";
    public static final String PHONE = "phone";
    public static final String FAX = "fax";
    public static final String EMAIL = "email";
    public static final String URL = "url";
    public static final String MISCELLANEOUS = "miscellaneous";
    public static final String LNG = "lng";
    public static final String LAT = "lat";
    public static final String FAVORITE = "favorite";
    public static final String PICTURE = "picture";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.messesinfo.church_favorite";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.messesinfo.church_favorite";
    public static final String CONTENT_URI_NAME = "church_favorite";
    public static final Uri CONTENT_URI = Uri.parse("content://" + MessesInfo.AUTHORITY + "/" + CONTENT_URI_NAME);
}
