package me.ialistannen.tree_command_system;

import java.lang.reflect.Array;

import org.bukkit.ChatColor;

/**
 * Some static utility functions.
 */
public class TreeCommandSystemUtil {
	
	/**
	 * Temporary constant until a message system is implemented. // TODO: Redo this.
	 */
	public static final String NO_PLAYER_MESSAGE = "&cYou can't execute the command. It may be reserved to the console, players or blocks.";
	
	
	/**
	 * Copies an array, starting at the given index
	 * 
	 * @param source The source array.
	 * @param startIndex The start index, to be included in the resulting array.
	 * @return The resulting array, or an empty one if startPos is >= source.length
	 */
	public static <T> T[] getArrayStartingAtPosition(T[] source, int startIndex) {
		if(startIndex < 0) {
			throw new IllegalArgumentException(
					"The start pos can't be smaller than 0. StartPos: " + startIndex);
		}
		if(source.length == 0) {
			return source;
		}
		
		@SuppressWarnings("unchecked")
		T[] argsToReturn = (T[]) Array.newInstance(source[0].getClass(), Math.max(source.length - startIndex, 0));
		
		if(argsToReturn.length > 0) {
			System.arraycopy(source, startIndex, argsToReturn, 0, argsToReturn.length);
		}
		
		return argsToReturn;
	}
	
	/**
	 * Colors the text with '&' as the color char
	 * 
	 * @param input The text to color
	 * @return The colored text
	 */
	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
}
