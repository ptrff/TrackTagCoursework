package ru.ptrff.tracktag.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {

    private String id;
    private String username;
    private String role;

    public User() {

    }

    public User(String id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        role = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(role);
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", username=" + username +
                ", role=" + role +
                '}';
    }
}
