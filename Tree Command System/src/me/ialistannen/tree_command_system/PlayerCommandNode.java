package me.ialistannen.tree_command_system;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A command a player needs to execute
 */
public abstract class PlayerCommandNode extends CommandNode {
	
	/**
	 * @param name The name of the command
	 * @param keyword The keyword the command has. Should match the pattern, but is the one you hope to get :P
	 * @param pattern The pattern which keywords must match.
	 * @param permission The permission for the command. Empty for everybody
	 */
	public PlayerCommandNode(String name, String keyword, Pattern pattern, String permission) {
		super(name, keyword, pattern, permission, (sender) -> sender instanceof Player);
	}

	@Override
	public abstract String getUsage();

	@Override
	public abstract String getDescription();

	@Override
	protected final List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender sender) {
		// should never face this, but you can never be too secure
		if(!(sender instanceof Player)) {
			return Collections.emptyList();
		}
		return getTabCompletions(input, wholeUserChat, (Player) sender);
	}

	/**
	 * The tab completions for this particular command
	 * 
	 * @param input The current word the user wrote. May be emtpy
	 * @param wholeUserChat All the words the user wrote.
	 * @param player The {@link Player} who initiated the tab complete. May be queried for permission checks or similar.
	 * @return A list with all valid tab completions
	 */
	protected abstract List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player);

	@Override
	protected final boolean execute(CommandSender sender, String... args) {
		// should never face this, but you can never be too secure
		if(!(sender instanceof Player)) {
			return true;
		}
		else {
			return execute((Player) sender, args);
		}
	}
	
	/**
	 * @param player The player who executed the command
	 * @param args The arguments he passed
	 * @return False if the usage should be send
	 */
	protected abstract boolean execute(Player player, String... args);

}
