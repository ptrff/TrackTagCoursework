package ru.ptrff.tracktag.utils;

import androidx.room.TypeConverter;

import java.util.HashMap;

import ru.ptrff.tracktag.data.UserData;

public class LikesConverter {
    @TypeConverter
    public boolean fromHashMap(HashMap<String, Object> data) {
        System.out.println("data = " + data);
        return true;
    }

    @TypeConverter
    public HashMap<String, Object> toHashMap(boolean isLiked) {
        String id = UserData.getInstance().getUserId();

        HashMap<String, Object> data = new HashMap<>();
        if (isLiked) data.put("id", id);
        return data;
    }
}
