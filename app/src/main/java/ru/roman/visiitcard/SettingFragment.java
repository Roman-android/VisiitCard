package ru.roman.visiitcard;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    // TODO: 16.01.2017 переменная для вывода в log-панель
    public final String SETTING_LOG = "settingLog";

    EditTextPreference telPreference;
    EditTextPreference namePreference;
    EditTextPreference emailPreference;

    CharSequence name, tel, email;

    SharedPreferences mSettings;
    SharedPreferences.Editor editor;

    private final String SAVE_NAME_USER = "nameUser";
    private final String SAVE_NUM_USER = "numUser";
    private final String SAVE_EMAIL_USER = "emailUser";

    public OnSendUserDataListener mListener;
    private boolean changeSet = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        namePreference = (EditTextPreference) findPreference("name_user");
        telPreference = (EditTextPreference) findPreference("tel_user");
        emailPreference = (EditTextPreference) findPreference("email_user");

        mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String name_user = mSettings.getString(SAVE_NAME_USER, "");
        namePreference.setSummary(name_user);
        namePreference.setText(name_user);

        String tel_user = mSettings.getString(SAVE_NUM_USER, "");
        telPreference.setSummary(tel_user);
        telPreference.setText(tel_user);

        String email_user = mSettings.getString(SAVE_EMAIL_USER, "");
        emailPreference.setSummary(email_user);
        emailPreference.setText(email_user);

        Log.d(SETTING_LOG,"name_user: "+name_user);
        Log.d(SETTING_LOG,"tel_user: "+tel_user);
        Log.d(SETTING_LOG,"email_user: "+email_user);
    }

    public interface OnSendUserDataListener {
       public void onSendUser(boolean changeSet);
    }

    private void updateSummary(String key) {

        editor = mSettings.edit();

        switch (key) {
            case "name_user":

                name = namePreference.getText();
                namePreference.setSummary(name);

                editor.putString(SAVE_NAME_USER, String.valueOf(name));

                break;

            case "tel_user":

                tel = telPreference.getText();
                telPreference.setSummary(tel);

                editor.putString(SAVE_NUM_USER, String.valueOf(tel));

                break;

            case "email_user":

               email = emailPreference.getText();
                emailPreference.setSummary(email);

                editor.putString(SAVE_EMAIL_USER, String.valueOf(email));
        }

        editor.apply();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnSendUserDataListener){
            mListener = (OnSendUserDataListener)activity;
        }else {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSendUserDataListener");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        //mListener.onSendUser("Roman456");
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String change_field = "";

        switch (key) {
            case "name_user":
                change_field = "name_user";
                break;

            case "tel_user":
                change_field = "tel_user";
                break;

            case "email_user":
                change_field = "email_user";
                break;
        }
        updateSummary(change_field);
        if (!changeSet){
            changeSet = true;
            mListener.onSendUser(true);
        }

    }
}
