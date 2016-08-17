package me.ialistannen.bukkitutil.commandsystem.util;

import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandRoot;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil.color;

/**
 * Pages entries
 */
public class Pager {

	/**
	 * Returns the wanted page.
	 * <p>The different language keys are explained here: {@link Page#send(CommandSender, MessageProvider)}
	 *
	 * @param provider       The language provider (usage, and stuff)
	 * @param node           The node to get the children from
	 * @param tree           The {@link CommandTree}
	 * @param withUsage      Whether the usage should be send as well
	 * @param depth          The depth of the entries
	 * @param entriesPerPage The entries per page
	 * @param pageIndex      The index of the page
	 *
	 * @return The resulting page
	 */
	public static Page getPage(MessageProvider provider, AbstractCommandNode node, CommandTree tree,
	                           boolean withUsage, int depth, int entriesPerPage, int pageIndex) {

		List<String> expanded = expand(provider, tree, withUsage, node, depth, 0);

		return slice(expanded, entriesPerPage, pageIndex);
	}

	/**
	 * Returns the page out of the list.
	 *
	 * @param all            All of the Strings
	 * @param entriesPerPage The entries per page
	 * @param pageIndex      Zero based page number. Will be corrected to the biggest one, if too high
	 *
	 * @return The resulting page
	 */
	private static Page slice(List<String> all, int entriesPerPage, int pageIndex) {
		int pageAmount = (int) Math.ceil(all.size() / (double) entriesPerPage);

		if (pageIndex < 0 || pageIndex >= pageAmount) {
			pageIndex = pageIndex < 0 ? 0 : pageAmount - 1;
		}

		List<String> entries = all.subList(
				pageIndex * entriesPerPage,
				Math.min((pageIndex + 1) * entriesPerPage, all.size()));

		return new Page(pageAmount, pageIndex, new ArrayList<>(entries));
	}

	/**
	 * Sends help for one command
	 *
	 * @param maxDepth The maximum depth. Index based. 0 ==> Just this command, 1 ==> Command and children
	 * @param counter  The current counter. Just supply 0. Used for recursion.
	 */
	private static List<String> expand(MessageProvider language, CommandTree tree,
	                                   boolean withUsage,
	                                   AbstractCommandNode node, int maxDepth, int counter) {
		List<String> list = new ArrayList<>();

		String usage = node.getUsage();
		String description = node.getDescription();
		String childrenAmount = tree.getChildren(node).isEmpty() ? "" : String.valueOf(tree.getChildren(node).size());

		if (!(node instanceof CommandRoot)) {
			String finalString;
			{
				if (withUsage) {
					String key = "command_help_format_with_usage";
					finalString = language.trOrDefault(key,
							"&3{0}&9: &7{1} &7<&6{2}&7><newline>  &cUsage: {3}",
							node.getName(), description, childrenAmount, usage);
				} else {
					String key = "command_help_format_without_usage";
					finalString = language.trOrDefault(key,
							"&3{0}&9: &7{1} &7<&6{2}&7>",
							node.getName(), description, childrenAmount, usage);
				}

				finalString = color(finalString);
			}

			for (String s : finalString.split("<newline>")) {
				if (counter == 0) {
					s = color(language.trOrDefault("command_help_top_level_prefix", "")) + s;
				} else {
					s = color(language.trOrDefault("command_help_sub_level_prefix", "")) + s;
				}
				s = CommandSystemUtil.repeat(" ", counter * 2) + s;
				list.add(s);
			}
		} else {
			counter--;
		}

		if (counter >= maxDepth) {
			return list;
		}

		for (AbstractCommandNode commandNode : tree.getChildren(node)) {
			list.addAll(expand(language, tree, withUsage, commandNode, maxDepth, counter + 1));
		}

		return list;
	}

	/**
	 * A displayable page
	 */
	public static class Page {
		private final int maxPages;
		private final int pageIndex;
		private final List<String> entries;

		/**
		 * @param maxPages  The amount of pages it would give, at this depth
		 * @param pageIndex The page number of this page
		 * @param entries   The entries of this page
		 */
		@SuppressWarnings("unused")
		private Page(int maxPages, int pageIndex, List<String> entries) {
			this.maxPages = maxPages;
			this.pageIndex = pageIndex;
			this.entries = entries;
		}

		/**
		 * Sends the page
		 * <ul>
		 * <li>Surrounding:
		 * <ul>
		 * <li>"command_help_header" ==> The header</li>
		 * <ul>
		 * <li>{0} ==> The current page</li>
		 * <li>{1} ==> The amount of pages</li>
		 * </ul>
		 * <li>"command_help_footer" ==> The footer</li>
		 * <ul>
		 * <li>{0} ==> The current page</li>
		 * <li>{1} ==> The amount of pages</li>
		 * </ul>
		 * </ul></li>
		 * <li>Command detail:
		 * <ul>
		 * <li>"command_help_format_with_usage" ==> base format. Supports newlines with {@literal <newline>}
		 * <ul>
		 * <li>{0} ==> Name</li>
		 * <li>{1} ==> Description</li>
		 * <li>{2} ==> Children amount</li>
		 * <li>{3} ==> Usage</li>
		 * </ul></li>
		 * <li>"command_help_top_level_prefix" ==> Prefix for a top level command</li>
		 * <li>"command_help_sub_level_prefix" ==> Prefix for it's children</li>
		 * </ul></li>
		 * </ul>
		 *
		 * @param sender   The {@link CommandSender} to send to
		 * @param language The {@link MessageProvider} to use
		 */
		public void send(CommandSender sender, MessageProvider language) {
			sender.sendMessage(color(language.trOrDefault("command_help_header",
					"\n&5+-------------- &a&lHelp &8(&a{0}&8/&2{1}&8) &5----------------+\n ",
					pageIndex + 1, maxPages)));
			entries.forEach(s -> sender.sendMessage(color(s)));
			sender.sendMessage(color(language.trOrDefault("command_help_footer",
					"\n&5+----------------- &8(&a{0}&8/&2{1}&8) &5------------------+\n ",
					pageIndex + 1, maxPages)));
		}
	}
}
