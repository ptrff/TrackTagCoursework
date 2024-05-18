package ru.ptrff.tracktag.viewmodels;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.ptrff.tracktag.api.FirebaseHelper;

public class AddTagViewModel extends ViewModel {

    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private Uri imageUri;
    private String description;
    private double latitude, longitude;

    public AddTagViewModel() {
    }

    public void createImageUri(ContentResolver resolver) {
        String imageFileName = "photo";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        setImageUri(imageUri);
    }

    @SuppressLint("CheckResult")
    public void createTag(double latitude, double longitude, String description) {
        FirebaseHelper helper = FirebaseHelper.getInstance();

        helper
                .uploadImage(imageUri)
                .flatMap(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        return helper.createTag(latitude, longitude, description, booleanStringPair.second);
                    } else {
                        return Flowable.just(booleanStringPair);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(booleanStringPair -> {
                    if (booleanStringPair.first) {
                        success.postValue(true);
                    } else {
                        success.postValue(false);
                        Log.e(getClass().getCanonicalName(), "Error creating tag: " + booleanStringPair.second);
                    }
                }, throwable -> {
                    success.postValue(false);
                    Log.e(getClass().getCanonicalName(), "Error creating tag: " + throwable.getMessage());
                });
    }

    private byte[] convertToByteArray(ContentResolver resolver, Uri imageUri) {
        try {
            InputStream inputStream = resolver.openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] byteArray = byteBuffer.toByteArray();
            inputStream.close();
            return byteArray;
        } catch (IOException e) {
            Log.e(getClass().getCanonicalName(), "Error getting byte array from uri: " + e.getMessage());
            return null;
        }
    }

    public MutableLiveData<Boolean> getSuccess() {
        return success;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}