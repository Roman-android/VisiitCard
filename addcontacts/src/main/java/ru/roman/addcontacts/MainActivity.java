package ru.roman.addcontacts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText name,number,email;
    Button saveContact;

    ContactsWork contactsWork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (EditText)findViewById(R.id.nameText);
        number = (EditText)findViewById(R.id.numberText);
        email = (EditText)findViewById(R.id.emailText);

        saveContact = (Button)findViewById(R.id.saveContact);

        contactsWork = new ContactsWork(this);
    }


    public void onClick(View view) {
        contactsWork.addContact(this,name.getText().toString(),number.getText().toString(),email.getText().toString());
    }
}
