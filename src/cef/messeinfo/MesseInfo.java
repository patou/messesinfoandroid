package cef.messeinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import cef.messeinfo.activity.AboutActivity;
import cef.messeinfo.activity.ChurchBookActivity;
import cef.messeinfo.activity.FavoriteActivity;
import cef.messeinfo.activity.NearMapActivity;
import cef.messeinfo.activity.SearchMassActivity;

public class MesseInfo extends ListActivity {
    /** Called when the activity is first created. */

    /** Attribute key for the list item text. */ 
    private static final String LABEL = "LABEL"; 
    /** Attribute key for the list item icon's drawable resource. */ 
    private static final String ICON  = "ICON"; 

    
    public static final int MENULIST_NEXT_MASS = 0;
    public static final int MENULIST_SEARCH_MASS = 1;
    public static final int MENULIST_FAVORITE = 2;
    public static final int MENULIST_CHURCH_BOOK = 3;
    public static final int MENULIST_PHONE_MESSEINFO = 4;
    
    
    public static final int MENU_CONTACT = 1;
    public static final int MENU_WEBSITE = 2;
    public static final int MENU_ABOUT = 3;
    public static final String AUTHORITY = "cef.messeinfo";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        List< Map<String,Object> > menulist = buildList();
        SimpleAdapter adapter = new SimpleAdapter( 
                // the Context 
                this, 
                // the data to display 
                menulist, 
                // The layout to use for each item 
                  R.layout.main_menu_item, 
                  // The list item attributes to display 
                  new String[] { LABEL, ICON }, 
                  // And the ids of the views where they should be displayed (same order) 
                  new int[] { android.R.id.text1, android.R.id.icon } 
           ); 

           setListAdapter( adapter ); 
           setContentView(R.layout.main);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
        case MENULIST_NEXT_MASS:
            NearMapActivity.activityStart(MesseInfo.this);
            break;
        case MENULIST_SEARCH_MASS:
            SearchMassActivity.activityStart(MesseInfo.this);
            break;
        case MENULIST_FAVORITE:
            FavoriteActivity.activityStart(MesseInfo.this);
            break;
        case MENULIST_CHURCH_BOOK:
            ChurchBookActivity.activityStart(MesseInfo.this);
            break;
        case MENULIST_PHONE_MESSEINFO:
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0892251212"));
            startActivity(intent);
            break;
        default:
            break;
        }
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean supRetVal = super.onCreateOptionsMenu(menu);
        
        menu.add(0, MENU_CONTACT, 0, getString(R.string.main_menu_contact)).setIcon(R.drawable.contact);
        menu.add(0, MENU_WEBSITE, 0, getString(R.string.main_menu_website)).setIcon(R.drawable.web);
        menu.add(0, MENU_ABOUT, 0, getString(R.string.main_menu_about)).setIcon(R.drawable.icon_mini);
        return supRetVal;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case MENU_CONTACT:
            //Uri uri = Uri.parse("mailto://contact@messesinfo.cef.fr");
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
            emailIntent.setType("plain/text"); 
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"contact@messesinfo.cef.fr"}); 
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contact depuis l'application Android"); 
//            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "myBodyText"); 
            startActivity(Intent.createChooser(emailIntent, "Contact messeinfo"));
//            startActivity(new Intent(Intent.ACTION_VIEW, uri)); 
            return true;
        case MENU_WEBSITE:
            final Intent urlIntent = new Intent(android.content.Intent.ACTION_VIEW);
            urlIntent.setData(Uri.parse(getString(R.string.messeinfo_url)));
            startActivity(urlIntent);
            return true;
        case MENU_ABOUT:
            AboutActivity.activityStart(MesseInfo.this);
            return true;
        default:
            break;
        }
        return false;
    }
    
    private List< Map<String,Object> > buildList() { 
        // Resulting list... 
        List< Map<String,Object> > list = new ArrayList< Map<String,Object> >( 5 ); 
         
        Map<String,Object> map1 = new HashMap<String,Object>(); 
        map1.put( LABEL, getString(R.string.menu_next_mass) ); 
        map1.put( ICON, R.drawable.church2 ); 
        list.add( map1 ); 
        Map<String,Object> map2 = new HashMap<String,Object>(); 
        map2.put( LABEL, getString(R.string.menu_search_mass) ); 
        map2.put( ICON, R.drawable.bible ); 
        list.add( map2 );
        Map<String,Object> map3 = new HashMap<String,Object>(); 
        map3.put( LABEL, getString(R.string.menu_favorite) ); 
        map3.put( ICON, R.drawable.favorites ); 
        list.add( map3 ); 
        Map<String,Object> map4 = new HashMap<String,Object>(); 
        map4.put( LABEL, getString(R.string.menu_church_book) ); 
        map4.put( ICON, R.drawable.church1 ); 
        list.add( map4 ); 
        Map<String,Object> map5 = new HashMap<String,Object>(); 
        map5.put( LABEL, getString(R.string.menu_phone_messeinfo) ); 
        map5.put( ICON, R.drawable.phone ); 
        list.add( map5 );
            return list; 
       } 
}