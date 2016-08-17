package me.ialistannen.bukkitutil.commandsystem;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin main
 */
@SuppressWarnings("unused")
public class PluginMain extends JavaPlugin {

	private static PluginMain instance;

	@Override
	public void onEnable() {
		instance = this;
	}

	@Override
	public void onDisable() {
		// prevent the old instance from still being around.
		instance = null;
	}

	/**
	 * Returns the plugins instance
	 *
	 * @return The plugin instance
	 */
	public static PluginMain getInstance() {
		return instance;
	}
}
