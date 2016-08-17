package me.ialistannen.bukkitutil.commandsystem.base;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Methods for a bukkit command
 */
public interface BukkitCommand {

	/**
	 * Tab-Completes the command
	 *
	 * @param sender             The {@link CommandSender} who tab-completed
	 * @param alias              The alias used
	 * @param wholeUserChat      Everything he wrote
	 * @param indexRelativeToYou The index of the argument he completed, relative to you.
	 *
	 * @return A list with valid completions. Empty for none, null for all online, visible players
	 */
	@SuppressWarnings("UnusedParameters")
	List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat, int indexRelativeToYou);

	/**
	 * Called when a user executes a command
	 *
	 * @param sender The sender of the command
	 * @param args   The arguments of the command
	 *
	 * @return False if the usage should be send
	 */
	CommandResultType execute(CommandSender sender, String[] args);
}
