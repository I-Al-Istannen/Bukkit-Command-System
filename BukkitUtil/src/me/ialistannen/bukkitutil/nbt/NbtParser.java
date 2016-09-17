package me.ialistannen.bukkitutil.nbt;

import me.ialistannen.bukkitutil.commandsystem.PluginMain;
import me.ialistannen.bukkitutil.commandsystem.util.ReflectionUtil;
import me.ialistannen.bukkitutil.nbt.NBTWrappers.INBTBase;
import me.ialistannen.bukkitutil.nbt.NBTWrappers.NBTTagCompound;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A wrapper for the MojangsonParser used for parsing NBT
 */
@SuppressWarnings("unused")
public class NbtParser {

	private static Method PARSE_METHOD;
	private static boolean error = false;

	static {
		Class<?> mojangsonParserClass = ReflectionUtil.getNMSClass("MojangsonParser");

		if (mojangsonParserClass == null) {
			PluginMain.getInstance().getLogger()
					.warning("Can't find the class MojangsonParser: "
							+ Bukkit.getServer().getClass().getName());
			error = true;
		}
		else {

			Optional<Method> parseMethodOpt = ReflectionUtil.getMethod(mojangsonParserClass, "parse", String.class);
			if (parseMethodOpt.isPresent()) {
				PARSE_METHOD = parseMethodOpt.get();
			}
			else {
				PluginMain.getInstance().getLogger()
						.warning("Can't find MojangsonParser's parse method: "
								+ mojangsonParserClass.getName());
				error = true;
			}
		}
	}

	/**
	 * @throws IllegalStateException If {@link #error} is true
	 */
	private static void ensureNoError() {
		if (error) {
			throw new IllegalStateException("A critical, non recoverable error occurred earlier.");
		}
	}

	/**
	 * Parses a String to an {@link NBTTagCompound}
	 *
	 * @param nbt The nbt to parse
	 *
	 * @return The parsed NBTTagCompound
	 *
	 * @throws NbtParseException if an error occurred while parsing the NBT tag
	 */
	public static NBTTagCompound parse(String nbt) throws NbtParseException {
		ensureNoError();

		try {
			Object resultingNBT = ReflectionUtil.invokeMethod(true, PARSE_METHOD, null, nbt);

			// is defined by the method and the only one making sense
			return (NBTTagCompound) INBTBase.fromNBT(resultingNBT);
		} catch (InvocationTargetException e) {
			throw new NbtParseException(e.getCause().getMessage(), e.getCause());
		}
	}

	/**
	 * An exception occurred while parsing a NBT tag. Checked.
	 */
	@SuppressWarnings("WeakerAccess") // other classes outside need to access it.
	public static class NbtParseException extends Exception {

		/**
		 * Constructs a new exception with the specified detail message and
		 * cause.  <p>Note that the detail message associated with
		 * {@code cause} is <i>not</i> automatically incorporated in
		 * this exception's detail message.
		 *
		 * @param message the detail message (which is saved for later retrieval
		 *                by the {@link #getMessage()} method).
		 * @param cause   the cause (which is saved for later retrieval by the
		 *                {@link #getCause()} method).  (A <tt>null</tt> value is
		 *                permitted, and indicates that the cause is nonexistent or
		 *                unknown.)
		 *
		 * @since 1.4
		 */
		private NbtParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
