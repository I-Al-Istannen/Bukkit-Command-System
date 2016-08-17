package me.ialistannen.bukkitutil.commandsystem.base;

/**
 * The keys for information about a command
 */
public enum CommandInformationKey {
	/**
	 * The usage of a command
	 */
	USAGE("usage"),
	/**
	 * The description of a command
	 */
	DESCRIPTION("description"),
	/**
	 * The name of the command
	 */
	NAME("name"),
	/**
	 * The keyword of the command, to insert on tab complete
	 */
	KEYWORD("keyword"),
	/**
	 * The pattern to match, in oder for the command to be recognized
	 */
	PATTERN("pattern");

	private final String key;

	/**
	 * Creates this enum entry and sets the key
	 *
	 * @param key The key
	 */
	@SuppressWarnings("unused")
	CommandInformationKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the key
	 *
	 * @return The key for the information
	 */
	private String getKey() {
		return key;
	}

	/**
	 * Applies the key to the base key (separation by '_')
	 *
	 * @param baseKey The base key
	 *
	 * @return The combined key
	 */
	public String applyTo(String baseKey) {
		return baseKey + "_" + getKey();
	}
}
