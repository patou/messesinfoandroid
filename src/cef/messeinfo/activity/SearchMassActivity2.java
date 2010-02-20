package cef.messeinfo.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import cef.messeinfo.R;

public class SearchMassActivity2 extends PreferenceActivity {
    private EditTextPreference city;
    private EditTextPreference zip;
    private ListPreference state;
    private ListPreference celebration;
    private CheckBoxPreference saturday;
    private CheckBoxPreference sunday;
    private CheckBoxPreference morning;
    private CheckBoxPreference afternoon;

    /**
     * Start the Activity
     * @param context
     */
    public static void activityStart(Context context) {
        context.startActivity(new Intent(context, SearchMassActivity2.class));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.search_mass);
        city = (EditTextPreference) findPreference("search_mass_city");
        zip = (EditTextPreference) findPreference("search_mass_zip");
        state = (ListPreference) findPreference("search_mass_state");
        celebration = (ListPreference) findPreference("search_mass_celebration");
        saturday = (CheckBoxPreference) findPreference("search_mass_saturday");
        sunday = (CheckBoxPreference) findPreference("search_mass_sunday");
        morning = (CheckBoxPreference) findPreference("search_mass_morning");
        afternoon = (CheckBoxPreference) findPreference("search_mass_afternoon");
        city.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
        zip.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
        state.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(state.getEntry());
                return true;
            }
        });
        celebration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(celebration.getEntry());
                return true;
            }
        });
        findPreference("search_mass_reset").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                reset();
                return false;
            }
        });
        findPreference("search_mass_submit").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                submit();
                return false;
            }
        });
        fillDefaultValues();
    }

    private void fillDefaultValues() {
        if (city.getText() != null && city.getText().length() > 0) {
            city.setSummary(city.getText());
        }
        else {
            city.setSummary(R.string.search_mass_empty);
        }
        if (zip.getText() != null && zip.getText().length() > 0) {
            zip.setSummary(zip.getText());
        }
        else {
            zip.setSummary(R.string.search_mass_empty);
        }
        if (state.getValue() != null && state.getValue().length() > 0) {
            state.setSummary(state.getEntry());
        }
        else {
            state.setSummary(R.string.search_mass_empty);
        }
        if (celebration.getValue() != null && celebration.getValue().length() > 0) {
            celebration.setSummary(celebration.getEntry());
        }
        else {
            celebration.setSummary(R.string.search_mass_empty);
        }
    }
    
    private void submit() {
        Map<String,String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("post_code", "x787z8ez04dz564d");  
        nameValuePairs.put("logo", "http://www.messesinfo.cef.fr/images/logo_cef.gif");
        nameValuePairs.put("url", "http://eglise.catholique.fr");
        nameValuePairs.put("post_diocese", "all");
        if (city.getText() != null && city.getText().length() > 0) {
            nameValuePairs.put("post_commune", city.getText());
        }
        if (zip.getText() != null && zip.getText().length() > 0) {
            nameValuePairs.put("post_cp", zip.getText());
        }
        else {
            if (state.getValue() != null && state.getValue().length() > 0) {
                nameValuePairs.put("post_cp", state.getValue());
            }
        }
        if (celebration.getValue() != null && celebration.getValue().length() > 0) {
            nameValuePairs.put("post_celebration", celebration.getValue());
        }
        if (saturday.isChecked()) {
            nameValuePairs.put("chksamedi", "1");
        }
        if (sunday.isChecked()) {
            nameValuePairs.put("chkdimanche", "1");
        }
        if (morning.isChecked()) {
            nameValuePairs.put("chkdimanche", "1");
        }
        if (afternoon.isChecked()) {
            nameValuePairs.put("chkdimanche", "1");
        }
        Log.e("messeinfo", nameValuePairs.toString());
        WebViewActivity.startActivity(this, nameValuePairs);
    }
    
    private void reset() {
        city.setText(null);
        zip.setText(null);
        state.setValue(null);
        celebration.setValue(null);
        saturday.setChecked(false);
        sunday.setChecked(false);
        morning.setChecked(false);
        afternoon.setChecked(false);
        fillDefaultValues();
    }
    
}
