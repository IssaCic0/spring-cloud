package com.baidu.security;

public class RequestContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Role> ROLE = new ThreadLocal<>();

    public static void setUserId(Long id) { USER_ID.set(id); }
    public static Long getUserId() { return USER_ID.get(); }

    public static void setRole(Role role) { ROLE.set(role); }
    public static Role getRole() { return ROLE.get(); }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}

