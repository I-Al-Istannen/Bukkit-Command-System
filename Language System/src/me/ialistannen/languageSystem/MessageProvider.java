package me.ialistannen.languageSystem;

import java.nio.file.Path;
import java.util.Locale;

/**
 * A class which provides messages, in exchange for keys and a language.
 */
public interface MessageProvider {
	
	/**
	 * The directory the default files lie in
	 * 
	 * @param packageName The name of the package
	 */
	public void setDefaultFilesPackage(String packageName);
	
	/**
	 * Sets the path, where the user editable language files lie
	 * 
	 * @param path The path to the user editable language files
	 */
	public void setFileLocation(Path path);
	
	/**
	 * Sets the language
	 * 
	 * @param locale The new Locale
	 * @return The actual new locale set
	 */
	public Locale setLanguage(Locale locale);
	
	/**
	 * Gets the current language
	 * 
	 * @return The current language
	 */
	public Locale getLanguage();
	
	/**
	 * Translates a key
	 * 
	 * @param key The key
	 * @param formattingObjects The additional objects which replace parts of the message
	 * @return The resulting String
	 */
	public String tr(String key, Object... formattingObjects);
}
