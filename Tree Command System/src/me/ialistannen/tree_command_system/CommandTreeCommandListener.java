package me.ialistannen.tree_command_system;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ialistannen.languageSystem.MessageProvider;

/**
 * Listens for commands and tries to process them.
 */
public class CommandTreeCommandListener implements CommandExecutor {
	
	private CommandTreeManager tree;
	private MessageProvider messageProvider;
	
	/**
	 * @param tree The command tree to use
	 * @param messageProvider The message provider. Must return answers for the keys "no help node" and "no permission"
	 */
	public CommandTreeCommandListener(CommandTreeManager tree, MessageProvider messageProvider) {
		this.tree = tree;
		this.messageProvider = messageProvider;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			executeHelp(sender, args);
			return true;
		}
		
		if(tree.nodeExists(args)) {
			if(tree.hasPermission(sender, args)) {
				tree.execute(sender, args);
			}
			else {
				sendNoPermission(sender);
			}
		}
		else {
			executeHelp(sender, args);
		}
		return true;
	}
	
	private void executeHelp(CommandSender sender, String[] args) {
		Optional<CommandNode> helpNode = tree.getHelpNode();
		if(helpNode.isPresent()) {
			if(helpNode.get().hasPermission(sender)) {
				helpNode.get().executeCommand(sender, args);
			}
			else {
				sendNoPermission(sender);
			}
		}
		else {
			sender.sendMessage(TreeCommandSystemUtil.color(messageProvider.tr("no help node")));
		}
	}

	private void sendNoPermission(CommandSender sender) {
		sender.sendMessage(TreeCommandSystemUtil.color(messageProvider.tr("no permission")));		
	}
	
}
