package me.ialistannen.bukkitutil.commandsystem.base;

import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultHelpCommand;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * The tree
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommandTree {

	@SuppressWarnings("unused")
	private final CommandRoot root;

	@SuppressWarnings("unused")
	private final List<InvalidationListener> invalidationListener = new ArrayList<>();

	/**
	 * Key for the base root is "tree_root", but it shouldn't be needed!
	 *
	 * @param language The language.
	 * @param plugin   The plugin
	 */
	@SuppressWarnings("unused")
	public CommandTree(MessageProvider language, Plugin plugin) {
		root = new CommandRoot(language);
		setHelpCommand(new DefaultHelpCommand(language, this, "command_help"));
	}

	/**
	 * Sets the help command
	 *
	 * @param node The help command node. Annotated with {@link HelpCommandAnnotation}
	 */
	@SuppressWarnings("unused")
	public void setHelpCommand(AbstractCommandNode node) {
		if (!node.getClass().isAnnotationPresent(HelpCommandAnnotation.class)) {
			throw new IllegalArgumentException("The help node must be annotated with the 'HelpCommandAnnotation'");
		}
		addChild(node);
	}

	/**
	 * Adds an invalidation listener
	 *
	 * @param invalidationListener The invalidationListener to add
	 *
	 * @return The index it was added at
	 */
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
	@SuppressWarnings("unused")
	public void removeInvalidationListener(InvalidationListener listener) {
		this.invalidationListener.remove(listener);
	}

	/**
	 * Adds a child node to the root
	 *
	 * @param child The child node to add
	 */
	@SuppressWarnings("unused")
	public void addChild(AbstractCommandNode child) {
		addChild(getRoot(), child);
	}

	/**
	 * Adds a child node to a parent node
	 *
	 * @param child The child node to add
	 */
	@SuppressWarnings("unused")
	public void addChild(AbstractCommandNode parent, AbstractCommandNode child) {
		onInvalidate(InvalidationReason.CHILD_ADDED, parent, child, true);
		parent.addChild(child);
		onInvalidate(InvalidationReason.CHILD_ADDED, parent, child, false);
	}

	/**
	 * Removes a child node from the root
	 *
	 * @param child The child to remove
	 */
	@SuppressWarnings("unused")
	public void removeChild(AbstractCommandNode child) {
		removeChild(getRoot(), child);
	}

	/**
	 * Removes a child node from a parent
	 *
	 * @param child The child to remove
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public Set<AbstractCommandNode> getChildren(AbstractCommandNode node) {
		return node.getChildren();
	}

	/**
	 * Returns ALL the children, meaning the children and their children and so on
	 *
	 * @return All nodes further down in the the tree from this one on
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public CommandResult executeCommand(CommandSender sender, String... args) {
		return root.executeCommand(sender, args);
	}

	/**
	 * Returns the root of the tree
	 *
	 * @return The root.
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
		void onPreInvalidate(InvalidationReason reason, AbstractCommandNode parent, AbstractCommandNode child);

		/**
		 * It is called AFTER the change has happened
		 *
		 * @param reason The reason why the tree changed it's structure
		 * @param parent The parent node
		 * @param child  The child node
		 */
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")CHILD_REMOVED,
		/**
		 * A child was added
		 */
		@SuppressWarnings("unused")CHILD_ADDED
	}
}
