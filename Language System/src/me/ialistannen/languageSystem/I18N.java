package me.ialistannen.languageSystem;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A class utilizing I18N to fulfill the {@link MessageProvider} contract
 */
public class I18N implements MessageProvider {

	/**
	 * The prefix for the logger
	 */
	private static final String LANGUAGE_LOG_PREFIX = "[Language System] ";

	/**
	 * Matches two double quotes. To be used to remove them from the translated strings, if they should occur.
	 */
	private static final Pattern REMOVE_DOUBLE_QUOTES = Pattern.compile("''");

	private Locale locale;
	private Logger logger;

	private String[] categories;
	private String defaultPackage;
	private Path defaultFilePath;


	private final Map<String, ResourceBundle> packageBundles = new LinkedHashMap<>();
	private final Map<String, ResourceBundle> fileBundles = new LinkedHashMap<>();

	private final Map<String, MessageFormat> formatCache = new HashMap<>();

	private FileClassLoader fileClassLoader;
	private ClassLoader defaultClassLoader;

	/**
	 * @param defaultPackage     The default fully-qualified package name
	 * @param defaultFilePath    The default file path
	 * @param locale             The default locale
	 * @param logger             The logger to use
	 * @param defaultClassLoader The classloader to use. Really ugly way, but otherwise it is not able to scan through
	 *                           the packages :/
	 * @param categories         The categories to add to the map. Will be the only files read and processed from the
	 *                           given parts.
	 */
	@SuppressWarnings("unused")
	public I18N(String defaultPackage, Path defaultFilePath, Locale locale, Logger logger, ClassLoader
			defaultClassLoader, String... categories) {
		if (categories.length < 1) {
			throw new IllegalArgumentException("Must specify at least one category.");
		}

		this.defaultClassLoader = defaultClassLoader;

		this.locale = Locale.ENGLISH;
		this.categories = categories;
		this.defaultFilePath = defaultFilePath;
		this.defaultPackage = defaultPackage;
		this.logger = logger;

		updateBundles();

		setLanguage(locale);
	}

	private void updateBundles() {
		formatCache.clear();
		if (fileClassLoader != null) {
			ResourceBundle.clearCache(fileClassLoader);
		}
		fileClassLoader = new FileClassLoader(defaultFilePath, defaultPackage);

		// Initialize the bundles
		for (String string : categories) {
			packageBundles.put(string, ResourceBundle.getBundle(defaultPackage + "." + string, getLanguage(),
					defaultClassLoader));
			fileBundles.put(string, ResourceBundle.getBundle(string, getLanguage(), fileClassLoader));
		}
	}

	@Override
	public Locale setLanguage(Locale locale) {
		if (!isAvailable(locale)) {
			logger.log(Level.WARNING, "Couldn't find the language: " + locale.getDisplayName());
			return getLanguage();
		}
		this.locale = locale;
		for (String string : packageBundles.keySet()) {
			packageBundles.put(string, ResourceBundle.getBundle(defaultPackage + "." + string, getLanguage(),
					defaultClassLoader));
			fileBundles.put(string, ResourceBundle.getBundle(string, getLanguage(), fileClassLoader));
		}
		return getLanguage();
	}

	@Override
	public Locale getLanguage() {
		return locale;
	}

	/**
	 * @param key      The key to get
	 * @param category The category the key is in
	 * @param objects  The formatting objects
	 *
	 * @return The formatted String
	 */
	private String format(String key, String category, Object... objects) {
		String format = translate(key, category);
		MessageFormat messageFormat = formatCache.get(format);
		if (messageFormat == null) {
			try {
				messageFormat = new MessageFormat(format);
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, LANGUAGE_LOG_PREFIX + "Invalid translation key for '" + key + "'", e);
				format = format.replaceAll("\\{(.*?)\\}", "\\[$1\\]");    // replace !all! Placeholders with []
				messageFormat = new MessageFormat(format);
			}
			formatCache.put(format, messageFormat);
		}

		return messageFormat.format(objects);
	}

	/**
	 * @param key      The key
	 * @param category The category of the key
	 *
	 * @return The translated String
	 */
	private String translate(String key, String category) {
		try {
			try {
				return fileBundles.get(category).getString(key);
			} catch (MissingResourceException e) {
				return packageBundles.get(category).getString(key);
			}
		} catch (MissingResourceException e) {
			//noinspection SpellCheckingInspection
			return "Couldn''t be translated. Key ''" + key + "'' not found";
		}
	}

	/**
	 * Defaults to the FIRST category
	 *
	 * @param key               The key to format
	 * @param formattingObjects The formatting objects
	 *
	 * @return The formatted String.
	 */
	@Override
	public String tr(String key, Object... formattingObjects) {
		return ChatColor.translateAlternateColorCodes('&', translate(key, categories[0], formattingObjects));
	}

	/**
	 * Uses the given category
	 *
	 * @param key               The key to format
	 * @param category          The category to translate from
	 * @param formattingObjects The formatting objects
	 *
	 * @return The formatted String.
	 */
	public String tr(String key, String category, Object... formattingObjects) {
		return ChatColor.translateAlternateColorCodes('&', translate(key, category, formattingObjects));
	}

	/**
	 * Returns the translation or applies the translation to the given String
	 *
	 * @param key               The key to translate
	 * @param defaultString     The default string to apply it to
	 * @param formattingObjects The formatting objects to use
	 *
	 * @return The translated String
	 */
	@Override
	public String trOrDefault(String key, String defaultString, Object... formattingObjects) {
		if (!containsKey(key)) {
			MessageFormat format;
			try {
				format = new MessageFormat(defaultString);
			} catch (IllegalArgumentException e) {
				format = new MessageFormat(defaultString.replaceAll("\\{(.+)\\}", "[$1]"));
			}
			return ChatColor.translateAlternateColorCodes('&', format.format(formattingObjects));
		}
		return tr(key, formattingObjects);
	}

	/**
	 * Checks whether the key exists
	 *
	 * @param key The key to check
	 *
	 * @return True if the key exists
	 */
	@Override
	public boolean containsKey(String key) {
		return containsKey(key, categories[0]);
	}

	/**
	 * Checks whether the key exists
	 *
	 * @param key      The key to check
	 * @param category The category
	 *
	 * @return True if the key exists
	 */
	private boolean containsKey(String key, String category) {
		try {
			try {
				fileBundles.get(category).getString(key);
				return true;
			} catch (MissingResourceException e) {
				packageBundles.get(category).getString(key);
				return true;
			}
		} catch (MissingResourceException e) {
			return false;
		}
	}

	/**
	 * @param key               The key to translate
	 * @param category          The category of the key
	 * @param formattingObjects The formatting objects
	 *
	 * @return The translated String. Uncolored.
	 */
	public String translate(String key, String category, Object... formattingObjects) {
		return REMOVE_DOUBLE_QUOTES.matcher(format(key, category, formattingObjects)).replaceAll("'");
	}

	@Override
	public void setDefaultFilesPackage(String packageName) {
		this.defaultPackage = packageName;
	}

	@Override
	public void setFileLocation(Path path) {
		this.defaultFilePath = path;
		updateBundles();
	}

	/**
	 * Reloads the language files in the !folder!
	 */
	@SuppressWarnings("unused") // That can come in handy
	public void reload() {
		ResourceBundle.clearCache(fileClassLoader);
		updateBundles();
	}

	/**
	 * @param locale The locale to check
	 *
	 * @return True if the language is available
	 */
	private boolean isAvailable(Locale locale) {
		for (String string : packageBundles.keySet()) {
			try {
				ResourceBundle packageBundle = ResourceBundle.getBundle(defaultPackage + "." + string, locale,
						defaultClassLoader);
				ResourceBundle fileBundle = ResourceBundle.getBundle(string, locale, fileClassLoader);
				if (!packageBundle.getLocale().equals(locale) || !fileBundle.getLocale().equals(locale)) {
					return false;
				}
			} catch (MissingResourceException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param defaultPackage The package they are in
	 * @param targetDir      The target directory
	 * @param overwrite      If the existing files should be overwritten.
	 * @param caller         The calling class. Used to distinguish the jar file if this class is provided by multiple
	 *                       plugins
	 *
	 * @return True if the files were written, false otherwise
	 */
	@SuppressWarnings("unused")
	public static boolean copyDefaultFiles(String defaultPackage, Path targetDir, boolean overwrite, Class<?> caller) {
		defaultPackage = defaultPackage.replace(".", "/");
		try {
			File jarFile = new File(caller.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!jarFile.getAbsolutePath().endsWith(".jar")) {
				return false;
			}
			// try for the resource here. Just to close it.
			try (JarFile file = new JarFile(jarFile)) {
				Enumeration<JarEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.getName().startsWith(defaultPackage)) {
						Path copyTo = targetDir.resolve(entry.getName().replace(defaultPackage + "/", ""));
						if (Files.exists(copyTo) && !overwrite) {
							continue;
						}
						Files.copy(file.getInputStream(entry), copyTo, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}

			return true;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * A classloader reading from a directory
	 */
	private static class FileClassLoader extends ClassLoader {

		private Path path;
		private String defaultPackage;

		/**
		 * @param path           The base path to read from
		 * @param defaultPackage The default package. Used for correctly mapping the two file structures. The path in
		 *                       the jar and outside.
		 */
		@SuppressWarnings("unused")
		public FileClassLoader(Path path, String defaultPackage) {
			if (!Files.isDirectory(path)) {
				throw new IllegalArgumentException("Path can only be a directory.");
			}
			this.path = path;
			this.defaultPackage = defaultPackage.replace(".", "/");
		}

		@Override
		public URL getResource(String name) {
			Path resourcePath = path.resolve(name.replace(defaultPackage + "/", ""));
			if (!Files.exists(resourcePath)) {
				return null;
			}
			try {
				return resourcePath.toUri().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			if (getResource(name) == null) {
				return null;
			}
			try {
				return Files.newInputStream(path.resolve(name.replace(defaultPackage + "/", "")), StandardOpenOption
						.READ);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
