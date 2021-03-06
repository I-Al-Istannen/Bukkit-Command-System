package me.ialistannen.bukkitutil.commandsystem.base;

import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultHelpCommand;
import me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * The tree
 */
public class CommandTree {

	private final CommandRoot root;

	private final List<InvalidationListener> invalidationListener = new ArrayList<>();
	private MessageProvider language;

	/**
	 * Key for the base root is "tree_root", but it shouldn't be needed!
	 *
	 * @param language The language.
	 */
	@SuppressWarnings("unused")
	public CommandTree(MessageProvider language) {
		root = new CommandRoot(language);
		this.language = language;
	}

	/**
	 * Sets the help command
	 *
	 * @param node The help command node. Annotated with {@link HelpCommandAnnotation}
	 */
	@SuppressWarnings("WeakerAccess")
	public void setHelpCommand(AbstractCommandNode node, AbstractCommandNode parent) {
		if (!node.getClass().isAnnotationPresent(HelpCommandAnnotation.class)) {
			throw new IllegalArgumentException("The help node must be annotated with the 'HelpCommandAnnotation'");
		}
		addChild(parent, node);
	}

	/**
	 * Adds an invalidation listener
	 *
	 * @param invalidationListener The invalidationListener to add
	 *
	 * @return The index it was added at
	 */
	// THIS is borderline useless. Maybe for invalidation caches, intended it for the pager originally
	@SuppressWarnings("unused")
	public int addInvalidationListener(InvalidationListener invalidationListener) {
		this.invalidationListener.add(invalidationListener);
		return this.invalidationListener.size() - 1;
	}

	/**
	 * Removes a listener
	 *
	 * @param index The index to remove
	 *
	 * @throws IndexOutOfBoundsException If the index is < 0 or >= the size of the List
	 */
	// THIS is borderline useless. Maybe for invalidation caches, intended it for the pager originally
	@SuppressWarnings("unused")
	public void removeInvalidationListener(int index) {
		if (index < 0 || index >= invalidationListener.size()) {
			String operation = index < 0 ? " < 0" : " >= " + invalidationListener.size();
			throw new IndexOutOfBoundsException("Index " + index + operation);
		}

		this.invalidationListener.remove(index);
	}

	/**
	 * Removes a listener
	 *
	 * @param listener The listener to remove (<b>Warning</b>, {@link InvalidationListener} doesn't implement equals)
	 */
	// THIS is borderline useless. Maybe for invalidation caches, intended it for the pager originally
	@SuppressWarnings("unused")
	public void removeInvalidationListener(InvalidationListener listener) {
		this.invalidationListener.remove(listener);
	}

	/**
	 * Adds a child node to the root
	 *
	 * @param child The child node to add
	 */
	@SuppressWarnings("WeakerAccess")
	public void addChild(AbstractCommandNode child) {
		addChild(getRoot(), child);
	}

	/**
	 * Adds a child node to a parent node
	 *
	 * @param child The child node to add
	 */
	@SuppressWarnings("WeakerAccess")
	public void addChild(AbstractCommandNode parent, AbstractCommandNode child) {
		onInvalidate(InvalidationReason.CHILD_ADDED, parent, child, true);
		parent.addChild(child);
		onInvalidate(InvalidationReason.CHILD_ADDED, parent, child, false);
	}

	/**
	 * Adds the child and registers the command
	 *
	 * @param child        The child to add
	 * @param attachHelp   If true a {@link DefaultHelpCommand} child will be added. If false, no action is taken.
	 * @param plugin       The plugin to
	 * @param executor     The {@link CommandExecutor} to assign to it
	 * @param tabCompleter The {@link TabCompleter} to assign to it
	 */
	public void addTopLevelChild(AbstractCommandNode child, boolean attachHelp,
	                             Plugin plugin, CommandExecutor executor, TabCompleter tabCompleter) {
		addChild(child);
		CommandSystemUtil.registerCommand(plugin, child.getKeyword(), executor, tabCompleter);
		if (attachHelp) {
			setHelpCommand(new DefaultHelpCommand(language, this, "command_help"), child);
		}
	}

	/**
	 * Adds the child and registers the command
	 *
	 * @param child  The child to remove
	 * @param plugin The plugin to
	 */
	public void removeTopLevelChild(AbstractCommandNode child, Plugin plugin) {
		removeChild(child);
		CommandSystemUtil.unregisterCommand(plugin, child.getKeyword());
	}

	/**
	 * Removes a child node from the root
	 *
	 * @param child The child to remove
	 */
	@SuppressWarnings("unused") // may be actually useless. But maybe you need to unregister a command
	public void removeChild(AbstractCommandNode child) {
		removeChild(getRoot(), child);
	}

	/**
	 * Removes a child node from a parent
	 *
	 * @param child The child to remove
	 */
	@SuppressWarnings("WeakerAccess")
	public void removeChild(AbstractCommandNode parent, AbstractCommandNode child) {
		onInvalidate(InvalidationReason.CHILD_REMOVED, parent, child, true);
		parent.removeChild(child);
		onInvalidate(InvalidationReason.CHILD_REMOVED, parent, child, false);
	}

	/**
	 * Returns all the direct children of the node
	 *
	 * @param node The node to get the children for
	 *
	 * @return ALl the direct children
	 */
	public Set<AbstractCommandNode> getChildren(AbstractCommandNode node) {
		return node.getChildren();
	}

	/**
	 * Returns ALL the children, meaning the children and their children and so on
	 *
	 * @return All nodes further down in the the tree from this one on
	 */
	@SuppressWarnings("unused") // useful for an own help command.
	public List<AbstractCommandNode> getAllChildren() {
		return root.getAllChildren();
	}

	/**
	 * Performs the tab completion by delegating it to a child
	 *
	 * @param sender The {@link CommandSender}
	 * @param alias  The used alias for the command
	 * @param args   The args the user entered
	 *
	 * @return A list with valid completions. Empty for none, null for all online, visible players
	 */
	public AbstractCommandNode.FindTabCompleteResult doTabComplete(@Nonnull CommandSender sender, @Nonnull String
			alias, @Nonnull String[] args) {
		return root.doTabComplete(sender, alias, args);
	}

	/**
	 * Tries to find a command using recursion
	 *
	 * @param args   The arguments to find the command for
	 * @param sender The sender to find it for
	 *
	 * @return The command or an empty optional
	 */
	public AbstractCommandNode.FindCommandResult find(Queue<String> args, CommandSender sender) {
		return root.find(args, sender);
	}

	/**
	 * Executes the command, by finding the responsible node and passing it to him
	 *
	 * @param sender The sender of the command
	 * @param args   The arguments of the command
	 *
	 * @return The CommandResult
	 */
	public CommandResult executeCommand(CommandSender sender, String... args) {
		return root.executeCommand(sender, args);
	}

	/**
	 * Returns the root of the tree
	 *
	 * @return The root.
	 */
	public CommandRoot getRoot() {
		return root;
	}

	/**
	 * Delegates it to the listeners
	 *
	 * @param reason The reason why the tree changed it's structure
	 * @param parent The parent node
	 * @param child  The child node
	 */
	private void onInvalidate(InvalidationReason reason, AbstractCommandNode parent, AbstractCommandNode child,
	                          boolean preInvalidate) {
		for (InvalidationListener listener : invalidationListener) {
			if (preInvalidate) {
				listener.onPreInvalidate(reason, parent, child);
			} else {
				listener.onPostInvalidate(reason, parent, child);
			}
		}
	}

	/**
	 * A listener for a structural change of the tree
	 */
	public interface InvalidationListener {
		/**
		 * It is called BEFORE the change has happened
		 *
		 * @param reason The reason why the tree changed it's structure
		 * @param parent The parent node
		 * @param child  The child node
		 */
		void onPreInvalidate(InvalidationReason reason, AbstractCommandNode parent, AbstractCommandNode child);

		/**
		 * It is called AFTER the change has happened
		 *
		 * @param reason The reason why the tree changed it's structure
		 * @param parent The parent node
		 * @param child  The child node
		 */
		void onPostInvalidate(InvalidationReason reason, AbstractCommandNode parent, AbstractCommandNode child);
	}

	/**
	 * The reason why the tree structure can change
	 */
	@SuppressWarnings("unused")
	public enum InvalidationReason {
		/**
		 * A Child was removed
		 */
		CHILD_REMOVED,
		/**
		 * A child was added
		 */
		CHILD_ADDED
	}
}
