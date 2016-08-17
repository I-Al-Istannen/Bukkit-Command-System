package me.ialistannen.bukkitutil.commandsystem.base;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 * Restricts the access to a command
 */
public interface BukkitAccessRestrictable {

	/**
	 * Checks if the {@link Permissible} has sufficient permission to execute the command
	 *
	 * @param permissible The permissible to check
	 *
	 * @return True if they can use the command
	 */
	boolean isForbidden(Permissible permissible);

	/**
	 * Checks whether the {@link CommandSender} can use this command (Console - Player - Blocks)
	 *
	 * @param sender The sender of the command
	 *
	 * @return True if the sender can use it
	 */
	boolean isNotAble(CommandSender sender);
}
