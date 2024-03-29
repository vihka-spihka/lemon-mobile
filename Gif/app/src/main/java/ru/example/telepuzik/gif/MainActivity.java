package ru.example.telepuzik.gif;

import android.app.AlertDialog;
import android.app.ListActivity;


import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends ListActivity {

/*    private final static String G_PLUS_SCOPE ="oauth2:https://www.googleapis.com/auth/plus.me";//права доступа
    private final static String USER_INFO_SCOPE ="https://www.googleapis.com/auth/userinfo.profile";//получение имя фамилии фото и тд
    private final static String EMAIL_SCOPE ="https://www.googleapis.com/auth/userinfo.email";//получение email
    //private final static String FOTO_SCOPE ="https://www.googleapis.com/auth/userinfo.photo";//получение фото
    private final static String SCOPES = G_PLUS_SCOPE + " " + USER_INFO_SCOPE + " " + EMAIL_SCOPE;
    String tokenGoogle;*/
    private static final String PREF_TOKEN = "token";
    private GAuthHelper gah;
    private static final String VK_APP_ID = "4818942";
    private VKRequest currentRequest;

    private Button loginButton;
    private Button exitButton;

    private final List<User> users = new ArrayList<User>();
    private ArrayAdapter<User> listAdapter;
    private final VKSdkListener sdkListener = new VKSdkListener() {

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onAcceptUserTokenVK " + token);
            startLoading();

        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.d("VkDemoApp", "onReceiveNewTokenVK " + newToken);
            Log.d("VkDemoApp", "onReceiveNewTokenVK " + VKSdk.isLoggedIn());
            startLoading();
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onRenewAccessTokenVK " + token);
            startLoading();
        }

        @Override
        public void onCaptchaError(VKError captchaError) {
            Log.d("VkDemoApp", "onCaptchaErrorVK " + captchaError);
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.d("VkDemoApp", "onTokenExpiredVK " + expiredToken);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.d("VkDemoApp", "onAccessDeniedVK " + authorizationError);
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//где-то тут вывод всех документов на экран
        //setListAdapter(listAdapter);

        VKSdk.initialize(sdkListener, VK_APP_ID);
        VKUIHelper.onCreate(this);

        exitButton=(Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("VkDemoApp", "exit");
                VKSdk.getAccessToken().accessToken=null;
                VKSdk.logout();
                Log.d("VkDemoApp", "log on/off " + VKSdk.isLoggedIn());
                Log.d("VkDemoApp", "Token VK " + VKSdk.getAccessToken());
                Log.d("VkDemoApp", "google token" +  PREF_TOKEN);
                loginButton.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.GONE);
            }
        });

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("VkDemoApp", "authorize ");
                VKSdk.authorize(VKScope.DOCS);

                //Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},false, null, null, null, null);
                //startActivityForResult(intent, 123);
                gah = new GAuthHelper(MainActivity.this);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String authToken = prefs.getString(PREF_TOKEN, "");
                Log.d("VkDemoApp", "Click authToken " +authToken);
                if (authToken.length()==0) { // token not found, need authorization
                    final String[] accn = gah.getAccNames();
                    if (accn.length==0) {
                        Toast.makeText(MainActivity.this, "Stored Google accounts not found", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Select a Google account");
                        builder.setItems(accn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, final int which) {
                                saveToken(accn[which]);
                            }
                        }).create().show();
                    }
                } else {     // use stored token
                    loadGDocs(authToken);
                }
            }
        });

        if (VKSdk.wakeUpSession()) {
            Log.d("VkDemoApp", "wakeUpSession");
            startLoading();
        } else {
            Log.d("VkDemoApp", "Login");
            loginButton.setVisibility(View.VISIBLE);
        }

    }


    private void saveToken(String accname) {
        Log.d("VkDemoApp", "Main SaveToken ");
        gah.getAuthToken(accname, new GAuthHelper.OAuthCallbackListener() {
            @Override
            public void callback(String authToken) {
                if (authToken==null) {
                    Toast.makeText(MainActivity.this, "Operation cancelled", Toast.LENGTH_LONG).show();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString(PREF_TOKEN, authToken).commit();
                    Log.d("VkDemoApp", "SaveToken authToken " +authToken);
                    loadGDocs(authToken);
                }
            }
        });
    }

    private void loadGDocs(final String token) {
        Log.d("VkDemoApp", "loadGDocs mainact");
        new DocsLoader(this){
            protected void onPostExecute(String[] result) {
                ScrollView sw = new ScrollView(MainActivity.this);
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);

                if (result!=null) {
                    Log.d("VkDemoApp", "result1 " + result);
                    for (String s : result) {
                        Log.d("VkDemoApp", "result2 " + result);
                        Log.d("VkDemoApp", "s " + s);
                        TextView tw = new TextView(MainActivity.this);
                        tw.setText(s);
                        ll.addView(tw);
                    }
                } else { // token expired: reload
                    Log.d("VkDemoApp", "loadGDocs mainact else token expired: reload");
                    Log.d("VkDemoApp", "token " + token);
                    gah.invalidateToken(token);
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString(PREF_TOKEN, "").commit();
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    finish();
                }
                sw.addView(ll);
                setContentView(sw);
                super.onPostExecute(result);
            };
        }.execute(token);

    }


    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);//vk

/*
        if (requestCode == 123 && resultCode == RESULT_OK) {//--------------------------------------------Google+
            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            AsyncTask<Void, Void, String> getToken = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        tokenGoogle = GoogleAuthUtil.getToken(MainActivity.this, accountName,SCOPES);
                        Log.d("VkDemoApp", "TokenG+ " + tokenGoogle);
                        return tokenGoogle;

                    } catch (UserRecoverableAuthException userAuthEx) {
                        Log.d("VkDemoApp", "Start Activity");
                        startActivityForResult(userAuthEx.getIntent(), 123);
                    }  catch (IOException ioEx) {
                        Log.d("VkDemoApp", "IOException");
                    }  catch (GoogleAuthException fatalAuthEx)  {
                        Log.d("VkDemoApp", "Fatal Authorization Exception" + fatalAuthEx.getLocalizedMessage());
                    }
                    return tokenGoogle;
                }

               /* @Override
                protected void onPostExecute(String token) {
                    Log.d("VkDemoApp", "RegToken ");
                    //reg(tokenGoogle);
                }

            };
            getToken.execute(null, null, null);
        }//---------------------------------------------------------------------------------------------------*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
    }

    private void startLoading() {
        loginButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.VISIBLE);
        if (currentRequest != null) {
            currentRequest.cancel();
        }

//запросы
        currentRequest  = new VKRequest ("docs", VKParameters.from(VKApiDocument.CONTENTS_FILE_DESCRIPTOR),VKRequest.HttpMethod.GET,VKApiDocument.class);
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "LoadDocs");
                //тут загрузка в vk документы
            }
            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

}