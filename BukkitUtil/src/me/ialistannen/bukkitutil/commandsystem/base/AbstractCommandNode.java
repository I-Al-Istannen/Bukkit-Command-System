package me.ialistannen.bukkitutil.commandsystem.base;

import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An abstract command node.
 */
public abstract class AbstractCommandNode implements BukkitCommand, BukkitAccessRestrictable {


	protected final MessageProvider language;


	private final String BASE_KEY;


	private final Set<AbstractCommandNode> children = new HashSet<>();

	/**
	 * Constructs a command.
	 *
	 * @param language The language to use
	 * @param baseKey  The base key for the language. Should be unique, is used by equals and hashcode.
	 */
	@SuppressWarnings("unused")
	public AbstractCommandNode(@Nonnull MessageProvider language, @Nonnull String baseKey) {
		this.language = language;
		this.BASE_KEY = baseKey;
	}

	/**
	 * Constructs a command.
	 * <br>The Base key will be the simple name of the class.
	 *
	 * @param language The language to use
	 *
	 * @see #AbstractCommandNode(MessageProvider, String)
	 */
	@SuppressWarnings("unused")
	public AbstractCommandNode(@Nonnull MessageProvider language) {
		this.language = language;
		this.BASE_KEY = getClass().getSimpleName();
	}

	/**
	 * The command's usage
	 *
	 * @return The usage of the command
	 */
	public String getUsage() {
		return language.tr(CommandInformationKey.USAGE.applyTo(BASE_KEY));
	}

	/**
	 * The command's description
	 *
	 * @return The description of the command
	 */
	public String getDescription() {
		return language.tr(CommandInformationKey.DESCRIPTION.applyTo(BASE_KEY));
	}

	/**
	 * The command's name
	 *
	 * @return The name of the command
	 */
	public String getName() {
		return language.tr(CommandInformationKey.NAME.applyTo(BASE_KEY));
	}

	/**
	 * The command's keyword
	 *
	 * @return The keyword of the command
	 */
	@SuppressWarnings("WeakerAccess")
	public String getKeyword() {
		return language.tr(CommandInformationKey.KEYWORD.applyTo(BASE_KEY));
	}

	/**
	 * Checks whether the string matches this commands keywords or not
	 *
	 * @param string The string to check
	 *
	 * @return True if it is this command
	 */
	@SuppressWarnings("WeakerAccess")
	public boolean matchesPattern(@Nullable String string) {
		return string != null
				&& Pattern
				.compile(language.tr(CommandInformationKey.PATTERN.applyTo(BASE_KEY)), Pattern.CASE_INSENSITIVE)
				.matcher(string)
				.matches();
	}


	/**
	 * Performs the tab completion by delegating it to a child
	 *
	 * @param sender      The {@link CommandSender}
	 * @param alias       The used alias for the command
	 * @param currentArgs The current arguments. Used for recursive iterating.
	 * @param args        The args the user entered
	 *
	 * @return A list with valid completions. Empty for none, null for all online, visible players
	 */
	private FindTabCompleteResult doTabComplete(@Nonnull CommandSender sender, @Nonnull String alias,
	                                            @Nonnull Queue<String> currentArgs,
	                                            @Nonnull String[] args) {

		// don't poll the first if it is the root. Make the root transparent
		String currentString = (this instanceof CommandRoot) ? "" : currentArgs.poll();
		String lastString = args.length == 0 ? "" : args[args.length - 1];

		if (args.length <= 1) {
			if (this instanceof CommandRoot) {
				return new FindTabCompleteResult(
						chooseStartingWith(getChildren().stream()
										.filter(child -> !child.isForbidden(sender) && !child.isNotAble(sender))
										.map(AbstractCommandNode::getKeyword)
										.collect(Collectors.toList()),
								lastString),
						CommandResultType.SUCCESSFUL);
			} else {
				return new FindTabCompleteResult(Collections.singletonList(getKeyword()),
						CommandResultType.SUCCESSFUL);
			}
		}

		//                  arg0
		// CurrentString:    ^                  ==> Fail. Not found, you can't completer yourself

		//                  arg0 arg1 arg2
		// CurrentString:    ^                  ==> Pass to child

		//                  arg1 arg2
		// CurrentString:    ^                  ==> Complete yourself

		if (matchesPattern(currentString) || (this instanceof CommandRoot)) {
			if (currentArgs.size() >= 1) {
				for (AbstractCommandNode child : getChildren()) {
					FindTabCompleteResult childResult = child
							// !pass a copy of the queue!
							.doTabComplete(sender, alias, new ArrayDeque<>(currentArgs), args);

					if (childResult.getResult() == CommandResultType.SUCCESSFUL) {
						return childResult;
					}
				}

				if (isForbidden(sender)) {
					return new FindTabCompleteResult(Collections.emptyList(), CommandResultType.PERMISSION_DENIED);
				}
				if (isNotAble(sender)) {
					return new FindTabCompleteResult(Collections.emptyList(), CommandResultType.WRONG_SENDER);
				}

				return new FindTabCompleteResult(

						chooseStartingWith(
								tabComplete(sender, alias, Arrays.asList(args), currentArgs.size() - 1),
								lastString),

						CommandResultType.SUCCESSFUL);
			} else {
				return new FindTabCompleteResult(Collections.emptyList(), CommandResultType.NOT_FOUND);
			}
		}

		return new FindTabCompleteResult(Collections.emptyList(), CommandResultType.NOT_FOUND);
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
	final FindTabCompleteResult doTabComplete(@Nonnull CommandSender sender, @Nonnull String alias,
	                                          @Nonnull String[] args) {
		return doTabComplete(sender, alias, new ArrayDeque<>(Arrays.asList(args)), args);
	}

	/**
	 * Chooses the entry from the list starting with the given string. Case ignored
	 *
	 * @param choices      The choices to choose from
	 * @param startingWith The string it should start with
	 *
	 * @return A list of all the choices which match the startingWith string
	 */
	private List<String> chooseStartingWith(List<String> choices, String startingWith) {
		if (startingWith == null || startingWith.isEmpty() || choices == null || choices.isEmpty()) {
			return choices;
		}
		return choices.stream()
				.filter(s -> s.toLowerCase().startsWith(startingWith.toLowerCase()))
				.collect(Collectors.toList());
	}

	/**
	 * Tries to find a command using recursion
	 *
	 * @param args   The arguments to find the command for
	 * @param sender The sender to find it for
	 *
	 * @return The command or an empty optional
	 */
	FindCommandResult find(Queue<String> args, CommandSender sender) {
		String currentArgs = this instanceof CommandRoot ? "" : args.poll();

		FindCommandResult result = new FindCommandResult(this, new ArrayDeque<>(args), CommandResultType.NOT_FOUND);
		if (matchesPattern(currentArgs) || (this instanceof CommandRoot)) {
			// now we at least found this matching command
			result = new FindCommandResult(this, new ArrayDeque<>(args), CommandResultType.SUCCESSFUL);

			if (isNotAble(sender)) {
				return new FindCommandResult(this, new ArrayDeque<>(args), CommandResultType.WRONG_SENDER);
			} else if (isForbidden(sender)) {
				return new FindCommandResult(this, new ArrayDeque<>(args), CommandResultType.PERMISSION_DENIED);
			} else {
				FindCommandResult tmpChildResult = null;
				for (AbstractCommandNode commandNode : getChildren()) {
					FindCommandResult childResult = commandNode.find(new ArrayDeque<>(args), sender);
					if (childResult.getResult() == CommandResultType.SUCCESSFUL) {
						return childResult;
					} else if (childResult.getResult() != CommandResultType.NOT_FOUND) {
						tmpChildResult = childResult;
					}
				}

				if (tmpChildResult != null) {
					result = tmpChildResult;
				}
			}
		}

		return result;
	}

	/**
	 * Executes the command, by finding the responsible node and passing it to him
	 *
	 * @param sender The sender of the command
	 * @param args   The arguments of the command
	 *
	 * @return The CommandResult
	 */
	CommandResult executeCommand(CommandSender sender, String... args) {
		FindCommandResult cmd = find(new ArrayDeque<>(Arrays.asList(args)), sender);

		String[] remainingArgs = cmd.getRemainingArguments().toArray(new String[cmd.getRemainingArguments().size()]);
		if (cmd.getResult() == CommandResultType.SUCCESSFUL) {
			return new CommandResult(cmd.getCommandNode(), cmd.getCommandNode().execute(sender, remainingArgs));
		}

		return new CommandResult(cmd.getCommandNode(), cmd.getResult());
	}

	/**
	 * Adds a child node
	 *
	 * @param child The child node to add
	 */
	@SuppressWarnings("WeakerAccess")
	protected void addChild(AbstractCommandNode child) {
		children.add(child);
	}

	/**
	 * Removes a child node
	 *
	 * @param child The child to remove
	 */
	@SuppressWarnings("WeakerAccess")
	protected void removeChild(AbstractCommandNode child) {
		children.remove(child);
	}

	/**
	 * Finds the help command
	 *
	 * @return The help command, if any found
	 */
	@SuppressWarnings("unused") // Maybe a command executor can make use of this.
	public Optional<AbstractCommandNode> findHelpCommand() {
		return getAllChildren().stream()
				.filter(abstractCommandNode -> abstractCommandNode.getClass()
						.isAnnotationPresent(HelpCommandAnnotation.class))
				.findAny();
	}

	/**
	 * Returns all the direct children
	 *
	 * @return All of the direct children, in an unmodifiable set
	 */
	protected Set<AbstractCommandNode> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	/**
	 * Returns ALL the children, meaning the children and their children and so on
	 *
	 * @return All nodes further down in the the tree from this one on
	 */
	List<AbstractCommandNode> getAllChildren() {
		List<AbstractCommandNode> list = new ArrayList<>();
		list.addAll(getChildren());

		for (AbstractCommandNode commandNode : getChildren()) {
			list.addAll(commandNode.getAllChildren());
		}

		return list;
	}

	/**
	 * The result of the find method
	 */
	protected class FindCommandResult {

		private final AbstractCommandNode commandNode;

		private final Queue<String> remainingArguments;

		private final CommandResultType result;

		/**
		 * @param commandNode        The command node
		 * @param remainingArguments The remaining arguments
		 * @param result             The CommandResultType (for permission, sender and not found)
		 */
		@SuppressWarnings("unused")
		public FindCommandResult(@Nonnull AbstractCommandNode commandNode, @Nonnull Queue<String> remainingArguments,
		                         @Nonnull CommandResultType result) {
			this.commandNode = commandNode;
			this.remainingArguments = remainingArguments;
			this.result = result;
		}

		/**
		 * The command result
		 *
		 * @return The result
		 */
		public CommandResultType getResult() {
			return result;
		}

		/**
		 * Returns the command Node if any
		 *
		 * @return The command node if any
		 */
		public AbstractCommandNode getCommandNode() {
			return commandNode;
		}

		/**
		 * Returns the remaining arguments
		 *
		 * @return The remaining arguments. May be empty.
		 */
		public Queue<String> getRemainingArguments() {
			return remainingArguments;
		}
	}

	/**
	 * The result of the tab complete method
	 */
	public class FindTabCompleteResult {

		private final List<String> resultList;

		private final CommandResultType result;

		/**
		 * @param resultList The resulting list with tab completions
		 * @param result     The CommandResultType (for permission, sender and not found)
		 */
		@SuppressWarnings("unused")
		public FindTabCompleteResult(@Nonnull List<String> resultList, @Nonnull CommandResultType result) {
			this.resultList = resultList;
			this.result = result;
		}

		/**
		 * The command result
		 *
		 * @return The result
		 */
		public CommandResultType getResult() {
			return result;
		}

		/**
		 * Returns the resulting completion
		 *
		 * @return The resulting completion
		 */
		public List<String> getResultList() {
			return resultList;
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AbstractCommandNode that = (AbstractCommandNode) o;
		return Objects.equals(BASE_KEY, that.BASE_KEY);
	}


	@Override
	public int hashCode() {
		return Objects.hash(BASE_KEY);
	}
}
