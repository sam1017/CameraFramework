package com.aiworks.android.utils;

import android.util.Size;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraHelper {

    private static final Constructor<?> CharacteristicsKeyConstructor = getKeyConstructor("android.hardware.camera2.CameraCharacteristics$Key");
    private static final Constructor<?> CaptureRequestKeyConstructor = getKeyConstructor("android.hardware.camera2.CaptureRequest$Key");
    private static final Constructor<?> CaptureResultKeyConstructor = getKeyConstructor("android.hardware.camera2.CaptureResult$Key");

    private static Constructor getKeyConstructor(String className) {
        Class<?> c = getClass(className);
        Constructor<?>[] constructors = c.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            Type[] types = constructor.getGenericParameterTypes();
            if (types.length > 1 && types[0].toString().equals("class java.lang.String")
                    && types[1].toString().equals("java.lang.Class<T>")) {
                return constructor;
            }
        }
        return null;
    }

    public static Object getCharacteristicsKey(String name, Class<?> type) {
        return getInstance(CharacteristicsKeyConstructor, name, type);
    }

    public static Object getCaptureRequestKey(String name, Class<?> type) {
        return getInstance(CaptureRequestKeyConstructor, name, type);
    }

    public static Object getCaptureResultKey(String name, Class<?> type) {
        return getInstance(CaptureResultKeyConstructor, name, type);
    }

    public static Class<?> getClass(String className) {
        Class<?> class1 = null;
        try {
            class1 = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return class1;
    }

    public static Object getInstance(Constructor<?> constructor, Object... parameters) {
        Object instance = null;
        if (null != parameters)
            try {
                instance = constructor.newInstance(parameters);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        else
            try {
                instance = constructor.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        return instance;
    }

    public static Method getMethod(String className, String methodName,
                                   Class<?>... parameterTypes) {
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

    public static List<Size> buildListFromAndroidSizes(List<Size> androidSizes) {
        ArrayList<Size> list = new ArrayList<Size>(androidSizes.size());
        for (Size androidSize : androidSizes) {
            list.add(new Size(androidSize.getWidth(), androidSize.getHeight()));
        }
        return list;
    }

}
