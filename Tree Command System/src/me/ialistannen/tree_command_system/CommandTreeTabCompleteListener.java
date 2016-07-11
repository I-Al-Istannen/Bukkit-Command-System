package me.ialistannen.tree_command_system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Listens for tab completes
 */
public class CommandTreeTabCompleteListener implements TabCompleter {

	private CommandTreeManager treeManager;
	
	private boolean distinct;
	private boolean startingWitchCheckForFoundOnes;
	
	/**
	 * @param treeManager The tree manager
	 * @param distinct If true, duplicate commands will be suppressed (e.g. for different senders or just registered twice)
	 */
	public CommandTreeTabCompleteListener(CommandTreeManager treeManager, boolean distinct) {
		this(treeManager, distinct, true);
	}

	/**
	 * @param treeManager The tree manager
	 * @param distinct If true, duplicate commands will be suppressed (e.g. for different senders or just registered twice)
	 * @param startingWitchCheckForFoundOnes If true, the commands will be filtered if they don't start with what the user wrote.
	 */
	public CommandTreeTabCompleteListener(CommandTreeManager treeManager, boolean distinct, boolean startingWitchCheckForFoundOnes) {
		this.treeManager = treeManager;
		this.distinct = distinct;
		this.startingWitchCheckForFoundOnes = startingWitchCheckForFoundOnes;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if(args.length <= 1) {
			list.addAll(
					applyDistinct(
						treeManager.getRoot().getDirectChildren().stream()
						.filter(cmd -> cmd.hasPermission(sender))
						.map(CommandNode::getKeyword)
						.filter(string -> string.toLowerCase().startsWith(args[0].toLowerCase()))
						.sorted()
					)
			);
			return list;
		}
		if(treeManager.nodeExists(args)) {
			List<String> commands = treeManager.getRoot().onTabComplete(Arrays.asList(args), sender);
			if(startingWitchCheckForFoundOnes) {
				return commands.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
			}
			else {
				return commands;
			}
		}
		else {
			return Collections.emptyList();
		}
	}

	/**
	 * If {@link #distinct} is true, this will make it disting. Otherwise it won't change anything.
	 * 
	 * @param input The input Stream
	 * @return The resulting List
	 */
	private List<String> applyDistinct(Stream<String> input) {
		if(!distinct) {
			return input.collect(Collectors.toList());
		}
		return input.distinct().collect(Collectors.toList());
	}
}
