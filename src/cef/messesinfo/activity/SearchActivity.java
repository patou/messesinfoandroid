package cef.messesinfo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.maps.MyLocation;

public class SearchActivity extends Activity {
    private static final int NEED_HELP_DIALOG = 1;
    Boolean searchSchedule = true;
    private EditText searchText;
    MyLocation myLocation = new MyLocation();
    private TextView titleText;

    static public void activityStart(Context context, Boolean searchSchedule) {
	Intent intent = new Intent(context, SearchActivity.class);
	intent.putExtra("searchSchedule", searchSchedule);
	context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search);
	searchSchedule = getIntent().getBooleanExtra("searchSchedule", true);
	searchText = (EditText) findViewById(R.id.searchField);
	titleText = (TextView) findViewById(R.id.title_text);
	titleText.setText(searchSchedule ? R.string.menu_search_mass : R.string.menu_church_book);
	searchText.setImeOptions(DEFAULT_KEYS_SEARCH_LOCAL);
	searchText.setOnEditorActionListener(new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		search(searchText.getText().toString());
		return true;
	    }
	});
	searchText.requestFocus();
	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	mgr.showSoftInput(searchText, InputMethodManager.SHOW_FORCED);
    }

    public void goHome(View v) {
	final Intent intent = new Intent(this, MessesInfo.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }

    public void onNeedHelpClick(View v) {
	showDialog(NEED_HELP_DIALOG);
    }

    public void onSearchClick(View v) {
	search(searchText.getText().toString());
    }

    public void onNearSearchClick(View v) {
	findViewById(R.id.localisation_in_progress).setVisibility(View.VISIBLE);
	myLocation.getLocation(SearchActivity.this, new MyLocation.LocationResult() {

	    @Override
	    public void gotLocation(final Location location) {
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			findViewById(R.id.localisation_in_progress).setVisibility(View.GONE);
			String search = searchText.getText().toString() + " > " + location.getLatitude() + ":" + location.getLongitude();
			search(search);
		    }
		});
	    }
	});
    }

    protected void search(String search) {
	if (search != null && !TextUtils.isEmpty(search))
	    if (searchSchedule)
		SearchScheduleActivity.activityStart(this, search);
	    else
		SearchChurchActivity.activityStart(this, search);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
	if (id == NEED_HELP_DIALOG) {
	    return new AlertDialog.Builder(this).setTitle(R.string.need_help).setNeutralButton("OK", null).setMessage(
		    searchSchedule ? R.string.list_search_mass_help : R.string.list_search_church_help).setIcon(R.drawable.need_help).create();
	}
	return null;
    }
}
