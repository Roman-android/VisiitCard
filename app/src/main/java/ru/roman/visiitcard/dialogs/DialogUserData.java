package ru.roman.visiitcard.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.roman.visiitcard.R;

/**
 * Created by Roman on 10.10.2017.
 */

public class DialogUserData extends AppCompatDialogFragment {

    EditText nameUser, numberUser,emailUser;

    public DialogUserData() {
    }

    public interface DialogUserDataListener{
        void onPositiveButton(String nameUser,String numberUser,String emailUser);
    }

    DialogUserDataListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_user_data,null);
        dialog.setView(view);
        //dialog.setCancelable(false);

        nameUser = (EditText)view.findViewById(R.id.nameUser);
        numberUser = (EditText)view.findViewById(R.id.numUser);
        emailUser = (EditText)view.findViewById(R.id.emailUser);

        dialog
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String getName = nameUser.getText().toString();
                        String getNumber = numberUser.getText().toString();
                        String getEmail = emailUser.getText().toString();
                        mListener.onPositiveButton(getName,getNumber,getEmail);
                        //dialogInterface.cancel();
                    }
                });


        return dialog.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DialogUserDataListener)context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
