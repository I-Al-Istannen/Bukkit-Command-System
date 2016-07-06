package me.ialistannen.tree_command_system;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

/**
 * Manages the CommandTree and provides utility functions.
 */
public class CommandTreeManager {

	private CommandNode root;
	
	/**
	 * Constructs a new instance and creates a root
	 */
	public CommandTreeManager() {
		root = new RootNode();
	}
	
	/**
	 * @param root The new root node
	 */
	public void setRoot(CommandNode root) {
		this.root = root;
	}
	
	/**
	 * @param parent The parent
	 * @param child The child
	 * @return True if the child was added
	 */
	public boolean registerChild(CommandNode parent, CommandNode child) {
		boolean worked = parent.addChild(child);
		if(!worked) {
			return false;
		}
		child.setParent(parent);
		
		return true;
	}

	/**
	 * @param source The {@link CommandNode} who wants to register the sibling
	 * @param sibling The sibling
	 * @return True if the sibling was added
	 */
	public boolean registerSibling(CommandNode source, CommandNode sibling) {
		if(!source.getParent().isPresent()) {
			return false;
		}
		
		return registerChild(source.getParent().get(), sibling);
	}
	
	/**
	 * @param otherRoot The other root to merge
	 * @throws IllegalStateException If there is no root set
	 */
	public void mergeRoot(CommandNode otherRoot) throws IllegalStateException {
		// can't happen normally
		if(this.root == null) {
			throw new IllegalStateException("There is no root. Use the setRoot method.");
		}
		for (CommandNode commandNode : otherRoot.getChildrenRecursive()) {
			Optional<CommandNode> parent = commandNode.getParent();
			if(!parent.isPresent()) {
				// didn't set the parent node. You would actively need to mess it to achieve this.
				// otherwise it is otherRoot
				continue;
			}
			// check if the node is a direct child of the root node
			if(!parent.get().getParent().isPresent()) {
				parent = Optional.of(root);
			}
			else {
				// find the corresponding parent in this tree
				parent = root.find(parent.get());
			}
			if(parent.isPresent()) {
				registerChild(parent.get(), commandNode);
			}
		}
	}
	
	/**
	 * @return The root node
	 */
	public CommandNode getRoot() {
		return root;
	}
	
	/**
	 * @return A list with all the children of {@link #getRoot()}
	 */
	public List<CommandNode> getAllCommands() {
		return root.getChildrenRecursive();
	}
	
	/**
	 * @param sender The sender
	 * @param args The arguments to execute it with
	 */
	public void execute(CommandSender sender, String... args) {
		root.executeCommand(sender, args);
	}
	
	/**
	 * @param sender The {@link CommandSender}
	 * @param args The arguments the sender wrote
	 * @return True if he has permission to execute the command
	 */
	public boolean hasPermission(CommandSender sender, String... args) {
		Optional<CommandNode> cmd = root.find(args);
		if(!cmd.isPresent()) {
			return true;
		}
		else {
			return cmd.get().hasPermission(sender);
		}
	}
	
	/**
	 * @param args The arguments the user wrote
	 * @return True if a close {@link CommandNode} was found, false otherwise
	 */
	public boolean nodeExists(String... args) {
		return root.find(args).isPresent();
	}
	
	/**
	 * @return The help node, if there is any
	 */
	public Optional<CommandNode> getHelpNode() {
		return getRoot().getChildrenRecursive().stream().filter(node -> node.getClass().isAnnotationPresent(HelpCommandNode.class)).findFirst();
	}
	

	/**
	 * A root node. Does basically nothing.
	 */
	private static class RootNode extends CommandNode {

		/**
		 * Creates a new root node 
		 */
		public RootNode() {
			super("Root", "ROOT_NODE", Pattern.compile("ROOT_NODE"), "");
		}

		@Override
		public String getUsage() {
			return "";
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender sender) {
			return Collections.emptyList();
		}

		@Override
		public boolean execute(CommandSender sender, String... args) {
			return true;
		}
	}
}
