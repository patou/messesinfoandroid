package cef.messesinfo.activity;

import java.util.ArrayList;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.provider.Church;
import cef.messesinfo.provider.Schedule;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ChurchActivity extends TabActivity {

    private static final String HORAIRE = "horaire";
    private static final String INFORMATION = "information";
    private static final int MENU_MAPS = 0;
    GoogleAnalyticsTracker tracker;
    protected Map<String, String> selectedItem;

    public static void activityStart(Context context, String code) {
	Intent intent = new Intent(context, ChurchActivity.class);
	intent.putExtra(Church.ID, code);
	context.startActivity(intent);
    }

    public static void activityStartSchedule(Context context, String code) {
	Intent intent = new Intent(context, ChurchActivity.class);
	intent.putExtra(Church.ID, code);
	intent.putExtra(Schedule.HORAIRE, true);
	context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.church_display);
	final String code = getIntent().getStringExtra(Church.ID);
	MessesInfo.getTracker(this).trackPageView("/church/" + code);
	TabHost tabs = getTabHost();
	tabs.addTab(tabs.newTabSpec(INFORMATION).setIndicator(getString(R.string.church_tab_information),
	        getResources().getDrawable(R.drawable.sym_action_sms)).setContent(R.id.information_tab));
	Intent intentHoraires = new Intent(getApplicationContext(), ScheduleActivity.class);
	intentHoraires.putExtra(Church.ID, code);
	tabs.addTab(tabs.newTabSpec(HORAIRE).setIndicator(getString(R.string.church_tab_schedule),
	        getResources().getDrawable(R.drawable.sym_schedule)).setContent(intentHoraires));
	if (getIntent().getBooleanExtra(Schedule.HORAIRE, false)) {
	    tabs.setCurrentTab(1);
	}
	else {
	    tabs.setCurrentTab(0);
	}
	if (code != null)

	    new Thread(new Runnable() {

		@Override
		public void run() {
		    try {
			final Map<String, String> item = new Server(getString(R.string.server_url)).getLocationInfo(code);
			ChurchActivity.this.selectedItem = item;
			Cursor cursor = getContentResolver().query(Uri.withAppendedPath(Church.CONTENT_URI, code), null, null, null, null);
			final Boolean starred = cursor.getCount() > 0;
			cursor.close();
			if (item != null) {
			    runOnUiThread(new Runnable() {
				@Override
				public void run() {
				    TextView nom = (TextView) findViewById(R.id.nom);
				    TextView commune = (TextView) findViewById(R.id.commune);
				    TextView paroisse = (TextView) findViewById(R.id.paroisse);
				    CheckBox star = (CheckBox) findViewById(R.id.star);
				    ListView list = (ListView) findViewById(R.id.list_contact);
				    list.setEmptyView(findViewById(R.id.loading));
				    list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					    ListView listView = (ListView) parent;
					    ViewAdapter adapter = (ViewAdapter) listView.getAdapter();
					    ViewEntry entry = (ViewEntry) adapter.getItem(position);
					    if (entry != null) {
						Intent intent = entry.intent;
						if (intent != null) {
						    try {
							startActivity(intent);
						    } catch (ActivityNotFoundException e) {
							Log.e("messeinfo", "No activity found for intent: " + intent);
						    }
						}
					    }
					}
				    });
				    ArrayList<ViewEntry> entries = new ArrayList<ViewEntry>();
				    TabHost tabs = getTabHost();
				    star.setChecked(starred);
				    final String city = item.get(Church.CITY);
				    final String name = item.get(Church.NAME);
				    final String paroisseName = item.get(Church.COMMUNITY);
				    final String cp = item.get(Church.ZIPCODE);
				    star.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					    if (isChecked) {
						ContentValues values = new ContentValues();
						values.put(Church.ID, item.get(Church.ID));
						values.put(Church.NAME, name);
						values.put(Church.CITY, city);
						values.put(Church.ZIPCODE, cp);
						values.put(Church.COMMUNITY, paroisseName);
						values.put(Church.LAT, item.get(Church.LAT));
						values.put(Church.LNG, item.get(Church.LNG));
						values.put(Church.FAVORITE, 1);
						getContentResolver().insert(Church.CONTENT_URI, values);
					    } else {
						getContentResolver().delete(Uri.withAppendedPath(Church.CONTENT_URI, code), null, null);
					    }
					}
				    });
				    nom.setText(name);
				    commune.setText(cp + " " + city);
				    paroisse.setText(paroisseName);

				    ViewEntry entry = new ViewEntry();
				    String adresse = item.get(Church.ADDRESS);
				    if (TextUtils.isEmpty(adresse)) {
					adresse = cp + " " + city;
				    } else {
					adresse += " " + cp + " " + city;
				    }
				    entry.label = adresse;
				    entry.data = getString(R.string.church_see_map);
				    entry.actionIcon = R.drawable.sym_action_map;
				    entry.intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("geo", item.get(Church.LAT) + ","
					    + item.get(Church.LNG) + "?z=12", null));
				    entries.add(entry);
				    String email = item.get(Church.EMAIL);
				    if (!TextUtils.isEmpty(email)) {
					entry = new ViewEntry();
					entry.label = email;
					entry.data = getString(R.string.church_send_mail);
					entry.actionIcon = android.R.drawable.sym_action_email;
					entry.intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
					entries.add(entry);
				    }
				    String internet = item.get(Church.URL);
				    if (!TextUtils.isEmpty(internet)) {
					entry = new ViewEntry();
					entry.label = internet;
					entry.data = getString(R.string.church_view_site);
					entry.actionIcon = R.drawable.sym_action_organization;
					String url = internet;
					if (!internet.startsWith("http://"))
					    url = "http://" + internet;
					entry.intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					entries.add(entry);
				    }
				    String tel = item.get(Church.PHONE);
				    if (!TextUtils.isEmpty(tel)) {
					entry = new ViewEntry();
					entry.label = tel;
					entry.data = getString(R.string.church_call_tel);
					entry.actionIcon = android.R.drawable.sym_action_call;
					entry.intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel.replaceAll("[^0-9()+]+", ""), null));
					entries.add(entry);
				    }
				    String fax = item.get(Church.FAX);
				    if (!TextUtils.isEmpty(fax)) {
					entry = new ViewEntry();
					entry.label = fax;
					entry.data = getString(R.string.church_send_fax);
					entry.actionIcon = android.R.drawable.sym_action_call;
					entries.add(entry);
				    }
				    String libre = item.get(Church.MISCELLANEOUS);
				    if (!TextUtils.isEmpty(libre)) {
					entry = new ViewEntry();
					entry.label = libre;
					entry.data = "";
					entry.actionIcon = R.drawable.sym_note;
					entries.add(entry);
				    }
				    String groupe_name = item.get(Church.GROUPE_NAME);
				    if (!TextUtils.isEmpty(groupe_name)) {
					entry = new ViewEntry();
					entry.label = getString(R.string.church_groupe_name, groupe_name);
					entry.data = "";
					entry.actionIcon = R.drawable.sym_action_organization;
					entries.add(entry);
				    }
				    String groupe_email = item.get(Church.GROUPE_EMAIL);
				    if (!TextUtils.isEmpty(groupe_email)) {
					entry = new ViewEntry();
					entry.label = groupe_email;
					entry.data = getString(R.string.church_groupe_email_send_mail, groupe_name);
					entry.actionIcon = android.R.drawable.sym_action_email;
					entry.intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", groupe_email, null));
					entries.add(entry);
				    }
				    String groupe_internet = item.get(Church.GROUPE_URL);
				    if (!TextUtils.isEmpty(groupe_internet)) {
					entry = new ViewEntry();
					entry.label = groupe_internet;
					entry.data = getString(R.string.church_groupe_internet_view_site, groupe_name);
					entry.actionIcon = R.drawable.sym_action_organization;
					String url = groupe_internet;
					if (!groupe_internet.startsWith("http://"))
					    url = "http://" + groupe_internet;
					entry.intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					entries.add(entry);
				    }
				    list.setAdapter(new ViewAdapter(getApplicationContext(), entries));
				}
			    });
			}
		    } catch (XMLRPCException e1) {
			e1.printStackTrace();
			displayErrorMessage();
		    }
		}
	    }).start();
    }

    protected void displayErrorMessage() {
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		((TextView) findViewById(R.id.loading)).setText(R.string.error_church_not_exist);
	    }
	});
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, MENU_MAPS, 0, getString(R.string.menu_maps)).setIcon(android.R.drawable.ic_menu_mapmode);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case MENU_MAPS:
	    if (selectedItem != null) {
		NearMapActivity.activityStart(this, this.selectedItem.get(Church.LAT), this.selectedItem.get(Church.LNG));
	    }
	    return true;
	default:
	    break;
	}
	return false;
    }


    final static class ViewEntry {
	public String label = "";
	public String data = "";
	public int primaryIcon = -1;
	public Intent intent;
	public int actionIcon = -1;
    }

    private static final class ViewAdapter extends BaseAdapter {
	/** Cache of the children views of a row */
	static class ViewCache {
	    public TextView label;
	    public TextView data;
	    public ImageView actionIcon;

	    @SuppressWarnings("unused")
            public ViewEntry entry;
	}

	private ArrayList<ViewEntry> mEntries = null;
	protected LayoutInflater mInflater;
	protected Context mContext;

	ViewAdapter(Context context, ArrayList<ViewEntry> entries) {
	    super();
	    mEntries = entries;
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewEntry entry = mEntries.get(position);
	    View v;

	    ViewCache views;

	    // Check to see if we can reuse convertView
	    if (convertView != null) {
		v = convertView;
		views = (ViewCache) v.getTag();
	    } else {
		// Create a new view if needed
		v = mInflater.inflate(R.layout.list_item_text_icons, parent, false);

		// Cache the children
		views = new ViewCache();
		views.label = (TextView) v.findViewById(android.R.id.text1);
		views.data = (TextView) v.findViewById(android.R.id.text2);
		views.actionIcon = (ImageView) v.findViewById(R.id.icon1);
		v.setTag(views);
	    }

	    // Update the entry in the view cache
	    views.entry = entry;

	    // Bind the data to the view
	    bindView(v, entry);
	    return v;
	}

	protected void bindView(View view, ViewEntry entry) {
	    final Resources resources = mContext.getResources();
	    ViewCache views = (ViewCache) view.getTag();

	    // Set the label
	    TextView label = views.label;
	    label.setText(entry.label);

	    // Set the data
	    TextView data = views.data;
	    if (data != null) {
		data.setText(entry.data);
	    }

	    // Set the action icon
	    ImageView action = views.actionIcon;
	    if (entry.actionIcon != -1) {
		action.setImageDrawable(resources.getDrawable(entry.actionIcon));
		action.setVisibility(View.VISIBLE);
	    } else {
		// Things should still line up as if there was an icon, so make
		// it invisible
		action.setVisibility(View.INVISIBLE);
	    }
	}

	@Override
	public int getCount() {
	    return mEntries.size();
	}

	@Override
	public ViewEntry getItem(int location) {
	    return mEntries.get(location);
	}

	@Override
	public long getItemId(int location) {
	    return location;
	}
    }
}
