package com.example.telepuzik.gif;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.vk.sdk.dialogs.VKShareDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import java.util.ArrayList;
import java.util.List;


public class MainUiFragment extends ListFragment {



    interface Callback {
        void onLoginButtonClick();
    }

    private Callback callback;
    private Button loginButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (Callback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onLoginButtonClick();

                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},false, null, null, null, null);
                startActivityForResult(intent, 123);
            }
        });
        return view;
    }


    public void setLoginVisible(boolean loginVisible) {
        loginButton.setVisibility(loginVisible ? View.VISIBLE : View.GONE);
    }

}