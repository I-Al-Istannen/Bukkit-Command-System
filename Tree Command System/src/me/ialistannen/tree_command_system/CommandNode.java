package me.ialistannen.tree_command_system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

/**
 * A single command node.
 */
public abstract class CommandNode {

	/**
	 * A list with all the children of this command
	 */
	private List<CommandNode> children;
	/**
	 * Never really used. Intended for your use and for distinguishing different commands in the equals method.
	 */
	private CommandNode parent;
	
	private String name, keyword, permission;
	private Pattern pattern;
	
	/**
	 * Determines if the commandSender is allowed to execute this command. Should be used for Console-Player-Block command sender differences
	 */
	private Predicate<CommandSender> acceptsCommandSender;
	
	/**
	 * @param name The name of the command
	 * @param keyword The keyword the command has. Should match the pattern, but is the one you hope to get :P
	 * @param pattern The pattern which keywords must match.
	 * @param permission The permission for the command. Empty for everybody
	 * @param acceptsCommandSender True if this command can be executed by the commandSender
	 */
	public CommandNode(String name, String keyword, Pattern pattern, String permission, Predicate<CommandSender> acceptsCommandSender) {
		this.name = name;
		this.keyword = keyword;
		this.pattern = pattern;
		this.children = new ArrayList<>();
		this.permission = permission;
		this.acceptsCommandSender = acceptsCommandSender;
	}

	/**
	 * acceptsCommandSender will return true every time.
	 * 
	 * @param name The name of the command
	 * @param keyword The keyword the command has. Should match the pattern, but is the one you hope to get :P
	 * @param pattern The pattern which keywords must match.
	 * @param permission The permission for the command. Empty for everybody
	 */
	public CommandNode(String name, String keyword, Pattern pattern, String permission) {
		this(name, keyword, pattern, permission, (sender) -> true);
	}

	/**
	 * The name of this command. Can be used in your help commands.
	 * 
	 * @return The name of the node
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the keyword for this command. Used in the TabCompleter to give you a valid choice
	 * 
	 * @return The keyword of the node
	 */
	public String getKeyword() {
		return keyword;
	}
	
	/**
	 * Checks if the Permissible has permission to execute this command or tab complete it
	 * 
	 * @param permissible The permissible to check
	 * @return True if the permissible can execute this command
	 */
	public boolean hasPermission(Permissible permissible) {
		return permission.isEmpty() ? true : permissible.hasPermission(permission);
	}
	
	/**
	 * Checks if the given input matches the pattern for this command
	 * 
	 * @param input The input to check
	 * @return True if the input matches the pattern
	 */
	public boolean matchesPattern(String input) {
		return pattern.matcher(input).matches();
	}
	
	/**
	 * @param sender The {@link CommandSender} to check
	 * 
	 * @return True if this command needs a player
	 */
	public boolean acceptsCommandSender(CommandSender sender) {
		return acceptsCommandSender.test(sender);
	}
	
	/**
	 * The usage of this command
	 * 
	 * @return The usage for this command. Nothing will be appended at the front.
	 */
	public abstract String getUsage();
	
	/**
	 * A description of this command
	 * 
	 * @return The description of the command. Nothing will be appended at the front.
	 */
	public abstract String getDescription();
	
	/**
	 * Sets the parent of this node
	 * 
	 * @param parent The new parent
	 */
	protected void setParent(CommandNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Gets the parent of this node.
	 * 
	 * @return The parent. May be empty if this is the root node.
	 */
	protected Optional<CommandNode> getParent() {
		return Optional.ofNullable(parent);
	}
	
	/**
	 * Reacts to the TabComplete and returns a list with valid choices. Passes it down to the best matching children.
	 * 
	 * @param wholeUserChat All the words the user wrote.
	 * @param tabCompleter The {@link CommandSender} who initiated the tab complete. May be queried for permission checks or similar.
	 * @return A list with all valid tab completions
	 */
	public final List<String> onTabComplete(List<String> wholeUserChat, CommandSender tabCompleter) {
		return getTabCompletionsRecursive(wholeUserChat.get(wholeUserChat.size() - 1), 0, wholeUserChat, tabCompleter);
	}
	
	/**
	 * You will never see this. But anyways. It tries to get a list with all valid tab completions.
	 * It does this, by grabbing the next part of the users message each iteration, and comparing it against all children.
	 * It will distinguish it in "possible children", whose keyword starts with the input and the real node, whose pattern
	 * matches the input. The real node will handle it. If there is none, the potential children will be checked.
	 * If there are also none, this node will tab complete it.
	 * 
	 * @param input The current word the user wrote. May be emtpy
	 * @param currentIterationIndex A counter in the recursive search 
	 * @param wholeUserChat All the words the user wrote.
	 * @param tabCompleter The {@link CommandSender} who initiated the tab complete. May be queried for permission checks or similar.
	 * @return A list with all valid tab completions
	 */
	private List<String> getTabCompletionsRecursive(String input, int currentIterationIndex, List<String> wholeUserChat, CommandSender tabCompleter) {
		String stringToCheck;
		if(currentIterationIndex >= wholeUserChat.size()) {
			stringToCheck = input;
		}
		else {
			stringToCheck = wholeUserChat.get(currentIterationIndex);
		}
		
		List<CommandNode> potentialChildren = new ArrayList<>();
		for (CommandNode commandNode : children) {
			// respect the acceptsCommandSender setting     and the permissions too 
			if(!commandNode.acceptsCommandSender(tabCompleter) || !commandNode.hasPermission(tabCompleter)) {
				continue;
			}
			if(commandNode.getKeyword().equalsIgnoreCase(stringToCheck) || commandNode.matchesPattern(stringToCheck)) {
				// let the child handle it. It was called.
				currentIterationIndex++;
				return commandNode.getTabCompletionsRecursive(input, currentIterationIndex, wholeUserChat, tabCompleter);
			}
			// add as potential child
			else if(commandNode.getKeyword().startsWith(stringToCheck)) {
				potentialChildren.add(commandNode);
			}
		}
		
		if(!potentialChildren.isEmpty()) {
			return potentialChildren.stream().map(CommandNode::getKeyword).collect(Collectors.toList());
		}
		
		return getTabCompletions(input, wholeUserChat, tabCompleter);
	}
	
	/**
	 * The tab completions for this particular command
	 * 
	 * @param input The current word the user wrote. May be emtpy
	 * @param wholeUserChat All the words the user wrote.
	 * @param tabCompleter The {@link CommandSender} who initiated the tab complete. May be queried for permission checks or similar.
	 * @return A list with all valid tab completions
	 */
	protected abstract List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter);
	
	/**
	 * Let's the lowest mentioned child execute the command with all remaining arguments
	 * 
	 * @param sender The CommandSender who executed the command
	 * @param args The arguments he passed
	 * @return False if the usage was send
	 */
	public boolean executeCommand(CommandSender sender, String... args) {
		return executeCommandRecursive(sender, args);
	}
	
	/**
	 * Checks if one of the children should execute the command, and if yes passes it down. Doesn't care for permissions.
	 * The permissions check is implemented in the CommandTree, as a message should be send and it can't fail silently.
	 * 
	 * @param sender The CommandSender who executed the command
	 * @param args The arguments he passed
	 * @return False if the usage was send
	 */
	private boolean executeCommandRecursive(CommandSender sender, String... args) {
		String stringToCheck;
		if(args.length == 0) {
			// I sincerly hope none of your regular expressions match empty Strings... Otherwise, this will be spectacular :P
			stringToCheck = "";
		}
		else {
			// as the item with the smallest index is removed at every iteration, this is valid.
			stringToCheck = args[0];
		}
		
		// this boolean is to see if the CommandSender failed executing a command for a different sender. It can then check for other
		// matching commands, which do not fail. Second reason for this is sending the message. If you don't do it, you will never
		// know if it failed at the CommandSender being invalid.
		boolean commandDeniedDueToSender = false;
		for (CommandNode commandNode : getDirectChildren()) {
			if(commandNode.matchesPattern(stringToCheck)) {
				// respect the CommandSender property
				if(commandNode.acceptsCommandSender(sender)) {
					// remove the left most element, as it would be the keyword of the command. And we don't want that, do we?
					return commandNode.executeCommand(sender, TreeCommandSystemUtil.getArrayStartingAtPosition(args, 1));	
				}
				else {
					// if another one matches, this will never be relevant. So you can safely set it and never reset it.
					commandDeniedDueToSender = true;
				}
			}
		}
		
		// handle if the console executes a command for a different command sender (and failed at a child) or fails at this one.
		if(commandDeniedDueToSender || !acceptsCommandSender(sender)) {
			// TODO: UN-Hardcode this string! How would you do it? Passing it through the constructor is not so nice, and passing a MessageProvider is a bit
			// overkill. How would you do it?
			sender.sendMessage(TreeCommandSystemUtil.color(TreeCommandSystemUtil.NO_PLAYER_MESSAGE));
			return true;
		}
		
		if(!execute(sender, args)) {
			sender.sendMessage(TreeCommandSystemUtil.color(getUsage()));
			return false;
		}
		return true;
	}
	
	/**
	 * Executes the command. The arguments won't contain your keyword.
	 * 
	 * @param sender The commandSender who executed the command
	 * @param args The arguments he passed
	 * @return False if the usage should be send.
	 */
	protected abstract boolean execute(CommandSender sender, String... args);
	
	/**
	 * Adds a child to this node
	 * 
	 * @param child The child to add
	 * @return True if the child was added
	 */
	protected boolean addChild(CommandNode child) {
		if(children.contains(child)) {
			return false;
		}
		return children.add(child);
	}

	/**
	 * Tries to find a node, given the arguments to execute it
	 * 
	 * @param arguments The arguments the user entered
	 * @return The closest command node or an empty optional if nothing matched
	 */
	public Optional<CommandNode> find(String... arguments) {
		if(arguments.length < 1) {
			return Optional.empty();
		}
		for (CommandNode commandNode : getDirectChildren()) {
			if(commandNode.matchesPattern(arguments[0])) {
				Optional<CommandNode> subNode = commandNode.find(TreeCommandSystemUtil.getArrayStartingAtPosition(arguments, 1));
				if(subNode.isPresent()) {
					return subNode;
				}
				else {
					return Optional.of(commandNode);
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Finds a given CommandNode. Used in the merger method in the CommandTreeManager.
	 * 
	 * @param node The node to search
	 * @return The CommandNode if found or an empty optional if not.
	 */
	protected Optional<CommandNode> find(CommandNode node) {
		return getChildrenRecursive().stream().filter(node::equals).findAny();
	}

	
	/**
	 * Gets all direct children of this node and returns them
	 * 
	 * @return The children in an unmodifiable list
	 */
	protected List<CommandNode> getDirectChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Returns all the nodes in the whole tree, which are children of this node.
	 * 
	 * @return The children and their children in a list.
	 */
	protected List<CommandNode> getChildrenRecursive() {
		// don't add yourself, as it wants children. You will be added by your parent, if you belong in the list.
		
		List<CommandNode> list = new ArrayList<>();

		list.addAll(getDirectChildren());
		
		for (CommandNode commandNode : getDirectChildren()) {
			list.addAll(commandNode.getChildrenRecursive());
		}
		
		return list;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CommandNode [name=" + name + ", keyword=" + keyword
				+ ", pattern=" + pattern + "]";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.pattern().hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandNode other = (CommandNode) obj;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.pattern().equals(other.pattern.pattern()))
			return false;
		return true;
	}
}