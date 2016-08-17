package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A relay command node. Provides an implementation for base methods.
 * <p>Inherits from {@link DefaultCommand}.
 * <p>Additional methods:
 * <p>
 * <b>Execute:</b>
 * <br>{@link CommandResultType#SEND_USAGE}
 * <p>
 * <b>TabComplete:</b>
 * <br>Children
 */
public class RelayCommandNode extends DefaultCommand {

	/**
	 * Constructs a command.
	 *
	 * @param language        The language to use
	 * @param baseKey         The base key for the language. Should be unique, is used by equals and hashcode.
	 * @param permission      The permission the sender needs
	 * @param senderPredicate The predicate the CommandSender must match
	 */
	@SuppressWarnings("unused")
	public RelayCommandNode(@Nonnull MessageProvider language, @Nonnull String baseKey,
	                        String permission, Predicate<CommandSender> senderPredicate) {

		super(language, baseKey, permission, senderPredicate);
	}

	/**
	 * Constructs a command.
	 * <br>The Base key will be the simple name of the class.
	 *
	 * @param language The language to use
	 *
	 * @see #RelayCommandNode(MessageProvider, String, String, Predicate)
	 */
	@SuppressWarnings("unused")
	public RelayCommandNode(@Nonnull MessageProvider language, String permission,
	                        Predicate<CommandSender> senderPredicate) {
		super(language, permission, senderPredicate);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return getChildren().stream()
				.filter(child -> !child.isNotAble(sender) && !child.isForbidden(sender))
				.map(AbstractCommandNode::getKeyword).collect(Collectors.toList());
	}

	@Override
	public CommandResultType execute(CommandSender sender, String[] args) {
		return CommandResultType.SEND_USAGE;
	}
}
