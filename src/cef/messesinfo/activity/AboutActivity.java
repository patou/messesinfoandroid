package cef.messesinfo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;

public class AboutActivity extends Activity {
    public static final int MENU_CONTACT = 1;
    public static final int MENU_WEBSITE = 2;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, AboutActivity.class));
    }

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.about);

	findViewById(R.id.about_img1).setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		Uri uri = Uri.parse(getString(R.string.messeinfo_url));
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	    }

	});
	findViewById(R.id.about_website).setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		Uri uri = Uri.parse(getString(R.string.messeinfo_url));
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	    }

	});
    }

    public void goHome(View v) {
	final Intent intent = new Intent(this, MessesInfo.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }
    
    public void onShareClick(View v) {
	String text = getString(R.string.messesinfo_share);
	Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT,
		getString(R.string.messesinfo_share_subject));
	startActivityForResult(Intent.createChooser(i, getString(R.string.share_with)), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	boolean supRetVal = super.onCreateOptionsMenu(menu);

	menu.add(0, MENU_CONTACT, 0, getString(R.string.main_menu_contact)).setIcon(R.drawable.contact);
	menu.add(0, MENU_WEBSITE, 0, getString(R.string.main_menu_website)).setIcon(R.drawable.icon_mini);
	return supRetVal;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	switch (item.getItemId()) {
	case MENU_CONTACT:
	    // Uri uri = Uri.parse("mailto://contact@messesinfo.cef.fr");
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent.setType("plain/text");
	    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "android@cef.fr" });
	    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contact depuis l'application Android");
	    // emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "myBodyText");
	    startActivity(Intent.createChooser(emailIntent, "Contact messeinfo"));
	    // startActivity(new Intent(Intent.ACTION_VIEW, uri));
	    return true;
	case MENU_WEBSITE:
	    final Intent urlIntent = new Intent(android.content.Intent.ACTION_VIEW);
	    urlIntent.setData(Uri.parse(getString(R.string.messeinfo_url)));
	    startActivity(urlIntent);
	    return true;
	default:
	    break;
	}
	return false;
    }

}
