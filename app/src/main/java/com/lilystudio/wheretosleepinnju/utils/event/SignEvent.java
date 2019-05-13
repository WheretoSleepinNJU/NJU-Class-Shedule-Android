package com.lilystudio.wheretosleepinnju.utils.event;

public class SignEvent {
    private boolean isSignOut = false;

    public boolean isSignOut() {
        return isSignOut;
    }

    public SignEvent setSignOut(boolean signOut) {
        isSignOut = signOut;
        return this;
    }
}
