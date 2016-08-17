package me.ialistannen.bukkitutil.commandsystem.base;

/**
 * The different command results
 */
@SuppressWarnings("unused")
public enum CommandResultType {
	/**
	 * If the command was executed successfully
	 */
	SUCCESSFUL,
	/**
	 * If the usage should be send
	 */
	SEND_USAGE,
	/**
	 * If the permission was denied
	 */
	PERMISSION_DENIED,
	/**
	 * If it was the wrong sender
	 */
	WRONG_SENDER,
	/**
	 * If the command was not found
	 */
	NOT_FOUND
}
