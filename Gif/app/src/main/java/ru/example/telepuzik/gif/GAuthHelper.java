package ru.example.telepuzik.gif;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Telepuzik on 31.03.15.
 */
public class GAuthHelper {

    private AccountManager accountManager;
    private static final String ACC_TYPE = "com.google";
    private static final String SCOPE = "oauth2:https://docs.google.com/feeds/";
    private Activity act;

    public GAuthHelper(Activity activity) {
        Log.d("VkDemoApp", "Auth GAuthHelper");
        accountManager = AccountManager.get(activity);
        act = activity;
    }

    public Account[] getAccounts() {
        Log.d("VkDemoApp", "Auth getAccounts");
        Account[] accounts = accountManager.getAccountsByType(ACC_TYPE);
        return accounts;
    }

    public String[] getAccNames() {
        Log.d("VkDemoApp", "Auth getAccNames");
        Account[] accounts = getAccounts();
        String[] rez = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            rez[i] = accounts[i].name;
        }
        return rez;
    }

    private Account getAccountByName(String name) {
        Log.d("VkDemoApp", "Auth getAccountByName");
        Account[] accounts = getAccounts();
        for (int i = 0; i < accounts.length; i++) {
            if (name.equals(accounts[i].name)) return accounts[i];
        }
        return null;
    }

    public void invalidateToken(String token) {
        Log.d("VkDemoApp", "Auth invalidateToken");
        accountManager.invalidateAuthToken(ACC_TYPE, token);
    }

    public void getAuthToken(String accname, OAuthCallbackListener authCallbackListener) {
        Log.d("VkDemoApp", "Auth getAuthToken1");
        getAuthToken(getAccountByName(accname), authCallbackListener);
    }

    public void getAuthToken(Account account, final OAuthCallbackListener authCallbackListener) {
        Log.d("VkDemoApp", "Auth getAuthToken2");
        accountManager.getAuthToken(account, SCOPE, null, act,
                new AccountManagerCallback<Bundle>() {
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                            authCallbackListener.callback(token);
                        } catch (OperationCanceledException e) {
                            authCallbackListener.callback(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);

    }

    public static interface OAuthCallbackListener {
        public void callback(String authToken);
    }
}