package com.ihub.android.app;

import com.ihub.android.app.service.UpdateMembersInfoService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

public class Settings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private EditTextPreference ihubWebserviceUrl;
	private ListPreference autoUpdateTimePref;
	private ListPreference barcodeTypePref;
	public static final String AUTO_FETCH_PREFERENCE = "auto_fetch_preference";
	private CheckBoxPreference autoFetchCheckBoxPref;

	private String TAG = "iHubSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		ihubWebserviceUrl = new EditTextPreference(this);
		autoUpdateTimePref = new ListPreference(this);
		barcodeTypePref = new ListPreference(this);
		autoFetchCheckBoxPref = new CheckBoxPreference(this);

		new ListPreference(this);

		setPreferenceScreen(createPreferenceHierarchy());
		this.saveSettings();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// cache
		if (key.equals("ihub_web_service_url")) {
			if (!sharedPreferences.getString("ihub_web_service_url", "http://")
					.equals(UpdateMembersInfoService.URL_STRING)) {
				UpdateMembersInfoService.URL_STRING = sharedPreferences
						.getString("ihub_web_service_url", "");
				Log.w(TAG,
						"The iHub WebService has just changed ... The value is - "
								+ UpdateMembersInfoService.URL_STRING);
			} else {

			}
		}

		if (key.equals("auto_sync_preference")) {
			Log.w(TAG, "Auto sync value is - "
					+ autoFetchCheckBoxPref.isChecked());

			if (autoFetchCheckBoxPref.isChecked()) {
				if (UpdateMembersInfoService.isRunning) {
					// service is already running.
					Log.w(TAG, UpdateMembersInfoService.class.getName()
							+ " |  is already running...");
				} else {
					Intent serviceIntent = new Intent();
					serviceIntent
							.setAction("com.ihub.android.app.service.UpdateMembersInfoService");
					// startService(new Intent(Settings.this,
					// UpdateMembersInfoService.class));
					startService(serviceIntent);
				}
			} else {
				Log
						.w(
								TAG,
								"User selected NOT to start the sync service. Application will not have up todate info.");
				Intent serviceIntent = new Intent();
				serviceIntent
						.setAction("com.ihub.android.app.service.UpdateMembersInfoService");
				// stopService(new Intent(Settings.this, UpdateMembersInfoService.class));
				stopService(serviceIntent);

			}
		}
	}

	private PreferenceScreen createPreferenceHierarchy() {
		// ROOT element
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
				this);

		// Basic preferences
		PreferenceCategory basicPrefCat = new PreferenceCategory(this);
		basicPrefCat.setTitle(R.string.basic_settings);
		root.addPreference(basicPrefCat);

		// URL entry field
		ihubWebserviceUrl.setDialogTitle(R.string.txt_domain);
		ihubWebserviceUrl.setKey("ihub_web_service_url");
		ihubWebserviceUrl.setTitle(R.string.txt_domain);
		ihubWebserviceUrl.setDefaultValue("http://");
		ihubWebserviceUrl.setSummary(R.string.hint_domain);
		basicPrefCat.addPreference(ihubWebserviceUrl);

		CharSequence[] barcodeTypes = { "QR CODE", "1D barcodes" };
		CharSequence[] barcodeTypeValues = { "qrcode", "1dcode" };

		barcodeTypePref.setEntries(barcodeTypes);
		barcodeTypePref.setEntryValues(barcodeTypeValues);
		barcodeTypePref.setDefaultValue(barcodeTypeValues[0]);
		barcodeTypePref.setDialogTitle(R.string.barcode_types);
		barcodeTypePref.setKey("barcode_types_preference");
		barcodeTypePref.setTitle(R.string.barcode_types);
		barcodeTypePref.setSummary(R.string.hint_barcode_types);
		basicPrefCat.addPreference(barcodeTypePref);

		// Advanced Preferences
		PreferenceCategory advancedPrefCat = new PreferenceCategory(this);
		advancedPrefCat.setTitle(R.string.advanced_settings);
		root.addPreference(advancedPrefCat);

		PreferenceScreen advancedScreenPref = getPreferenceManager()
				.createPreferenceScreen(this);
		advancedScreenPref.setKey("advanced_screen_preference");
		advancedScreenPref.setTitle(R.string.advanced_settings);
		advancedScreenPref.setSummary(R.string.hint_advanced_settings);
		advancedPrefCat.addPreference(advancedScreenPref);

		// Total reports to fetch at a time
		// set list values
		CharSequence[] autoUpdateEntries = { "5 Minutes", "10 Minutes",
				"15 Minutes", "30 Minutes", "60 Minutes" };
		CharSequence[] autoUpdateValues = { "0", "50000", "10000", "15000",
				"30000", "60000" };

		autoUpdateTimePref.setEntries(autoUpdateEntries);
		autoUpdateTimePref.setEntryValues(autoUpdateValues);
		autoUpdateTimePref.setDefaultValue(autoUpdateValues[0]);
		autoUpdateTimePref.setDialogTitle(R.string.txt_auto_update_delay);
		autoUpdateTimePref.setKey("auto_update_time_preference");
		autoUpdateTimePref.setTitle(R.string.txt_auto_update_delay);
		autoUpdateTimePref.setSummary(R.string.hint_auto_update_delay);
		advancedScreenPref.addPreference(autoUpdateTimePref);

		// Auto fetch reports
		autoFetchCheckBoxPref.setKey("auto_sync_preference");
		autoFetchCheckBoxPref.setTitle(R.string.chk_auto_fetch);
		autoFetchCheckBoxPref.setSummary(R.string.hint_auto_fetch);
		advancedScreenPref.addPreference(autoFetchCheckBoxPref);

		return root;
	}

	protected void saveSettings() {

		String autoUpdate = autoUpdateTimePref.getValue();
		String ihubUrl = ihubWebserviceUrl.getText();

		// "5 Minutes", "10 Minutes", "15 Minutes", "c", "60 Minutes"
		if (autoUpdate.matches("5")) {
			UpdateMembersInfoService.SLEEP_TIME = 50000;
		} else if (autoUpdate.matches("10")) {
			UpdateMembersInfoService.SLEEP_TIME = 10000;
		} else if (autoUpdate.matches("15")) {
			UpdateMembersInfoService.SLEEP_TIME = 15000;
		} else if (autoUpdate.matches("30")) {
			UpdateMembersInfoService.SLEEP_TIME = 30000;
		} else if (autoUpdate.matches("60")) {
			UpdateMembersInfoService.SLEEP_TIME = 60000;
		}

		// ihub web service.
		UpdateMembersInfoService.URL_STRING = ihubUrl;

		// store location for the images.
		UpdateMembersInfoService.savePath = "/sdcard/ihub/pics/";
		UpdateMembersInfoService.autoSync = autoFetchCheckBoxPref.isChecked();
		UpdateMembersInfoService.saveSettings(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);

	}

}
