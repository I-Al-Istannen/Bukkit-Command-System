package me.ialistannen.languageSystem;

import java.nio.file.Path;
import java.util.Locale;

/**
 * A class which provides messages, in exchange for keys and a language.
 */
@SuppressWarnings("unused")
public interface MessageProvider {

	/**
	 * The directory the default files lie in
	 *
	 * @param packageName The name of the package
	 */
	void setDefaultFilesPackage(String packageName);

	/**
	 * Sets the path, where the user editable language files lie
	 *
	 * @param path The path to the user editable language files
	 */
	void setFileLocation(Path path);

	/**
	 * Sets the language
	 *
	 * @param locale The new Locale
	 *
	 * @return The actual new locale set
	 */
	Locale setLanguage(Locale locale);

	/**
	 * Gets the current language
	 *
	 * @return The current language
	 */
	Locale getLanguage();

	/**
	 * Translates a key
	 *
	 * @param key               The key
	 * @param formattingObjects The additional objects which replace parts of the message
	 *
	 * @return The resulting String
	 */
	String tr(String key, Object... formattingObjects);

	/**
	 * Returns the translation or applies the translation to the given String
	 *
	 * @param key               The key to translate
	 * @param defaultString     The default string to apply it to
	 * @param formattingObjects The formatting objects to use
	 *
	 * @return The translated String
	 */
	String trOrDefault(String key, String defaultString, Object... formattingObjects);

	/**
	 * Checks whether the key exists
	 *
	 * @param key The key to check
	 *
	 * @return True if the key exists
	 */
	boolean containsKey(String key);
}
