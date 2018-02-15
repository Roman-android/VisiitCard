package ru.roman.visiitcard;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class ContactsWork {

    private Cursor cursorContactAll;
    private Cursor cursor;
    private Cursor cursorEmail;
    private Cursor cursorName;
    private Cursor cursorSait;

    private String emailOfContact;
    private Context getContext;
    private Uri resultUri;
    private String numberOfContact;
    private String nameOfContact;

    private String saitOfContact;
    private final String TAG_CONTACTS = "myTagContact";

    private ContentResolver contentResolver;

    private DataContactCallback myContactCallback;
    private NumberContactCallback numberContactCallback;
    private String id;

    public ContactsWork(Context context) {

        cursor = null;
        cursorContactAll = null;
        cursorEmail = null;
        cursorName = null;
        cursorSait = null;

        emailOfContact = "";
        resultUri = null;
        numberOfContact = "";
        nameOfContact = "";
        saitOfContact = "";

        contentResolver = null;

        id="";

        getContext = context;


    }

    public interface DataContactCallback {
        void callBackContactData(String name,String number,String email,String sait);
    }

    public interface NumberContactCallback{
        void callBackContactNumber(String number);
    }

    public void registredDataContactCallback(DataContactCallback dataContactCallback,NumberContactCallback numberContactCallback) {
        this.myContactCallback = dataContactCallback;
        this.numberContactCallback = numberContactCallback;
    }



    private void getCursorContactAll(Intent data) {
        contentResolver = getContext.getContentResolver();
        resultUri = data.getData();
        cursorContactAll = getContext.getContentResolver().query(resultUri, null, null, null, null);
        id = resultUri.getLastPathSegment();
    }

    // TODO: 02.04.2017 Получаем данные об имени контакта
    private void getNameContact(){

        //DISPLAY_NAME - все имя полностью (имя, фамилия и т.д.)
        //GIVEN_NAME - только имя
        ContentResolver contentResolver = getContext.getContentResolver();

        String id = resultUri.getLastPathSegment();

        String addrWhere = ContactsContract.Data.CONTACT_ID
                + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        cursorName = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                null, addrWhere, addrWhereParams, null);
        if (cursorName != null && cursorName.moveToNext()) {
            nameOfContact = cursorName.getString(cursorName.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
        }

        if (nameOfContact.equals("")) {
            nameOfContact = "";
        }

        if (cursorName != null) {
            cursorName.close();
        }


    }

    // TODO: 02.04.2017 Получаем данные о телефонном номере контакта для отправки ему СМС
    public void getNumberContact(Intent data) {

        getCursorContactAll(data);
        if (cursorContactAll != null && cursorContactAll.moveToNext()) {
            int columnIndex_ID = cursorContactAll.getColumnIndex(ContactsContract.Contacts._ID);
            String contact_ID = cursorContactAll.getString(columnIndex_ID);
            int columnIndex_HASPHONENUMBER = cursorContactAll.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            String stringHasPhoneNumber = cursorContactAll.getString(columnIndex_HASPHONENUMBER);

            if (stringHasPhoneNumber.equalsIgnoreCase("1")) {
                Cursor cursorNumber = getContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + "=" + contact_ID, null, null);
                if (cursorNumber != null && cursorNumber.moveToNext()) {
                    int columnIndex_number = cursorNumber
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    numberOfContact = cursorNumber.getString(columnIndex_number);
                    cursorNumber.close();
                }
            } else {
                Log.d(TAG_CONTACTS, "У контакта номера не найдено");
                numberOfContact = "";
            }
        }

        numberContactCallback.callBackContactNumber(numberOfContact);

    }

    // TODO: 02.04.2017 Получаем данные о телефонном номере контакта
    private void getNumberContact() {

        if (cursorContactAll != null && cursorContactAll.moveToNext()) {
            int columnIndex_ID = cursorContactAll.getColumnIndex(ContactsContract.Contacts._ID);
            String contact_ID = cursorContactAll.getString(columnIndex_ID);
            int columnIndex_HASPHONENUMBER = cursorContactAll.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            String stringHasPhoneNumber = cursorContactAll.getString(columnIndex_HASPHONENUMBER);

            if (stringHasPhoneNumber.equalsIgnoreCase("1")) {
                Cursor cursorNumber = getContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + "=" + contact_ID, null, null);
                if (cursorNumber != null && cursorNumber.moveToNext()) {
                    int columnIndex_number = cursorNumber
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    numberOfContact = cursorNumber.getString(columnIndex_number);
                    cursorNumber.close();
                }
            } else {
                Log.d(TAG_CONTACTS, "У контакта номера не найдено");
                numberOfContact = "";
            }
        }

    }

    // TODO: 02.04.2017 Получаем данные об электронном адресе контакта
    private void getEmailContact(){

        cursorEmail = getContext.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id},
                null);

        if (cursorEmail != null) {
            int saitIdx = cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

            if (cursorEmail.moveToFirst()) {
                emailOfContact = cursorEmail.getString(saitIdx);
                Log.d(TAG_CONTACTS, "Получаем адрес почты: " + emailOfContact);
            } else {
                Log.d(TAG_CONTACTS, "У контакта emailOfContact не найден");
                emailOfContact = "";
            }

            cursorEmail.close();
        }

    }



    // TODO: 02.04.2017 Получаем адрес вебсайта контакта
    private void getSaitContact(){

        cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToNext()) {

            String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
            String[] orgWhereParams = new String[]{id,
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE};
            cursorSait = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    null, orgWhere, orgWhereParams, null);
            if (cursorSait != null && cursorSait.moveToFirst()) {
                saitOfContact = cursorSait.getString(cursorSait.getColumnIndex(ContactsContract.CommonDataKinds.Website.DATA));
            }

            if (saitOfContact.length() == 0) {
                saitOfContact = "";
            }

            if (cursorSait != null) {
                cursorSait.close();
            }
        }

    }

    public void getContactData(Intent data) {

        getCursorContactAll(data);

        getNameContact();
        getNumberContact();
        getEmailContact();
        getSaitContact();

        myContactCallback.callBackContactData(nameOfContact,numberOfContact,emailOfContact,saitOfContact);
    }


    public void addContact(Context context,String name,String number,String email){
        // TODO: 23.03.2017 добавление контакта без показа пользователю
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation.Builder op =
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);

        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)

                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);

        ops.add(op.build());


        op =
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        ops.add(op.build());

        op =
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

        op.withYieldAllowed(true);

        // Builds the operation and adds it to the array of operations
        ops.add(op.build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            //Toast.makeText(getContext, "Test1 добавлен в список контактов!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG_CONTACTS, e.getMessage());
            //Toast.makeText(getContext, "Test1 не добавлен в список контактов!", Toast.LENGTH_LONG).show();
        }

        //Toast.makeText(getContext, "Test1 добавлен в список контактов", Toast.LENGTH_LONG).show();




        // TODO: 23.03.2017 добавление контакта с уведомлением (показом пользователю)
                /*Intent intent = new Intent(
                        ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                        ContactsContract.Contacts.CONTENT_URI);
                intent.setData(Uri.parse("tel:8-907-1111111"));
                intent.putExtra(ContactsContract.Intents.Insert.NAME, "Матроскин");
                intent.putExtra(ContactsContract.Intents.Insert.COMPANY, "Простоквашино");
                intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, "Охотник за мышами");
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, "cat@mail.ru");
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, "451");
                intent.putExtra(ContactsContract.Intents.Insert.POSTAL,
                        "д.17, Кошачья улица, Кот Д'Ивуар");
                startActivity(intent);*/
    }
}
