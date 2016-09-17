package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.base.CommandRoot;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.bukkitutil.commandsystem.base.HelpCommandAnnotation;
import me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil;
import me.ialistannen.bukkitutil.other.Pager;
import me.ialistannen.bukkitutil.other.Pager.Options;
import me.ialistannen.bukkitutil.other.Pager.PagerFilterable;
import me.ialistannen.bukkitutil.other.Pager.SearchMode;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil.color;

/**
 * The default help command
 * <p><b>Needs a few keys:</b>
 * <li>KEY + "_permission"</li>
 * <li>KEY + "_name"</li>
 * <li>KEY + "_keyword"</li>
 * <li>KEY + "_description"</li>
 * <li>KEY + "_usage"</li>
 * <li>KEY + "_pattern"</li>
 * <p>
 * <li>KEY + "_not_found"</li>
 * <ul>
 * <li>{0} ==> What the user entered</li>
 * </ul>
 * </ul>
 * <p>
 * And all the keys from the {@link Pager.Page#send(CommandSender, MessageProvider)} method
 */
@HelpCommandAnnotation
public class DefaultHelpCommand extends AbstractCommandNode {

	private final CommandTree tree;
	private final String KEY;

	/**
	 * Please see {@link DefaultHelpCommand} for the needed language keys
	 *
	 * @param language The language
	 * @param tree     The command tree. Queried for all the children.
	 * @param key      The Base key. Default is "command_help"
	 *
	 * @see AbstractCommandNode#AbstractCommandNode(MessageProvider)
	 */
	@SuppressWarnings("unused")
	public DefaultHelpCommand(@Nonnull MessageProvider language, @Nonnull CommandTree tree,
	                          String key) {

		super(language, key);
		this.tree = tree;
		this.KEY = key;
	}

	@Override
	public boolean isForbidden(Permissible permissible) {
		return !permissible.hasPermission(language.tr(KEY + "_permission"));
	}

	@Override
	public boolean isNotAble(CommandSender sender) {
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return Arrays.asList("--depth=", "--page=", "--entriesPerPage=", "--showUsage=false",
				"--search=", "--searchRegEx=");
	}

	@Override
	public CommandResultType execute(CommandSender sender, String[] args) {
		AtomicInteger page = new AtomicInteger(0);
		AtomicInteger depth = new AtomicInteger(2);
		AtomicInteger entriesPerPage = new AtomicInteger(10);
		AtomicBoolean showUsage = new AtomicBoolean(false);
		StringBuilder searchFilter = new StringBuilder();
		AtomicBoolean searchUsingRegEx = new AtomicBoolean(false);
		Arrays.stream(args)
				.filter(s -> s.matches("--depth=\\d{1,9}"))
				.forEach(s -> {
					s = s.replace("--depth=", "");
					depth.set(Integer.parseInt(s));
				});
		Arrays.stream(args)
				.filter(s -> s.toLowerCase().matches("--page=\\d{1,9}"))
				.forEach(s -> {
					s = s.replace("--page=", "");
					page.set(Integer.parseInt(s) - 1);
				});
		Arrays.stream(args)
				.filter(s -> s.toLowerCase().matches("--entriesperpage=\\d{1,9}"))
				.forEach(s -> {
					s = s.replace("--entriesPerPage=", "");
					entriesPerPage.set(Integer.parseInt(s));
				});
		Arrays.stream(args)
				.filter(s -> s.toLowerCase().matches("--showusage=(true|false)"))
				.forEach(s -> {
					s = s.replace("--showUsage=", "");
					showUsage.set(Boolean.parseBoolean(s));
				});
		Arrays.stream(args)
				.filter(s -> s.matches("--search=.+"))
				.forEach(s -> {
					s = s.replace("--search=", "");
					searchFilter.append(s.trim());
				});
		Arrays.stream(args)
				.filter(s -> s.matches("--searchRegEx=.+"))
				.forEach(s -> {
					s = s.replace("--searchRegEx=", "");
					if (searchFilter.length() != 0) {
						searchFilter.delete(0, searchFilter.length());
					}
					searchFilter.append(s.trim());

					searchUsingRegEx.set(true);
				});

		Options options = Options.builder()
				.setEntriesPerPage(entriesPerPage.get())
				.setPageIndex(page.get())
				.setSearchPattern(searchFilter.toString())
				.setSearchModes(searchUsingRegEx.get()
						? SearchMode.REGEX_FIND
						: SearchMode.CONTAINS).build();

		if (args.length > 0) {
			AbstractCommandNode.FindCommandResult result = tree.find(new ArrayDeque<>(Arrays.asList(args)), sender);

			if (result.getResult() == CommandResultType.SUCCESSFUL) {
				List<PagerFilterable> entries =
						getCommandFilterable(language, tree, showUsage.get(), result.getCommandNode(), depth.get(), 0);
				Pager.getPageFromFilterable(options, entries)
						.send(sender, language);
			}
			else {
				sender.sendMessage(language.tr(KEY + "_not_found",
						Arrays.stream(args).collect(Collectors.joining(" "))));
			}
			return CommandResultType.SUCCESSFUL;
		}

		List<PagerFilterable> entries =
				getCommandFilterable(language, tree, showUsage.get(), tree.getRoot(), depth.get(), 0);

		Pager.getPageFromFilterable(options, entries)
				.send(sender, language);

		return CommandResultType.SUCCESSFUL;
	}

	/**
	 * Sends help for one command
	 *
	 * @param maxDepth The maximum depth. Index based. 0 ==> Just this command, 1 ==> Command and children
	 * @param counter  The current counter. Just supply 0. Used for recursion.
	 */
	private static List<PagerFilterable> getCommandFilterable(MessageProvider language, CommandTree tree,
	                                                          boolean withUsage, AbstractCommandNode node,
	                                                          int maxDepth, int counter) {
		List<PagerFilterable> list = new ArrayList<>();

		if (!(node instanceof CommandRoot)) {
			PagerFilterable filterable = new CommandFilterable(node, withUsage, tree.getChildren(node).size(),
					language, counter);
			list.add(filterable);
		}
		else {
			counter--;
		}

		if (counter >= maxDepth) {
			return list;
		}

		for (AbstractCommandNode commandNode : tree.getChildren(node)) {
			list.addAll(getCommandFilterable(language, tree, withUsage, commandNode, maxDepth, counter + 1));
		}

		return list;
	}

	private static class CommandFilterable implements PagerFilterable {

		private AbstractCommandNode node;
		private boolean showUsage;
		private String childrenAmount;
		private MessageProvider language;
		private int depth;

		private List<String> allLines;

		CommandFilterable(AbstractCommandNode node, boolean showUsage, int childrenAmount,
		                  MessageProvider language, int depth) {
			this.node = node;
			this.showUsage = showUsage;
			this.childrenAmount = childrenAmount == 0 ? "" : Integer.toString(childrenAmount);
			this.language = language;
			this.depth = depth;

			calculateAllLines();
		}

		@Override
		public boolean accepts(Options options) {
			// match against what is shown
			for (String line : allLines) {
				if (options.matchesPattern(strip(line))) {
					return true;
				}
			}
			return false;
		}

		/**
		 * @param coloredString The String to strip the colors from
		 *
		 * @return The uncolored String
		 */
		private static String strip(String coloredString) {
			return ChatColor.stripColor(coloredString);
		}

		private void calculateAllLines() {
			String finalString;
			{
				if (showUsage) {
					String key = "command_help_format_with_usage";
					finalString = language.trOrDefault(key,
							"&3{0}&9: &7{1} &7<&6{2}&7><newline>  &cUsage: {3}",
							node.getName(), node.getDescription(), childrenAmount, node.getUsage());
				}
				else {
					String key = "command_help_format_without_usage";
					finalString = language.trOrDefault(key,
							"&3{0}&9: &7{1} &7<&6{2}&7>",
							node.getName(), node.getDescription(), childrenAmount, node.getUsage());
				}

				finalString = color(finalString);
			}

			List<String> list = new ArrayList<>();

			for (String s : finalString.split("<newline>")) {
				if (depth == 0) {
					s = color(language.trOrDefault("command_help_top_level_prefix", "")) + s;
				}
				else {
					s = color(language.trOrDefault("command_help_sub_level_prefix", "")) + s;
				}
				s = CommandSystemUtil.repeat(language.trOrDefault("command_help_padding_char", "  "), depth) + s;

				if (!s.isEmpty()) {
					list.add(s);
				}
			}

			allLines = list;
		}

		@Override
		public @NotNull List<String> getAllLines() {
			return allLines;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CommandFilterable)) {
				return false;
			}
			CommandFilterable that = (CommandFilterable) o;
			return Objects.equals(node, that.node);
		}

		@Override
		public int hashCode() {
			return Objects.hash(node);
		}

		@Override
		public String toString() {
			return "CommandFilterable{" +
					"node=" + node.getName() +
					", showUsage=" + showUsage +
					", childrenAmount='" + childrenAmount + '\'' +
					", depth=" + depth +
					", allLines=" + getAllLines() +
					'}';
		}
	}
}
