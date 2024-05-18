package ru.ptrff.tracktag.viewmodels;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.ptrff.tracktag.api.FirebaseHelper;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.models.Tag;
import ru.ptrff.tracktag.models.User;

public class TagViewModel extends ViewModel {
    private MutableLiveData<Boolean> deleteDone = new MutableLiveData<>();
    private FirebaseHelper helper;

    public TagViewModel() {
        helper = FirebaseHelper.getInstance();
    }

    @SuppressLint("CheckResult")
    public void likeTag(Tag tag, boolean like) {
        helper
                .likeDislike(tag, like)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (!result) {
                                Log.e(getClass().getCanonicalName(), "Error liking/disliking tag");
                            }
                        },
                        throwable -> Log.e(getClass().getCanonicalName(), throwable.toString())
                );
    }

    @SuppressLint("CheckResult")
    public void deleteTag(Tag tag) {
        helper
                .removeTag(tag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        booleanStringPair -> {
                            if (booleanStringPair.first) {
                                deleteDone.postValue(true);
                            } else {
                                Log.e(getClass().getCanonicalName(), booleanStringPair.second);
                            }
                        },
                        throwable -> {
                            Log.e(getClass().getCanonicalName(), throwable.toString());
                        }
                );
    }

    public void subscribe(Tag tag) {
        UserData data = UserData.getInstance();
        User user = tag.getUser();
        if (!data.isSubscribed(user)) {
            data.addSub(user);
        } else {
            data.removeSub(user);
        }
    }

    public MutableLiveData<Boolean> getDeleteDone() {
        return deleteDone;
    }
}
