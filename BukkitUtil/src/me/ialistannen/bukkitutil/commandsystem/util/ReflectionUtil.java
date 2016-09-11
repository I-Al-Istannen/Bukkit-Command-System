package me.ialistannen.bukkitutil.commandsystem.util;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A small help with reflection
 */
public class ReflectionUtil {

	private static final String SERVER_VERSION;

	static {
		String name = Bukkit.getServer().getClass().getName();
		name = name.substring(name.indexOf("craftbukkit.") + "craftbukkit.".length());
		name = name.substring(0, name.indexOf("."));

		SERVER_VERSION = name;
	}

	/**
	 * Returns the major version of the server
	 *
	 * @return The major version of the server
	 */
	public static int getMajorVersion() {
		String name = Bukkit.getVersion();

		name = name.substring(name.indexOf("MC: ") + "MC: ".length());
		name = name.replace(")", "");

		return Integer.parseInt(name.split("\\.")[0]);
	}

	/**
	 * Returns the minor version of the server
	 *
	 * @return The minor version of the server
	 */
	public static int getMinorVersion() {
		String name = Bukkit.getVersion();
		name = name.substring(name.indexOf("MC: ") + "MC: ".length());
		name = name.replace(")", "");

		return Integer.parseInt(name.split("\\.")[1]);
	}

	/**
	 * Returns the patch version of the server
	 *
	 * @return The patch version of the server
	 */
	public static int getPatchVersion() {
		String name = Bukkit.getVersion();
		name = name.substring(name.indexOf("MC: ") + "MC: ".length());
		name = name.replace(")", "");

		String[] splitted = name.split("\\.");
		if (splitted.length < 3) {
			return 0;
		}

		return Integer.parseInt(splitted[2]);
	}

	/**
	 * Returns the NMS class.
	 *
	 * @param name The name of the class
	 *
	 * @return The NMS class or null if an error occurred
	 */
	@SuppressWarnings("unused")
	@Nullable
	public static Class<?> getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + SERVER_VERSION + "." + name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Returns the CraftBukkit class.
	 *
	 * @param name The name of the class
	 *
	 * @return The CraftBukkit class or null if an error occurred
	 */
	@Nullable
	public static Class<?> getCraftbukkitClass(String name, String packageName) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + "." + packageName + "." + name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Invokes the method
	 *
	 * @param handle           The handle to invoke it on
	 * @param methodName       The name of the method
	 * @param parameterClasses The parameter types
	 * @param args             The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	@SuppressWarnings("unused")
	@Nullable
	public static Object invokeMethod(Object handle, String methodName, Class[] parameterClasses, Object... args) {
		return invokeMethod(handle.getClass(), handle, methodName, parameterClasses, args);
	}

	/**
	 * Invokes the method
	 *
	 * @param clazz            The class to invoke it from
	 * @param handle           The handle to invoke it on
	 * @param methodName       The name of the method
	 * @param parameterClasses The parameter types
	 * @param args             The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	@Nullable
	public static Object invokeMethod(Class<?> clazz, Object handle, String methodName, Class[] parameterClasses,
	                                  Object... args) {
		Optional<Method> methodOptional = getMethod(clazz, methodName, parameterClasses);

		if (!methodOptional.isPresent()) {
			return null;
		}

		Method method = methodOptional.get();

		try {
			return method.invoke(handle, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Invokes the method
	 *
	 * @param method The method to invoke
	 * @param handle The handle to invoke on
	 * @param args   The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 */
	public static Object invokeMethod(Method method, Object handle, Object... args) {
		try {
			return method.invoke(handle, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Invokes the method
	 *
	 * @param rethrowMethodErrors If true, any exceptions by the method invoked will be propagated up
	 * @param method              The method to invoke
	 * @param handle              The handle to invoke on
	 * @param args                The arguments
	 *
	 * @return The resulting object or null if an error occurred / the method didn't return a thing
	 *
	 * @throws InvocationTargetException if an exception is thrown by the method
	 */
	public static Object invokeMethod(boolean rethrowMethodErrors, Method method, Object handle, Object... args)
			throws InvocationTargetException {
		try {
			return method.invoke(handle, args);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (rethrowMethodErrors) {
				throw e;
			}
			else {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * Sets the value of an instance field
	 *
	 * @param handle The handle to invoke it on
	 * @param name   The name of the field
	 * @param value  The new value of the field
	 */
	@SuppressWarnings("unused")
	public static void setInstanceField(Object handle, String name, Object value) {
		Class<?> clazz = handle.getClass();
		Optional<Field> fieldOptional = getField(clazz, name);
		if (!fieldOptional.isPresent()) {
			return;
		}

		Field field = fieldOptional.get();
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			field.set(handle, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the value of an instance field
	 *
	 * @param handle The handle to invoke it on
	 * @param name   The name of the field
	 *
	 * @return The value
	 */
	@SuppressWarnings("unused")
	@Nullable
	public static Object getInstanceField(Object handle, String name) {
		Class<?> clazz = handle.getClass();
		Optional<Field> fieldOptional = getField(clazz, name);
		if (!fieldOptional.isPresent()) {
			return null;
		}

		Field field = fieldOptional.get();
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			return field.get(handle);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the constructor
	 *
	 * @param clazz  The class
	 * @param params The Constructor parameters
	 *
	 * @return The Constructor or null if there is none with these parameters
	 */
	@SuppressWarnings("unused")
	@Nullable
	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
		try {
			return clazz.getConstructor(params);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Instantiates the class. Will print the errors it gets
	 *
	 * @param constructor The constructor
	 * @param arguments   The initial arguments
	 *
	 * @return The resulting object, or null if an error occurred.
	 */
	@SuppressWarnings("unused")
	@Nullable
	public static Object instantiate(Constructor<?> constructor, Object... arguments) {
		try {
			return constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			System.out.println("Can't instantiate class " + constructor.getDeclaringClass());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param clazz  The class to get the method for
	 * @param name   The name of the method
	 * @param params The parameters of the method
	 *
	 * @return The method, if any
	 */
	public static Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... params) {
		try {
			return Optional.of(clazz.getMethod(name, params));
		} catch (NoSuchMethodException ignored) {
		}

		try {
			return Optional.of(clazz.getDeclaredMethod(name, params));
		} catch (NoSuchMethodException ignored) {
		}

		return Optional.empty();
	}

	private static Optional<Field> getField(Class<?> clazz, String name) {
		try {
			return Optional.of(clazz.getField(name));
		} catch (NoSuchFieldException ignored) {
		}

		try {
			return Optional.of(clazz.getDeclaredField(name));
		} catch (NoSuchFieldException ignored) {
		}

		return Optional.empty();
	}

}
