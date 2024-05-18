package ru.ptrff.tracktag.api;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.models.Tag;
import ru.ptrff.tracktag.models.User;

public class FirebaseHelper {

    private static FirebaseHelper instance;

    // Firebase stuff
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private DatabaseReference tags;
    private StorageReference storage;

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("users");
        tags = database.getReference("tags");
        storage = FirebaseStorage.getInstance().getReference().child("images");

        updateInfo();
    }

    public Flowable<Boolean> likeDislike(Tag tag, boolean like) {
        return Flowable.create(
                emitter -> {
                    if (like) {
                        tags
                                .child(tag.getId())
                                .child("likes")
                                .child(auth.getUid())
                                .setValue(auth.getUid())
                                .addOnSuccessListener(command -> {
                                    emitter.onNext(true);
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(command -> {
                                    emitter.onNext(false);
                                    emitter.onComplete();
                                });
                    } else {
                        tags
                                .child(tag.getId())
                                .child("likes")
                                .child(auth.getUid())
                                .removeValue()
                                .addOnSuccessListener(command -> {
                                    emitter.onNext(true);
                                    emitter.onComplete();
                                })
                                .addOnFailureListener(command -> {
                                    emitter.onNext(false);
                                    emitter.onComplete();
                                });
                    }
                }, BackpressureStrategy.BUFFER
        );
    }

    private void updateInfo(){
        users
                .get()
                .addOnSuccessListener(command -> {
                    for (DataSnapshot mapUser : command.getChildren()) {
                        String id = UserData.getInstance().getUserId();
                        User user = mapUser.getValue(User.class);
                        if (user != null && user.getId().equals(id)) {
                            UserData.getInstance().setRole(user.getRole());
                            UserData.getInstance().setUserName(user.getUsername());
                            break;
                        }
                    }
                })
                .addOnFailureListener(command -> {
                    Log.e(this.getClass().getCanonicalName(), command.getMessage());
                });
    }

    public Flowable<Pair<Boolean, String>> removeTag(Tag tag) {
        return Flowable.create(
                emitter -> {
                    tags
                            .get()
                            .addOnSuccessListener(command -> {
                                for (DataSnapshot mapTag : command.getChildren()) {
                                    Tag t = mapTag.getValue(Tag.class);
                                    if (t.getId().equals(tag.getId())) {
                                        tags
                                                .child(t.getId())
                                                .removeValue();
                                        break;
                                    }
                                }
                                emitter.onNext(new Pair<>(true, ""));
                                emitter.onComplete();
                            })
                            .addOnFailureListener(command -> {
                                emitter.onNext(new Pair<>(false, command.getMessage()));
                                emitter.onComplete();
                            });
                }, BackpressureStrategy.LATEST
        );
    }

    public Flowable<List<Tag>> getAllTags() {
        return Flowable.create(
                emitter -> {
                    tags
                            .get()
                            .addOnSuccessListener(command -> {
                                List<Tag> tags = new ArrayList<>();
                                for (DataSnapshot mapTag : command.getChildren()) {
                                    tags.add(mapTag.getValue(Tag.class));
                                }
                                emitter.onNext(tags);
                                emitter.onComplete();
                            })
                            .addOnFailureListener(command -> {
                                emitter.onNext(new ArrayList<>());
                                emitter.onComplete();
                            });
                }, BackpressureStrategy.LATEST
        );
    }

    public Flowable<Pair<Boolean, String>> uploadImage(Uri imageUri) {
        if (imageUri == null) return Flowable.just(new Pair<>(true, ""));

        return Flowable.create(
                emitter -> {
                    StorageReference imageRef = storage.child(UUID.randomUUID().toString());
                    imageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        emitter.onNext(new Pair<>(true, uri.toString()));
                                        emitter.onComplete();
                                    })
                                    .addOnFailureListener(e -> {
                                        emitter.onNext(new Pair<>(false, e.getMessage()));
                                        emitter.onComplete();
                                    }))
                            .addOnFailureListener(command -> {
                                emitter.onNext(new Pair<>(false, command.getMessage()));
                                emitter.onComplete();
                            });
                }, BackpressureStrategy.LATEST
        );
    }

    public Flowable<Pair<Boolean, String>> createTag(
            double latitude, double longitude,
            String description, @Nullable String imageUrl) {

        UserData userData = UserData.getInstance();
        String name = userData.getUserName();
        String id = userData.getUserId();
        String role = userData.getRole();
        User user = new User(id, name, role);

        DatabaseReference newTag = tags.push();

        if (newTag == null) return Flowable.just(new Pair<>(false, "Error creating tag"));

        Tag tag = new Tag(
                newTag.getKey(),
                latitude,
                longitude,
                description,
                imageUrl,
                new HashMap<>(),
                user
        );

        return Flowable.create(
                emitter -> {
                    newTag
                            .setValue(tag)
                            .addOnSuccessListener(command -> emitter.onNext(
                                    new Pair<>(true, "")
                            ))
                            .addOnFailureListener(command -> emitter.onNext(
                                    new Pair<>(false, command.getMessage())
                            ))
                            .addOnCompleteListener(task -> emitter.onComplete());
                }, BackpressureStrategy.BUFFER
        );
    }

    public Flowable<Pair<Boolean, String>> login(String email, String password) {
        return Flowable.create(
                emitter -> auth
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(task -> emitter.onNext(
                                new Pair<>(true, task.getUser().getUid())
                        ))
                        .addOnFailureListener(task -> emitter.onNext(
                                new Pair<>(false, task.getMessage())
                        ))
                        .addOnCompleteListener(task -> emitter.onComplete()),
                BackpressureStrategy.BUFFER
        );
    }

    public Flowable<Pair<Boolean, String>> findUserInDatabase(String id) {
        return Flowable.create(
                emitter -> users
                        .child(id)
                        .get()
                        .addOnSuccessListener(command -> {
                            if (command.getValue() != null) {
                                emitter.onNext(new Pair<>(true, command.getValue().toString()));
                            } else {
                                emitter.onNext(new Pair<>(false, "User not found in database"));
                            }
                        })
                        .addOnFailureListener(command -> emitter.onNext(
                                new Pair<>(false, command.getMessage())
                        ))
                        .addOnCompleteListener(task -> emitter.onComplete()),
                BackpressureStrategy.BUFFER
        );
    }

    public Flowable<Pair<Boolean, String>> register(String email, String password) {
        return Flowable.create(
                emitter -> auth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(task -> emitter.onNext(
                                new Pair<>(true, task.getUser().getUid())
                        ))
                        .addOnFailureListener(task -> emitter.onNext(
                                new Pair<>(false, task.getMessage())
                        ))
                        .addOnCompleteListener(task -> emitter.onComplete()),
                BackpressureStrategy.BUFFER
        );
    }

    public Flowable<Pair<Boolean, String>> addNewUserToDatabase(String id, String name) {
        User u = new User(id, name, "user");
        return Flowable.create(
                emitter -> users
                        .child(id)
                        .setValue(u)
                        .addOnSuccessListener(unused -> emitter.onNext(
                                new Pair<>(true, u.toString())
                        ))
                        .addOnFailureListener(command -> emitter.onNext(
                                new Pair<>(false, command.getMessage())
                        ))
                        .addOnCompleteListener(task -> emitter.onComplete()),
                BackpressureStrategy.BUFFER
        );
    }
}
