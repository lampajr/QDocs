package com.polimi.proj.qdocs.support;

import com.google.firebase.auth.FirebaseUser;

public class User {

    // modes
    public static final String ANONYMOUS = "ANONYMOUS";
    public static final String GOOGLE = "GOOGLE";
    public static final String FACEBOOK = "FACEBOOK";
    public static final String EMAIL = "EMAIL";
    public static final String UNKNOWN = "UNKNOWN";

    // attributes
    private String mode; // 'email, google, facebook, anonymous
    private String uid;
    private String email;
    private String username;

    private static User ourInstance = null;

    public static void createUser(FirebaseUser user, String mode) {
        if (ourInstance != null)
            return;

        ourInstance = new User(user.getUid(), user.getEmail(), user.getDisplayName(), mode);
    }

    public static void updateUser(FirebaseUser user, String mode) {
        ourInstance = new User(user.getUid(), user.getEmail(), user.getDisplayName(), mode);
    }

    public static User getUser() {
        return ourInstance;
    }

    private User(String uid, String username, String email, String mode) {
        this.email = email;
        this.username = username;
        this.mode = mode;
    }
}
