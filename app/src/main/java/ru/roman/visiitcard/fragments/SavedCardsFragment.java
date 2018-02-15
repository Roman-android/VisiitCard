package ru.roman.visiitcard.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.lypeer.fcpermission.FcPermissions;
import com.lypeer.fcpermission.impl.FcPermissionsCallbacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Scanner;

import ru.roman.visiitcard.ContactsWork;
import ru.roman.visiitcard.QRcoder;
import ru.roman.visiitcard.R;
import ru.roman.visiitcard.ScanActivity;
import ru.roman.visiitcard.SmsWork;
import ru.roman.visiitcard.dialogs.DialogInfo;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedCardsFragment extends Fragment implements ContactsWork.DataContactCallback,
        ContactsWork.NumberContactCallback,SmsWork.TextSmsCallback, FcPermissionsCallbacks{

    private final static int CONTACT_ALL = 100;
    private final static int CONTACT_NUMBER = 200;
    public static final int REQUEST_QR_CODE = 300;
    public static final int PERMISSION_REQUEST = 400;

    final String SAVED_TAG = "savedTagFragment";
    final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    final String SEND_SMS_FLAG = "SEND_SMS";
    SmsManager smsManager;

    Button buttonSMS,buttonUserData,buttonSrc,buttonChoiceNumber, buttonCreateQr,buttonReadQR;
    ImageView but_info;

    CheckBox auto_focus,use_flash;

    TextView textSms;
    EditText numToSms;

    Intent sendSMS;
    Intent contactIntent;
    IntentFilter filter;
    BroadcastReceiver receiver;
    BroadcastReceiver sendReceiver;
    Cursor cursor;

    String numberFromToSms,nameFrom,numberFrom,emailFrom,saitFrom;
    String contactInfo,userInfo;
    String queryString;

    SmsWork smsWork;
    ContactsWork contactsWork;
    Intent dataFromContactsWork;

    ImageView user_qr_image,contact_qr_image;
    Bitmap bitmap;

    //переменная для отображения выбран ли контакт для генерации QR-кода
    boolean isNumChoice;

    final String SAVE_DATA = "saveData";

    //флаг показывает была ли хоть один раз нажата кнопка OK при занесении данных о пользователе, т.е. был ли вызван метод DialogUserData,
    // т.е. если приложение запущено уже не первый раз и есть сохраненный QR-код в jpeg на телефоне
    private boolean get_status_save_data;

    File qr_path_user;
    String qr_user_name;

    // флаг, который определяет был ли создан новый qr-код (если был создан принимает значение true, и в методе onStop сохраняет его в виде jpg)
    boolean get_user_data;

    RadioButton userQR, contactQR;


    public static SavedCardsFragment newInstance (boolean saveData) {
        SavedCardsFragment savedCardsFragment = new SavedCardsFragment();
        Bundle args = new Bundle();
        args.putBoolean("saveData",saveData);
        savedCardsFragment.setArguments(args);
        return  savedCardsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        get_status_save_data = getArguments().getBoolean(SAVE_DATA);
        Log.d(SAVED_TAG,SAVE_DATA+" = "+get_status_save_data);
        Log.d(SAVED_TAG,"onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_saved_cards, container, false);

        user_qr_image = (ImageView) view.findViewById(R.id.user_qr);
        contact_qr_image = (ImageView) view.findViewById(R.id.contact_qr);
        contact_qr_image.setVisibility(View.GONE);

        userQR = (RadioButton)view.findViewById(R.id.userQR);
        contactQR = (RadioButton)view.findViewById(R.id.contactQR);
        but_info = view.findViewById(R.id.but_info);

        userQR.setOnClickListener(radioButtonListener);
        contactQR.setOnClickListener(radioButtonListener);
        but_info.setOnClickListener(infoListener);



        cursor = null;

        queryString = "visitApp";
        isNumChoice = false;
        get_user_data = false;

        numberFromToSms =nameFrom=numberFrom=emailFrom=saitFrom="";

        buttonSMS = (Button) view.findViewById(R.id.btnSms);
        buttonSMS.setEnabled(false);

        // TODO: 10.10.2017 временно скрываем кнопку "Отправить СМС" и убираем из манифеста
        buttonSMS.setVisibility(View.GONE);


        buttonChoiceNumber = (Button) view.findViewById(R.id.btnChoiceNumber);

        buttonCreateQr = (Button)view.findViewById(R.id.btnQR);
        //buttonCreateQr.setEnabled(false);

        buttonReadQR = (Button)view.findViewById(R.id.btnReadQR);

        // TODO: временно скрываем поле для ввода номера, на который отправить смс и кнопку обзор
        numToSms = (EditText) view.findViewById(R.id.numToSms);
        numToSms.setVisibility(View.GONE);
        buttonSrc = (Button) view.findViewById(R.id.butSrc);
        buttonSrc.setVisibility(View.GONE);

        auto_focus = (CheckBox)view.findViewById(R.id.auto_focus);
        use_flash = (CheckBox)view.findViewById(R.id.use_flash);

        // TODO: 02.04.2017 Инициализируем классы
        contactsWork = new ContactsWork(getActivity());
        smsWork = new SmsWork(getActivity());

        // TODO: 23.10.2017 Переменные для сохранения qr пользователя
        qr_path_user = getActivity().getFilesDir();
        qr_user_name = "user_qr.png";
        String qr_path_full = qr_path_user+"/"+qr_user_name;

        /*
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        Bitmap b1 = Bitmap.createScaledBitmap(bitmap,width,width,false);
        */

        if(get_status_save_data){
            user_qr_image.setImageDrawable(Drawable.createFromPath(qr_path_full));
        }

        contactsWork.registredDataContactCallback(this,this);
        smsWork.registredTextSmsCallback(this);

        textSms = (TextView) view.findViewById(R.id.textSMS);

        // TODO: 12.09.2017 выбрать контакт, который будет отправлен по смс или через QR код
        buttonChoiceNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactIntent, CONTACT_ALL);
            }
        });

        // TODO: 12.09.2017 кнопка для выбора контакта, которому отправить смс
        buttonSrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactIntent, CONTACT_NUMBER);
            }
        });

        // TODO: 12.09.2017 кнопка отправки смс с выбранным номером
        buttonSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS();
            }
        });

        // TODO: 12.09.2017 кнопка генерации QR кода с информацией о контакте
        buttonCreateQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNumChoice){
                    new QRcoder(getActivity(),contact_qr_image).execute(contactInfo);
                }else {
                    Toast.makeText(getActivity(), "Выберите контакт из справочника для генерации QR-кода", Toast.LENGTH_LONG).show();
                }
            }
        });

        // TODO: 12.09.2017 кнопка для считывания QR кода и добавления информации о контакте в телефонную книжку
        buttonReadQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(intent, REQUEST_QR_CODE);
            }
        });

        sendSMS = new Intent(SEND_SMS_FLAG);
        filter = new IntentFilter(SMS_RECEIVED);

        receiver = new SmsReciever();
        sendReceiver = new sendReceiver();

        return view;
    }

    // TODO: 23.10.2017 Генерируем QR-код пользователя и выводим на экран (срабатывает только при нажатии кнопки "ОК" и при нажатии кнопки назад из настроек)
    public void createUserQR(String getName, String getNumber, String getEmail){
        String sait = "sait";
        userInfo = queryString+","+getName+","+getNumber+","+getEmail+","+sait;
        new QRcoder(getActivity(),user_qr_image).execute(userInfo);
        get_user_data = true;
    }

    // TODO: 17.10.2017 Сохраняем свой QR-код во внутренней памяти телефона
    private void saveQR (){
        Log.d(SAVED_TAG, qr_path_user.toString());
        FileOutputStream fileOutputStream;

        user_qr_image.setDrawingCacheEnabled(true);
        bitmap = user_qr_image.getDrawingCache();

        try {
            fileOutputStream = getActivity().openFileOutput(qr_user_name,Context.MODE_PRIVATE);
            boolean work = bitmap.compress(Bitmap.CompressFormat.PNG, 100,fileOutputStream);
            if (work){
                Log.d(SAVED_TAG,"Файл создан");
            }else {
                Log.d(SAVED_TAG,"Не удалось создать файл");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(SAVED_TAG,"Сработал метод saveQR");
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(receiver, filter);
        getActivity().registerReceiver(sendReceiver, new IntentFilter(SEND_SMS_FLAG));
        Log.d(SAVED_TAG,"Сработал метод onResume");
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(sendReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(SAVED_TAG,"Сработал метод onStop");

        if(get_user_data){
            saveQR();
        }

    }

    View.OnClickListener radioButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RadioButton rb = (RadioButton)view;
            switch (rb.getId()){
                case R.id.userQR:
                    onChoiceUserQr();
                    break;
                case R.id.contactQR:
                    onChoiceContactQR();
                    break;

            }
        }
    };

    private void onChoiceUserQr() {
        if (contact_qr_image.getVisibility()== View.VISIBLE){
            contact_qr_image.setVisibility(View.GONE);
        }
        if (user_qr_image.getVisibility()==View.GONE){
            user_qr_image.setVisibility(View.VISIBLE);
        }
    }

    private void onChoiceContactQR() {
        if (contact_qr_image.getVisibility()== View.GONE){
            contact_qr_image.setVisibility(View.VISIBLE);
        }
        if (user_qr_image.getVisibility()==View.VISIBLE){
            user_qr_image.setVisibility(View.GONE);
        }
    }

    View.OnClickListener infoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DialogInfo dialogInfo = new DialogInfo();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            dialogInfo.show(fragmentManager,"dialogInfo");

        }
    };

    // TODO: 06.04.2017 Получаем данные о контакте из выбранного контакта для отправки
    @Override
    public void callBackContactData(String name,String number,String email,String sait) {
        Log.d(SAVED_TAG,"Получено из интерфейса 1: "+name+" "+number+" "+email+" "+sait);

        nameFrom = name;
        numberFrom = number;
        emailFrom = email;
        saitFrom = sait;
        contactInfo = queryString+","+name+","+number+","+email+","+sait;
        textSms.setText("Выбранный контакт: "+name+","+number+","+email+","+sait);
        Log.d(SAVED_TAG,name+","+contactInfo);
        isNumChoice = true;
    }

    // TODO: 06.04.2017 Получаем номер контакта для отправки ему смс
    @Override
    public void callBackContactNumber(String number) {
        Log.d(SAVED_TAG,"Получено из интерфейса 2: "+number);
        numberFromToSms = number;
        if (textSms.getText().length() > 0){
            buttonSMS.setEnabled(true);
        }
    }

    // TODO: 06.04.2017 Отправляем СМС с данными выбранного контакта на номер выбранного или введенного вручную контакта
    public void sendSMS() {
        smsManager = SmsManager.getDefault();

        final PendingIntent sentPIn = PendingIntent.getBroadcast(getActivity(), 0, sendSMS, 0);
        String smsNumber = numberFromToSms;
        String smsText = queryString+","+nameFrom+","+numberFrom+","+emailFrom+","+saitFrom;

        smsManager.sendTextMessage(smsNumber, null, smsText, sentPIn, null);

        textSms.setText("");
        numToSms.setText("");

        Toast.makeText(getActivity(), "Нажата кнопка отправить смс", Toast.LENGTH_SHORT).show();
    }


//TODO: ========================ДЕЙСТВИЯ ПРИ ПРИЕМЕ ИНФОРМАЦИИ (СМС ИЛИ QR КОД)===========================
    // TODO: 06.04.2017 Получаем данные о контакте из принятого СМС
    @Override
    public void callbackTextSms(String name, String number, String email, String sait) {
        nameFrom = name;
        numberFrom = number;
        emailFrom = email;
        saitFrom = sait;

        //textSms.setText("Получено сообщение из принятого СМС: " + '\n' + name+","+number+","+email+","+sait);
        textSms.setText("Получены данные из принятого СМС: "+name+", "+number+", "+email+", "+sait);
        contactsWork.addContact(getActivity(),nameFrom,numberFrom,emailFrom);
    }


    // TODO: 06.04.2017 При приеме смс запускаем функцию его чтения и сохранения данных
    class SmsReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            smsWork.readSMS(intent,queryString);
            //Toast.makeText(getActivity(), "Принял сообщение", Toast.LENGTH_SHORT).show();
            //Log.d(SAVED_TAG, "Принял сообщение");
        }
    }

    class sendReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "Сообщение отправлено!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case CONTACT_ALL:
                    // TODO: 15.02.2018 запрос разрешения для чтения контакта (для Android 6.0 и выше)
                    //contactsWork.getContactData(data);
                    dataFromContactsWork = data;
                    contactQR.setChecked(true);
                    requestPermission();
                    break;
                case CONTACT_NUMBER:
                    contactsWork.getNumberContact(data);
                    numToSms.setText(numberFromToSms);
                    break;
                case REQUEST_QR_CODE:
                    if(data != null){
                        final Barcode barcode = data.getParcelableExtra("barcode");
                        textSms.post(new Runnable() {
                            @Override
                            public void run() {
                                textSms.setText(barcode.displayValue);
                                Log.d(SAVED_TAG,barcode.displayValue);

                                //чтение данных из QR-кода
                                if (barcode.displayValue.startsWith(queryString)) {

                                    // TODO: 05.04.2017 Работа со сканером
                                    Scanner scanner = new Scanner(barcode.displayValue).useDelimiter(",");
                                    int i = 0;
                                    String [] data = new String[5];

                                    while (scanner.hasNext()){
                                        data[i]=scanner.next();
                                        i++;
                                    }

                                    //Toast.makeText(getActivity(), "Принял СМС "+data[0], Toast.LENGTH_SHORT).show();
                                    Log.d(SAVED_TAG,"Полученные данные из QR кода:");
                                    Log.d(SAVED_TAG,data[1]);
                                    Log.d(SAVED_TAG,data[2]);
                                    Log.d(SAVED_TAG,data[3]);
                                    Log.d(SAVED_TAG,data[4]);
                                    contactsWork.addContact(getActivity(),data[1],data[2],data[3]);
                                }else{
                                    textSms.setText("Этот QR код не содержит контакта или получен не из приложения!");
                                }
                            }
                        });
                    }
                    break;
            }
        } else {
            Log.w(SAVED_TAG, "Внимание: Activity не вернула значений для обработки");
        }

    }

    // TODO: 25.09.2017 Работаем с разрешениями Android 6.0 и выше
    private void requestPermission() {
        FcPermissions.requestPermissions(this,"Требуется разрешение отправить СМС", FcPermissions.REQ_PER_CODE, Manifest.permission.READ_CONTACTS);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        contactsWork.getContactData(dataFromContactsWork);
        onChoiceContactQR();
        Toast.makeText(getActivity(), "Разрешение получено!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int i, List<String> list) {
        Toast.makeText(getActivity(), "Разрешение НЕ получено!", Toast.LENGTH_SHORT).show();
        FcPermissions.checkDeniedPermissionsNeverAskAgain(getActivity(),"Разрешение для отправки СМС нужно для...",R.string.settings,R.string.cancel,list);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FcPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }


}
