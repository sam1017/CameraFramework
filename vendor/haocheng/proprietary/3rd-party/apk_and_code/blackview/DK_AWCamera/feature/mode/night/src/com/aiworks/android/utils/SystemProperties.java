package com.aiworks.android.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemProperties {

    public static String get(String key) {
        Method method = getMethod("android.os.SystemProperties", "get", String.class);
        return (String) invokeMethod(method, null, key);
    }

    public static String get(String key, String def) {
        Method method = getMethod("android.os.SystemProperties", "get", String.class, String.class);
        return (String) invokeMethod(method, null, key, def);
    }

    public static int getInt(String key, int def) {
        Method method = getMethod("android.os.SystemProperties", "getInt", String.class, int.class);
        return (int) invokeMethod(method, null, key, def);
    }

    public static long getLong(String key, long def) {
        Method method = getMethod("android.os.SystemProperties", "getLong", String.class, long.class);
        return (long) invokeMethod(method, null, key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        Method method = getMethod("android.os.SystemProperties", "getBoolean", String.class, boolean.class);
        return (boolean) invokeMethod(method, null, key, def);
    }

    public static void set(String key, String val) {
        Method method = getMethod("android.os.SystemProperties", "set", String.class, String.class);
        invokeMethod(method, null, key, val);
    }

    public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Class<?> clazz;
        Method method = null;
        try {
            clazz = Class.forName(className);
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static Object invokeMethod(Method method, Object obj, Object... parameters) {
        Object retObj = null;
        if (null == method)
            return null;
        try {
            retObj = method.invoke(obj, parameters);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retObj;
    }

}
