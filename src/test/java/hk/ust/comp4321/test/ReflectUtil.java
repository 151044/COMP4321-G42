package hk.ust.comp4321.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * A utility class for performing frequently used reflection hacks.
 */
public class ReflectUtil {
    private ReflectUtil() {
        throw new AssertionError("ReflectUtil cannot be instantiated!");
    }

    /**
     * Retrieves an instance of a private inner class from the given object.
     * @param name The name of the inner class
     * @param type The type of the inner class, for casting
     * @param rootObj The object to create an instance of the inner class from
     * @return An instance of the inner class
     * @param <T> The return type of the function
     * @throws NoSuchMethodException If the no-argument constructor does not exist
     * @throws InvocationTargetException If the constructor throws an exception
     * @throws InstantiationException If there is a problem with instantiating an object
     * @throws IllegalAccessException If setting the inner class to public fails
     */
    public static <T> T getInnerInstance(String name, Class<T> type, Object rootObj) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Arrays.stream(rootObj.getClass().getDeclaredClasses())
                .filter(c -> c.getName().equals(rootObj.getClass().getName() + "$" + name))
                .findFirst()
                .orElseThrow();
        Constructor<?> cons = clazz.getDeclaredConstructor(rootObj.getClass());
        cons.setAccessible(true);
        return type.cast(cons.newInstance(rootObj));
    }

    /**
     * Retrieves a private field from the instance supplied.
     * Warning: This does not work if you have anonymously subclassed said class.
     * Subclasses cannot read the private fields of a superclass.
     * @param name The name of the private field to retrieve
     * @param type The return type, used for casting
     * @param instance The object instance to retrieve the private field from
     * @return The contents of the retrieved field
     * @param <T> The return type
     * @throws NoSuchFieldException If the specified field does not exist
     * @throws IllegalAccessException If setting the private field to public fails
     */
    public static <T> T getField(String name, Class<T> type, Object instance) throws NoSuchFieldException, IllegalAccessException {
        /*
        Programmer warning: This does not work if you have anonymously subclassed said object...
         */
        Field f = instance.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return type.cast(f.get(instance));
    }

    /**
     * Retrieves a private static field from a class.
     * @param name The name of the field
     * @param type The type of the field, for casting to the correct return type
     * @param toReflect The static class to use reflection on
     * @return The static instance of the requested private field
     * @param <T> The return type of the method, and the type of the field
     * @param <U> The type of the class to use reflection on
     * @throws NoSuchFieldException If the field specified does not exist
     * @throws IllegalAccessException If setting the private field to public fails
     */
    public static <T, U> T getStaticField(String name, Class<T> type, Class<U> toReflect) throws NoSuchFieldException, IllegalAccessException {
        Field f = toReflect.getDeclaredField(name);
        f.setAccessible(true);
        return type.cast(f.get(null));
    }

    /**
     * Sets the contents of a private field from a given object instance.
     * @param name The name of the private field
     * @param instance The object to retrieve said field from
     * @param toSet The value to set onto the field
     * @throws NoSuchFieldException If the field specified does not exist
     * @throws IllegalAccessException If setting the private field to be public fails
     */
    public static void setField(String name, Object instance, Object toSet) throws NoSuchFieldException, IllegalAccessException {
        Field f = instance.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(instance, toSet);
    }

    /**
     * Sets a private field from a static class.
     * @param name The name of the field
     * @param toReflect The static class to use reflection on
     * @param <U> The type of the class to use reflection on
     * @throws NoSuchFieldException If the field specified does not exist
     * @throws IllegalAccessException If setting the private field to public fails
     */
    public static <U> void setStaticField(String name, Object toSet, Class<U> toReflect) throws NoSuchFieldException, IllegalAccessException {
        Field f = toReflect.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, toSet);
    }
}
