package com.polimi.proj.qdocs.support;

import com.google.firebase.auth.FirebaseUser;

public class User {

    public enum LoginMode {ANONYMOUS, GOOGLE, FACEBOOK, EMAIL, UNKNOWN}

    // attributes
    private String uid;
    private String email;
    private String username;
    private LoginMode mode;

    private static User ourInstance = null;

    /**
     * create the user whether it is no yet created
     * @param user FirebaseUser from which get information
     * @param mode LoginMode how the user has been logged in
     */
    public static void createUser(FirebaseUser user, LoginMode mode) {
        if (ourInstance != null)
            return;

        ourInstance = new User(user.getUid(), user.getEmail(), user.getDisplayName(), mode);
    }

    public static User getUser() {
        return ourInstance;
    }

    private User(String uid, String username, String email, LoginMode mode) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.mode = mode;
    }

    public String getUid() {
        return uid;
    }
}
