package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

/**
 * The default TabCompleter
 */
@SuppressWarnings("WeakerAccess") // It said the class can be package-private. I disagree.
public class DefaultTabCompleter implements TabCompleter {

	private final CommandTree tree;

	private boolean assumeCommandIsPartOfTree;

	/**
	 *
	 * @param tree The {@link CommandTree}
	 *
	 * @see #DefaultTabCompleter(CommandTree, boolean) with true as the boolean
	 */
	@SuppressWarnings("unused")
	public DefaultTabCompleter(CommandTree tree) {
		this(tree, true);
	}

	/**
	 * @param tree                      The {@link CommandTree}
	 * @param assumeCommandIsPartOfTree If true, the command's name will be treated as the first argument.
	 */
	@SuppressWarnings("unused")
	public DefaultTabCompleter(CommandTree tree, boolean assumeCommandIsPartOfTree) {
		this.tree = tree;
		this.assumeCommandIsPartOfTree = assumeCommandIsPartOfTree;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		String[] arguments = args;
		if (assumeCommandIsPartOfTree) {
			arguments = new String[args.length + 1];
			System.arraycopy(args, 0, arguments, 1, args.length);
			arguments[0] = command.getName();
		}

		AbstractCommandNode.FindTabCompleteResult result = tree.doTabComplete(sender, alias, arguments);
		if (result.getResult() == CommandResultType.SUCCESSFUL) {
			return result.getResultList();
		}
		return Collections.emptyList();
	}
}
