package ru.roman.visiitcard;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ru.roman.visiitcard.dialogs.DialogInfo;
import ru.roman.visiitcard.dialogs.DialogUserData;
import ru.roman.visiitcard.fragments.MyCardFragment;
import ru.roman.visiitcard.fragments.SavedCardsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,DialogUserData.DialogUserDataListener {

    // TODO: 13.01.2017 Объявляем переменные для классов
    String actionStateChanged;

    // TODO: 13.01.2017 Переменные для фрагментов
    MyCardFragment myCardFragment;
    SavedCardsFragment savedCardsFragment;
    SettingFragment settingFragment;

    // TODO: 13.01.2017 Переменные для интерфейса (кнопки, текстовые поля и т.д.)
    FloatingActionButton fab;

    // TODO: 16.01.2017 переменная для вывода в log-панель
    public final String MY_LOG = "myLog";

    //TODO объявление переменных для работы с сохранением настроек приложения
    private static final String APP_PREFERENCES = "Settings";
    private SharedPreferences mSettings;
    private final String SAVE_NAME_USER = "nameUser";
    private final String SAVE_NUM_USER = "numUser";
    private final String SAVE_EMAIL_USER = "emailUser";
    private final String SAVE_DATA = "saveData";

    private String saveName, saveNumber, saveEmail;
    FragmentManager fragmentManager;
    boolean saveData;

    private final int GET_DATA_USER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.d(MY_LOG,"Работает!");

        actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /*
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                superClass.setTestNum("change to 55");
                Log.d(MY_LOG,superClass.getTestNum());
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


       fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().add(R.id.content_main, myCardFragment).commit();



        // TODO: 11.10.2017 Инициализация переменных для сохранения настроек
        //mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        saveData = mSettings.getBoolean(SAVE_DATA,false);
        if (!saveData){
            drawer.openDrawer(GravityCompat.START);

            DialogUserData dialogUserData = new DialogUserData();
            dialogUserData.show(fragmentManager,"userData");
            dialogUserData.setCancelable(false);
        }else {
            Log.d(MY_LOG,"Настройки сохранены!");
        }
        savedCardsFragment = SavedCardsFragment.newInstance(saveData);
        //settingFragment = new SettingFragment();

        fragmentManager.beginTransaction().add(R.id.content_main, savedCardsFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fragmentManager.beginTransaction().replace(R.id.content_main, savedCardsFragment).commit();
        } else if (id == R.id.nav_gallery) {
            //fragmentManager.beginTransaction().replace(R.id.content_main, myCardFragment).commit();
            DialogInfo dialogInfo = new DialogInfo();
            dialogInfo.show(fragmentManager,"dialogInfo");
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivityForResult(intent,GET_DATA_USER);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

/*        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*/


        //fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_DATA_USER){
            if (resultCode == RESULT_OK){
                Log.d(MY_LOG,"Будет выполнена генерация QR-кода");
                saveName = mSettings.getString(SAVE_NAME_USER,"");
                saveNumber = mSettings.getString(SAVE_NUM_USER,"");
                saveEmail = mSettings.getString(SAVE_EMAIL_USER,"");
                savedCardsFragment.createUserQR(saveName,saveNumber,saveEmail);
            }

        }
    }

    // TODO: 11.10.2017 При первом запуске приложения, при нажатии "ОК" в диалоговом окне с данными пользователя
    @Override
    public void onPositiveButton(String getName,String getNumber,String getEmail) {
        Toast.makeText(this, getName, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, getNumber, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, getEmail, Toast.LENGTH_SHORT).show();

        saveName = getName;
        saveNumber = getNumber;
        saveEmail = getEmail;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(SAVE_NAME_USER, saveName);
        editor.putString(SAVE_NUM_USER, saveNumber);
        editor.putString(SAVE_EMAIL_USER, saveEmail);
        editor.putBoolean(SAVE_DATA, true);
        editor.apply();

        savedCardsFragment.createUserQR(saveName,saveNumber,saveEmail);
    }

}
