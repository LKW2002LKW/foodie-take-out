package com.foodie.common.context;

/**
 * Thread-local context holder for user-specific data.
 * Minimal skeleton to match project structure.
 */
public class UserContext {

    private static final ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();

    public static void setUserId(Long id) {
        userThreadLocal.set(id);
    }

    public static Long getUserId() {
        return userThreadLocal.get();
    }

    public static void remove() {
        userThreadLocal.remove();
    }

}

