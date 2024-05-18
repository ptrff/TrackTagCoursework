package ru.ptrff.tracktag.viewmodels;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.ptrff.tracktag.api.FirebaseHelper;
import ru.ptrff.tracktag.data.Options;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.models.Option;
import ru.ptrff.tracktag.models.Tag;
import ru.ptrff.tracktag.models.User;

public class HomeViewModel extends ViewModel {
    private FirebaseHelper helper;
    private final MutableLiveData<List<Option>> options = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();

    public HomeViewModel() {
        helper = FirebaseHelper.getInstance();
    }


    public void initOptions() {
        options.postValue(Options.home);
    }

    @SuppressLint("CheckResult")
    public void getData() {
        helper
                .getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        receivedTags -> {
                            Collections.reverse(receivedTags);
                            tags.postValue(receivedTags);
                        },
                        throwable -> {
                            Log.e(getClass().getCanonicalName(), throwable.toString());
                        }
                );
    }

    public void updateLastTagsIDs(List<Tag> tags) {
        UserData data = UserData.getInstance();
        if (tags != null && !tags.isEmpty()) {
            for (User sub : data.getSubs()) {
                Log.d(getClass().getCanonicalName(), "updating last tag id for " + sub.getUsername());
                for (Tag tag : tags) {
                    if (tag.getUser() != null && tag.getUser().getUsername().equals(sub.getUsername())) {
                        Log.d(getClass().getCanonicalName(), "last tag set to " + tag.getId());
                        data.setLastTagId(sub, tag.getId());
                        break;
                    }
                }
            }
        }
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
                                List<Tag> newTags = tags.getValue();
                                newTags.remove(tag);
                                tags.postValue(newTags);
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

    public void updateLastTagsIDs() {
        updateLastTagsIDs(tags.getValue());
    }


    public MutableLiveData<List<Tag>> getTags() {
        return tags;
    }

    public MutableLiveData<List<Option>> getOptions() {
        return options;
    }
}
