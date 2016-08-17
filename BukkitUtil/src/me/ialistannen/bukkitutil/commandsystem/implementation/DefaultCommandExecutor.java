package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.PluginMain;
import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResult;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * A {@link CommandExecutor} implementation for the command system
 */
public class DefaultCommandExecutor implements CommandExecutor {

	private final CommandTree tree;
	private final MessageProvider language;

	/**
	 * @param tree     The CommandTree
	 * @param language Lhe Language system
	 */
	@SuppressWarnings("unused")
	public DefaultCommandExecutor(CommandTree tree, MessageProvider language) {
		this.tree = tree;
		this.language = language;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		CommandResult commandResult = tree.executeCommand(sender, args);
		switch (commandResult.getResultType()) {
			case SUCCESSFUL:
				break;
			case SEND_USAGE:
				Optional<AbstractCommandNode> node = commandResult.getCommandNode();
				if (!node.isPresent()) {
					PluginMain.getInstance().getLogger().warning("How the heck can this happen?" +
							" Send usage, but no command defined.");
					sender.sendMessage(language.tr("not_found"));
					return true;
				}
				sender.sendMessage(node.get().getUsage());
				break;
			case PERMISSION_DENIED:
				sender.sendMessage(language.tr("permission_denied"));
				break;
			case WRONG_SENDER:
				sender.sendMessage(language.tr("wrong_sender_type"));
				break;
			case NOT_FOUND:
				sender.sendMessage(language.tr("not_found"));
				break;
		}
		return true;
	}
}
