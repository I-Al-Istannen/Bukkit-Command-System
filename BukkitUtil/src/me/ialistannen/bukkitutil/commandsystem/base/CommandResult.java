package me.ialistannen.bukkitutil.commandsystem.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The result of a command execution
 */
public class CommandResult {

	private final AbstractCommandNode commandNode;
	private final CommandResultType resultType;

	/**
	 * @param commandNode The command node that ended up executing
	 * @param resultType  The result of the execution
	 */
	public CommandResult(@Nullable AbstractCommandNode commandNode, @Nonnull CommandResultType resultType) {
		this.commandNode = commandNode;
		this.resultType = resultType;
	}

	/**
	 * The result of the execution
	 *
	 * @return The {@link CommandResultType}
	 */
	public CommandResultType getResultType() {
		return resultType;
	}

	/**
	 * The command node that ended up executing
	 *
	 * @return The {@link AbstractCommandNode} that ended up executing
	 */
	public Optional<AbstractCommandNode> getCommandNode() {
		return Optional.ofNullable(commandNode);
	}
}
