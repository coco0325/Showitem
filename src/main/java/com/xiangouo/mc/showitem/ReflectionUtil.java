package com.xiangouo.mc.showitem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;

/*
    This class is copied from web
 */

/**
 * Java Reflection Tools
 */
public class ReflectionUtil {

    /*
     * The server version string to location NMS & OBC classes
     */
    private static String versionString;

    /*
     * Cache of NMS classes that we've searched for
     */
    private static Map<String, Class<?>> loadedNMSClasses = new HashMap<String, Class<?>>();

    /*
     * Cache of OBS classes that we've searched for
     */
    private static Map<String, Class<?>> loadedOBCClasses = new HashMap<String, Class<?>>();

    /*
     * Cache of methods that we've found in particular classes
     */
    private static Map<Class<?>, Map<String, Method>> loadedMethods = new HashMap<Class<?>, Map<String, Method>>();

    /*
     * Cache of fields that we've found in particular classes
     */
    private static Map<Class<?>, Map<String, Field>> loadedFields = new HashMap<Class<?>, Map<String, Field>>();

    /**
     * Gets the version string for NMS and OBC class paths
     *
     * @return The version string of OBC and NMS packages
     */
    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    /**
     * Get an NMS Class
     *
     * @param nmsClassName The name of the class
     * @return The class
     */
    public static Optional<Class<?>> getNMSClass(String nmsClassName) {
        if (loadedNMSClasses.containsKey(nmsClassName)) {
            return Optional.ofNullable(loadedNMSClasses.get(nmsClassName));
        }
        String clazzName = "net.minecraft.server." + getVersion() + nmsClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.ofNullable(loadedNMSClasses.put(nmsClassName, null));
        }

        loadedNMSClasses.put(nmsClassName, clazz);
        return Optional.ofNullable(clazz);
    }

    /**
     * Get a class from the org.bukkit.craftbukkit package
     *
     * @param obcClassName the path to the class
     * @return the found class at the specified path
     */
    public synchronized static Optional<Class<?>> getOBCClass(String obcClassName) {
        if (loadedOBCClasses.containsKey(obcClassName)) {
            return Optional.of(loadedOBCClasses.get(obcClassName));
        }

        String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            loadedOBCClasses.put(obcClassName, null);
            return Optional.empty();
        }

        loadedOBCClasses.put(obcClassName, clazz);
        return Optional.ofNullable(clazz);
    }

    /**
     * Get a Bukkit {@link Player} players NMS playerConnection object
     *
     * @param player The player
     * @return The players connection
     */
    public static Optional<Object> getConnection(Player player) {
        Optional<Method> getHandleMethod = getMethod(player.getClass(), "getHandle");

        if (getHandleMethod.isPresent()) {
            try {
                Object nmsPlayer = getHandleMethod.get().invoke(player);
                Field playerConField = getField(nmsPlayer.getClass(), "playerConnection").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
                return Optional.ofNullable(playerConField.get(nmsPlayer));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    /**
     * Get a classes constructor
     *
     * @param clazz  The constructor class
     * @param params The parameters in the constructor
     * @return The constructor object
     */
    public static Optional<Constructor<?>> getConstructor(Class<?> clazz, Class<?>... params) {
        try {
            return Optional.ofNullable(clazz.getConstructor(params));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Get a method from a class that has the specific paramaters
     *
     * @param clazz      The class we are searching
     * @param methodName The name of the method
     * @return The method with appropriate paramaters
     */
    public static Optional<Method> getMethod(Class<?> clazz, String methodName) {
        if (!loadedMethods.containsKey(clazz)) {
            loadedMethods.put(clazz, new HashMap<String, Method>());
        }

        Map<String, Method> methods = loadedMethods.get(clazz);

        if (methods.containsKey(methodName)) {
            return Optional.ofNullable(methods.get(methodName));
        }

        try {
            Method method = clazz.getMethod(methodName);
            methods.put(methodName, method);
            loadedMethods.put(clazz, methods);
            return Optional.ofNullable(method);
        } catch (Exception e) {
            e.printStackTrace();
            methods.put(methodName, null);
            loadedMethods.put(clazz, methods);
            return Optional.empty();
        }
    }

    /**
     * Get a method from a class that has the specific paramaters
     *
     * @param clazz      The class we are searching
     * @param methodName The name of the method
     * @param params     Any parameters that the method has
     * @return The method with appropriate paramaters
     */
    public static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        if (!loadedMethods.containsKey(clazz)) {
            loadedMethods.put(clazz, new HashMap<String, Method>());
        }

        Map<String, Method> methods = loadedMethods.get(clazz);

        if (methods.containsKey(methodName)) {
            return Optional.ofNullable(methods.get(methodName));
        }

        try {
            Method method = clazz.getMethod(methodName, params);
            methods.put(methodName, method);
            loadedMethods.put(clazz, methods);
            return Optional.ofNullable(method);
        } catch (Exception e) {
            e.printStackTrace();
            methods.put(methodName, null);
            loadedMethods.put(clazz, methods);
            return Optional.empty();
        }
    }

    /**
     * Get a field with a particular name from a class
     *
     * @param clazz     The class
     * @param fieldName The name of the field
     * @return The field object
     */
    public static Optional<Field> getField(Class<?> clazz, String fieldName) {
        if (!loadedFields.containsKey(clazz)) {
            loadedFields.put(clazz, new HashMap<String, Field>());
        }

        Map<String, Field> fields = loadedFields.get(clazz);

        if (fields.containsKey(fieldName)) {
            return Optional.ofNullable(fields.get(fieldName));
        }

        try {
            Field field = clazz.getField(fieldName);
            fields.put(fieldName, field);
            loadedFields.put(clazz, fields);
            return Optional.ofNullable(field);
        } catch (Exception e) {
            e.printStackTrace();
            fields.put(fieldName, null);
            loadedFields.put(clazz, fields);
            return Optional.empty();
        }
    }

    /*
   copied from HNMCUtils
    */
    public static String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Optional<Method> asNMSCopyMethodOpt = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Optional<Method> saveNmsItemStackMethodOpt = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method


        try {
            Method asNMSCopyMethod = asNMSCopyMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 CraftItemStack 的 asNMSCopy 方法"));
            Method saveNmsItemStackMethod = saveNmsItemStackMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 ItemStack 的 save 方法"));
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.getConstructor().newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "ItemStack 轉換json失敗", t);
            return null;
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }

    public static ItemStack deserializeItemStackFromNBTJson(String json) {
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound").orElseThrow(() -> new IllegalStateException("錯誤訊息"));
        Class<?> MojangsonParserClazz = ReflectionUtil.getNMSClass("MojangsonParser").orElseThrow(() -> new IllegalStateException("錯誤訊息"));

        Optional<Method> asCraftMirrorMethodOpt = ReflectionUtil.getMethod(craftItemStackClazz, "asCraftMirror", nmsItemStackClazz);

        Optional<Method> aMethodOpt = ReflectionUtil.getMethod(nmsItemStackClazz, "a", nbtTagCompoundClazz);

        Optional<Method> parseMethodOpt = ReflectionUtil.getMethod(MojangsonParserClazz, "parse", String.class);

        Object nmsItemStackObj;
        Object craftItemStackObj;
        Object MojangsonParserObj;

        try {
            Method asCraftMirrorMethod = asCraftMirrorMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 CraftItemStack 的 asCraftMirror 方法"));
            Method aMethodOptMethod = aMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 ItemStack 的 a 方法"));
            Method parseMethodOptMethod = parseMethodOpt.orElseThrow(() -> new NoSuchElementException("找不到 MojangsonParser 的 parse 方法"));
            MojangsonParserObj = parseMethodOptMethod.invoke(null, json);
            nmsItemStackObj = aMethodOptMethod.invoke(null, MojangsonParserObj);
            craftItemStackObj = asCraftMirrorMethod.invoke(null, nmsItemStackObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "json 轉換 ItemStack 失敗", t);
            return null;
        }
        return (ItemStack) craftItemStackObj;
    }
}
