package me.ialistannen.bukkitutil.commandsystem.base;

import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A root for the command tree
 */
public class CommandRoot extends AbstractCommandNode {

	/**
	 * The tree root. Key is "tree_root".
	 *
	 * @param language The language
	 */
	@SuppressWarnings("unused")
	public CommandRoot(@Nonnull MessageProvider language) {
		super(language, "tree_root");
	}

	@Override
	public boolean isForbidden(Permissible permissible) {
		return false;
	}

	@Override
	public boolean isNotAble(CommandSender sender) {
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return getChildren().stream().map(AbstractCommandNode::getKeyword).collect(Collectors.toList());
	}

	@Override
	public CommandResultType execute(CommandSender sender, String[] args) {
		return CommandResultType.NOT_FOUND;
	}
}
