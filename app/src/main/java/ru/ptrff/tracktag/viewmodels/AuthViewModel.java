package ru.ptrff.tracktag.viewmodels;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.ptrff.tracktag.api.FirebaseHelper;
import ru.ptrff.tracktag.data.UserData;

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authError = new MutableLiveData<>();
    private String authErrorText;
    private FirebaseHelper helper;

    public AuthViewModel() {
        helper = FirebaseHelper.getInstance();
    }

    @SuppressLint("CheckResult")
    public void register(String email, String username, String password) {
        helper
                .register(email, password)
                .flatMap(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        return helper.addNewUserToDatabase(booleanStringPair.second, username);
                    } else {
                        return Flowable.just(booleanStringPair);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        Map<String, Object> userData = (Map<String, Object>) parseStringToMap(booleanStringPair.second);
                        String role = (String) userData.get("role");
                        String id = (String) userData.get("id");

                        UserData data = UserData.getInstance();
                        data.setUserId(id);
                        data.setUserName(username);
                        data.setRole(role);
                        data.setEmail(email);
                        data.setLoggedIn(true);

                        loggedIn.postValue(true);
                    } else {

                        authErrorText = booleanStringPair.second;
                        authError.postValue(true);
                    }
                }, throwable -> Log.e(getClass().getCanonicalName(), throwable.toString()));
    }

    @SuppressLint("CheckResult")
    public void login(String email, String password) {
        helper
                .login(email, password)
                .flatMap(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        return helper.findUserInDatabase(booleanStringPair.second);
                    } else {
                        return Flowable.just(booleanStringPair);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        Map<String, Object> userData = (Map<String, Object>) parseStringToMap(booleanStringPair.second);
                        String username = (String) userData.get("username");
                        String role = (String) userData.get("role");
                        String id = (String) userData.get("id");

                        UserData data = UserData.getInstance();
                        data.setUserId(id);
                        data.setUserName(username);
                        data.setRole(role);
                        data.setEmail(email);
                        data.setLoggedIn(true);

                        loggedIn.postValue(true);
                    } else {
                        authErrorText = booleanStringPair.second;
                        authError.postValue(true);
                    }
                }, throwable -> Log.e(getClass().getCanonicalName(), throwable.toString()));
    }

    public static Map<String, Object> parseStringToMap(String mapString) {
        String[] keyValuePairs = mapString.substring(1, mapString.length() - 1).split(", ");

        Map<String, Object> map = new HashMap<>();
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            map.put(entry[0], entry[1]);
        }
        return map;
    }

    public MutableLiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public MutableLiveData<Boolean> getAuthError() {
        return authError;
    }

    public String getAuthErrorText() {
        return authErrorText;
    }
}
