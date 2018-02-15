package ru.roman.visiitcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity implements SettingFragment.OnSendUserDataListener {

    private final int RESULT = 1;
    public final static String  ANSWER = "ru.roman.visiitcard.RESULT";

    private final String SETTING_ACTIVITY_LOG = "setting_activity_log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new SettingFragment())
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Intent answerIntent = new Intent();
        //answerIntent.putExtra(ANSWER,"Сработало!");
    }

    // TODO: 18.12.2017 принимаем данные при изменении настроек SettingFragment
    @Override
    public void onSendUser(boolean changeSet) {

        if (changeSet){
            changeSettings();
            Log.d(SETTING_ACTIVITY_LOG,"changeSet = "+String.valueOf(changeSet));
        }else {
            Log.d(SETTING_ACTIVITY_LOG,"changeSet=false"+changeSet);
        }

    }

    public void changeSettings(){
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
    }

    @Override
    public void finish() {
        super.finish();
    }
}
