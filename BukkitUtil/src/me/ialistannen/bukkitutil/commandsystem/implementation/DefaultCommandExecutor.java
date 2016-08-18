package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.PluginMain;
import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResult;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link CommandExecutor} implementation for the command system
 * <p>
 * <b>Language Keys:</b>
 * <ul>
 * <li>"command_not_found" ==> if a command was not found</li>
 * <li>"permission_denied"</li>
 * <li>"wrong_sender_type" ==> If the {@link CommandSender} is of the wrong type</li>
 * </ul>
 */
@SuppressWarnings("WeakerAccess")
public class DefaultCommandExecutor implements CommandExecutor {

	private final CommandTree tree;
	private final MessageProvider language;

	private boolean assumeCommandIsPartOfTree;


	/**
	 * @param tree     The CommandTree
	 * @param language Lhe Language system
	 *
	 * @see #DefaultCommandExecutor(CommandTree, MessageProvider, boolean) with true as the boolean
	 */
	@SuppressWarnings("unused")
	public DefaultCommandExecutor(CommandTree tree, MessageProvider language) {
		this(tree, language, true);
	}

	/**
	 * @param tree                      The CommandTree
	 * @param language                  Lhe Language system
	 * @param assumeCommandIsPartOfTree If true, the command's name will be treated as the first argument.
	 */
	@SuppressWarnings("unused")
	public DefaultCommandExecutor(CommandTree tree, MessageProvider language, boolean assumeCommandIsPartOfTree) {
		this.tree = tree;
		this.language = language;
		this.assumeCommandIsPartOfTree = assumeCommandIsPartOfTree;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String[] arguments = args;
		if (assumeCommandIsPartOfTree) {
			arguments = new String[args.length + 1];
			System.arraycopy(args, 0, arguments, 1, args.length);
			arguments[0] = command.getName();
		}

		CommandResult commandResult = tree.executeCommand(sender, arguments);
		switch (commandResult.getResultType()) {
			case SUCCESSFUL:
				break;
			case SEND_USAGE:
				Optional<AbstractCommandNode> node = commandResult.getCommandNode();
				if (!node.isPresent()) {
					PluginMain.getInstance().getLogger().warning("How the heck can this happen?" +
							" Send usage, but no command defined.");
					sender.sendMessage(language.tr("command_not_found",
							Arrays.stream(arguments).collect(Collectors.joining(" "))));
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
				sender.sendMessage(language.tr("command_not_found",
						Arrays.stream(arguments).collect(Collectors.joining(" "))));
				break;
		}
		return true;
	}
}
