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

	/**
	 * @param tree The {@link CommandTree}
	 */
	@SuppressWarnings("unused")
	public DefaultTabCompleter(CommandTree tree) {
		this.tree = tree;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		AbstractCommandNode.FindTabCompleteResult result = tree.doTabComplete(sender, alias, args);
		if (result.getResult() == CommandResultType.SUCCESSFUL) {
			return result.getResultList();
		}
		return Collections.emptyList();
	}
}
