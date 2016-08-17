package me.ialistannen.bukkitutil.commandsystem.implementation;

import me.ialistannen.bukkitutil.commandsystem.base.AbstractCommandNode;
import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.bukkitutil.commandsystem.base.HelpCommandAnnotation;
import me.ialistannen.bukkitutil.commandsystem.util.Pager;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
		return Arrays.asList("--depth=", "--page=", "--entriesPerPage=", "--showUsage=false");
	}

	@Override
	public CommandResultType execute(CommandSender sender, String[] args) {
		AtomicInteger page = new AtomicInteger(0);
		AtomicInteger depth = new AtomicInteger(2);
		AtomicInteger entriesPerPage = new AtomicInteger(10);
		AtomicBoolean showUsage = new AtomicBoolean(false);
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

		if (args.length > 0) {
			AbstractCommandNode.FindCommandResult result = tree.find(new ArrayDeque<>(Arrays.asList(args)), sender);

			if (result.getResult() == CommandResultType.SUCCESSFUL) {
				Pager.getPage(language, result.getCommandNode(), tree, showUsage.get(), depth.get(),
						entriesPerPage.get(), page.get())
						.send(sender, language);
			} else {
				sender.sendMessage(language.tr(KEY + "_not_found",
						Arrays.stream(args).collect(Collectors.joining(" "))));
			}
			return CommandResultType.SUCCESSFUL;
		}

		Pager.getPage(language, tree.getRoot(), tree, showUsage.get(), depth.get(), entriesPerPage.get(), page.get())
				.send(sender, language);

		return CommandResultType.SUCCESSFUL;
	}
}
