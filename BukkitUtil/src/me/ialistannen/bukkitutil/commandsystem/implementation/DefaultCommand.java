package me.ialistannen.bukkitutil.commandsystem.implementation;


import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * A default command node. Provides an implementation for most base methods.
 * <p>
 * <b>isAllowed:</b>
 * <br>Checks the permission
 * <p>
 * <b>isAble:</b>
 * <br>Checks the passed predicate
 */
public abstract class DefaultCommand extends AbstractCommandNode {

	@SuppressWarnings("WeakerAccess") // This could be interesting though
	protected final String permission;
	// there is a checker for this.
	private final Predicate<CommandSender> canUse;

	/**
	 * Constructs a command.
	 *
	 * @param language        The language to use
	 * @param baseKey         The base key for the language. Should be unique, is used by equals and hashcode.
	 * @param permission      The permission the sender needs
	 * @param senderPredicate The predicate the CommandSender must match
	 */
	@SuppressWarnings("unused")
	public DefaultCommand(@Nonnull MessageProvider language, @Nonnull String baseKey,
	                      String permission, Predicate<CommandSender> senderPredicate) {

		super(language, baseKey);

		this.permission = permission;
		this.canUse = senderPredicate;
	}

	/**
	 * Constructs a command.
	 * <br>The Base key will be the simple name of the class.
	 *
	 * @param language The language to use
	 *
	 * @see #DefaultCommand(MessageProvider, String, String, Predicate
	 */
	@SuppressWarnings("unused")
	public DefaultCommand(@Nonnull MessageProvider language, String permission,
	                      Predicate<CommandSender> senderPredicate) {
		super(language);

		this.permission = permission;
		this.canUse = senderPredicate;
	}


	@Override
	public boolean isForbidden(Permissible permissible) {
		// empty permission means no restriction
		return !permission.isEmpty() && !permissible.hasPermission(permission);
	}

	@Override
	public boolean isNotAble(CommandSender sender) {
		return !canUse.test(sender);
	}
}
