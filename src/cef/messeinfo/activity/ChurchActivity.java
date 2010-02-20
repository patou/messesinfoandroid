package cef.messeinfo.activity;

import java.util.ArrayList;
import java.util.Map;

import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import cef.messeinfo.R;
import cef.messeinfo.client.Server;
import cef.messeinfo.provider.Church;

public class ChurchActivity extends TabActivity {
	public static void activityStart(Context context, String code) {
		Intent intent = new Intent(context, ChurchActivity.class);
		intent.putExtra("code", code);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.church_display);

		final String code = getIntent().getStringExtra("code");
		TabHost tabs = getTabHost();
		tabs.addTab(tabs.newTabSpec("information")
				.setIndicator(getString(R.string.church_tab_information), getResources()
				.getDrawable(R.drawable.sym_action_sms))
				.setContent(R.id.information_tab));
		Intent intentHoraires = new Intent(getApplicationContext(), ScheduleActivity.class);
		intentHoraires.putExtra("code", code);
		tabs.addTab(tabs.newTabSpec("horaire")
				.setIndicator(getString(R.string.church_tab_schedule), getResources()
				.getDrawable(R.drawable.sym_schedule))
				.setContent(intentHoraires));
		Log.e("messeinfo", code);
		if (code != null)
			new Thread(new Runnable() {

				@Override
				public void run() {
					final Map<String, String> item = new Server(getString(R.string.server_url)).getChurchInfo(code);
					Cursor cursor = getContentResolver().query(Uri.withAppendedPath(Church.CONTENT_URI, code), null, null, null, null);
					final Boolean starred = cursor.getCount() > 0;
					if (item != null) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								// ImageView icon = (ImageView)
								// findViewById(R.id.icon);

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
								final String city = item.get(Church.COMMUNE);
								final String name = item.get(Church.NOM);
								final String paroisseName = item.get(Church.PAROISSE);
								star.setOnCheckedChangeListener(new OnCheckedChangeListener() {

									@Override
									public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
										if (isChecked) {
											ContentValues values = new ContentValues();
											values.put(Church.CODE, item.get(Church.CODE));
											values.put(Church.NOM, name);
											values.put(Church.COMMUNE, city);
											values.put(Church.PAROISSE, paroisseName);
											getContentResolver().insert(Church.CONTENT_URI, values);
										} else {
											getContentResolver().delete(Uri.withAppendedPath(Church.CONTENT_URI, code), null, null);
										}
									}
								});
								/*
								 * WebView webview = (WebView)
								 * findViewById(R.id.webview);
								 * webview.loadUrl(item.get("url"));
								 * webview.setWebViewClient(new WebViewClient(){
								 * public void onPageFinished(WebView view,
								 * String url) { super.onPageFinished(view,
								 * url); view.scrollTo(10, 408); }; });
								 */
								// icon.setImageResource(R.drawable.church);
								// icon.setImageResource(R.drawable.church);
								nom.setText(name);
								commune.setText(city);
								paroisse.setText(paroisseName);

								ViewEntry entry = new ViewEntry();
								String adresse = item.get(Church.ADRESSE);
								if (TextUtils.isEmpty(adresse)) {
									adresse = city;
								}
								entry.label = adresse;
								entry.data = getString(R.string.church_see_map);
								entry.actionIcon = R.drawable.sym_action_map;
								entry.intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("geo", item.get(Church.LAT) + "," + item.get(Church.LON) + "?z=12", null));
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
								String internet = item.get(Church.INTERNET);
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
								String tel = item.get(Church.TEL);
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
								String libre = item.get(Church.LIBRE);
								if (!TextUtils.isEmpty(libre)) {
									entry = new ViewEntry();
									entry.label = libre;
									entry.data = "";
									entry.actionIcon = R.drawable.sym_note;
									entries.add(entry);
								}
								list.setAdapter(new ViewAdapter(getApplicationContext(), entries));
								tabs.setCurrentTab(0);
								/*
								 * URL img; try { img = new
								 * URL(getString(R.string.photo_url) + code);
								 * InputStream is = (InputStream)
								 * img.getContent(); final Drawable d =
								 * Drawable.createFromStream(is, "src");
								 * icon.setImageDrawable(d); } catch
								 * (MalformedURLException e) {
								 * e.printStackTrace(); } catch (IOException e)
								 * { e.printStackTrace(); }
								 */
							}
						});
					}
				}
			}).start();
	}

	private Handler handler = new Handler();

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

			// Need to keep track of this too
			ViewEntry entry;
		}

		private ArrayList<ViewEntry> mEntries = null;
		protected LayoutInflater mInflater;
		protected Context mContext;

		ViewAdapter(Context context, ArrayList<ViewEntry> entries) {
			super();
			mEntries = entries;
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
