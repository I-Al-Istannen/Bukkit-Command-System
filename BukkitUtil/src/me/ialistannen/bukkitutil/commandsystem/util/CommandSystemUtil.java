package me.ialistannen.bukkitutil.commandsystem.util;

import me.ialistannen.bukkitutil.commandsystem.PluginMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.meta.When;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Some static utility classes
 */
public class CommandSystemUtil {

	/**
	 * Colors the input
	 *
	 * @param string The Input
	 *
	 * @return The colored string
	 */
	@Nonnull
	public static String color(@Nonnull(when = When.ALWAYS) String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	/**
	 * Repeats the string amount times
	 *
	 * @param string The string to repeat
	 * @param amount The amount to repeat it for
	 *
	 * @return The repeated String
	 */
	@SuppressWarnings("SameParameterValue") // May not always want to use " " in the future. This is probably cleaner.
	public static String repeat(String string, int amount) {
		return IntStream.range(0, amount).mapToObj(value -> string).collect(Collectors.joining());
	}


	/**
	 * Registers a command at runtime
	 *
	 * @param plugin    The owning plugin
	 * @param name      The name of the command
	 * @param executor  The executor for the command
	 * @param completer The TabCompleter for the command
	 *
	 * @return True if it was registered
	 */
	public static boolean registerCommand(Plugin plugin, String name, CommandExecutor executor, TabCompleter
			completer) {

		PluginCommand command = getCommand(name, plugin);

		if (command == null) {
			return false;
		}

		command.setExecutor(executor);
		command.setTabCompleter(completer);

		CommandMap map = getCommandMap();

		return map != null && map.register(plugin.getName(), command);

	}

	/**
	 * Unregisters a command at runtime
	 *
	 * @param plugin The owning plugin
	 * @param name   The name of the command
	 *
	 * @return True if it was unregistered
	 */
	public static boolean unregisterCommand(Plugin plugin, String name) {
		Command command;

		SimpleCommandMap map = (SimpleCommandMap) getCommandMap();

		if (map == null) {
			return false;
		}

		@SuppressWarnings("unchecked")
		Map<String, Command> commands = (Map<String, Command>) ReflectionUtil.getInstanceField(map, "knownCommands");


		if (commands == null) {
			PluginMain.getInstance().getLogger().log(Level.WARNING, "Can't get known commands map.");
			return false;
		}

		command = commands.remove(plugin.getName().toLowerCase().trim() + ":" + name.toLowerCase().trim());
		if (command == null) {
			command = commands.remove(name.toLowerCase().trim());
		} else {
			commands.remove(name.toLowerCase().trim());
		}

		return command != null && command.unregister(map);
	}


	/**
	 * Gets the used {@link CommandMap}
	 *
	 * @return The {@link CommandMap}
	 */
	private static
	@Nullable
	CommandMap getCommandMap() {
		CommandMap commandMap = (CommandMap) ReflectionUtil.getInstanceField(Bukkit.getServer(), "commandMap");
		if (commandMap == null) {
			PluginMain.getInstance().getLogger().log(Level.WARNING, "Can't get server command map.");
		}
		return commandMap;
	}

	/**
	 * Gets a plugin command
	 *
	 * @param name   The name of the command
	 * @param plugin The owning plugin
	 *
	 * @return The {@link PluginCommand}
	 */
	private static
	@Nullable
	PluginCommand getCommand(String name, Plugin plugin) {
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class
					.getDeclaredConstructor(String.class, Plugin.class);

			constructor.setAccessible(true);

			return constructor.newInstance(name, plugin);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
				e) {
			PluginMain.getInstance().getLogger().log(Level.WARNING, "Can't get plugin command.", e);
		}

		return null;
	}

}
