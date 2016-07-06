package me.ialistannen.tree_command_system;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * A command only the console can execute
 */
public abstract class ConsoleCommandNode extends CommandNode {

	/**
	 * @param name The name of the command
	 * @param keyword The keyword the command has. Should match the pattern, but is the one you hope to get :P
	 * @param pattern The pattern which keywords must match.
	 * @param permission The permission for the command. Empty for everybody
	 */
	public ConsoleCommandNode(String name, String keyword, Pattern pattern, String permission) {
		super(name, keyword, pattern, permission, (sender) -> sender instanceof ConsoleCommandSender);
	}

	@Override
	public abstract String getUsage();

	@Override
	public abstract String getDescription();

	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		// should never face this, but you can never be too secure
		if(!(tabCompleter instanceof ConsoleCommandSender)) {
			return Collections.emptyList();
		}
		
		return getTabCompletions(input, wholeUserChat, (ConsoleCommandSender) tabCompleter);
	}
	
	/**
	 * The tab completions for this particular command
	 * 
	 * @param input The current word the user wrote. May be emtpy
	 * @param wholeUserChat All the words the user wrote.
	 * @param tabCompleter The {@link ConsoleCommandSender} who initiated the tab complete. May be queried for permission checks or similar.
	 * @return A list with all valid tab completions
	 */
	protected abstract List<String> getTabCompletions(String input, List<String> wholeUserChat, ConsoleCommandSender tabCompleter);

	@Override
	protected boolean execute(CommandSender sender, String... args) {
		// should never face this, but you can never be too secure
		if(!(sender instanceof ConsoleCommandSender)) {
			return true;
		}
		
		return execute((ConsoleCommandSender) sender, args);
	}

	/**
	 * Executes the command. The arguments won't contain your keyword.
	 * 
	 * @param sender The {@link ConsoleCommandSender} who executed the command
	 * @param args The arguments he passed
	 * @return False if the usage should be send.
	 */
	protected abstract boolean execute(ConsoleCommandSender sender, String... args);

}
