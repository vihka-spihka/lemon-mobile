package com.example.telepuzik.gif;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements MainUiFragment.Callback {

    private static final String VK_APP_ID = "4818942";

    private final VKSdkListener sdkListener = new VKSdkListener() {

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            //Log.d("MyLemonProject", "onAcceptUserToken " + token);
            startLoading();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            //Log.d("MyLemonProject", "onReceiveNewToken " + newToken);
            startLoading();
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            //Log.d("MyLemonProject", "onRenewAccessToken " + token);
            startLoading();
        }

        @Override
        public void onCaptchaError(VKError captchaError) {
            //Log.d("MyLemonProject", "onCaptchaError " + captchaError);
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            //Log.d("MyLemonProject", "onTokenExpired " + expiredToken);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            //Log.d("MyLemonProject", "onAccessDenied " + authorizationError);
        }

    };

    private VKRequest currentRequest;

    private MainUiFragment uiFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        uiFragment = (MainUiFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);

        VKSdk.initialize(sdkListener, VK_APP_ID);

        VKUIHelper.onCreate(this);
        if (VKSdk.wakeUpSession()) {
            startLoading();
        } else {
            uiFragment.setLoginVisible(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
    }

    private void startLoading() {//активити список друзей+дата+запрос
        uiFragment.setLoginVisible(false);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
        currentRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                //Log.d("MyLemonProject", "onComplete " + response);

                final List<User> users = new ArrayList<User>();

                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                final String[] formats = new String[]{"dd.MM.yyyy", "dd.MM"};

                for (VKApiUserFull userFull : usersArray) {
                    DateTime birthDate = null;
                    String format = null;
                    if (!TextUtils.isEmpty(userFull.bdate)) {
                        for (int i = 0; i < formats.length; i++) {
                            format = formats[i];
                            try {
                                birthDate = DateTimeFormat.forPattern(format).parseDateTime(userFull.bdate);
                            } catch (Exception ignored) {
                            }
                            if (birthDate != null) {
                                break;
                            }
                        }

                    }
                    users.add(new User(userFull.toString(), birthDate, format));
                }

                uiFragment.setUsers(users);

            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                //Log.d("MyLemonProject", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                //Log.d("MyLemonProject", "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                //Log.d("MyLemonProject", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    @Override
    public void onLoginButtonClick() {
        VKSdk.authorize(VKScope.FRIENDS, VKScope.WALL);//DOCS
    }

}