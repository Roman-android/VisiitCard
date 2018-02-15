package ru.roman.visiitcard.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import ru.roman.visiitcard.R;

/**
 * Created by Roman on 03.01.2018.
 */

public class DialogInfo extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder dialogInfo =  new AlertDialog.Builder(getActivity());
        dialogInfo
                .setMessage(R.string.info)
                .setPositiveButton("Понятно!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });




        return dialogInfo.create();
    }
}
