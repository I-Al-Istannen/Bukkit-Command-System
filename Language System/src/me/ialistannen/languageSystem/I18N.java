package me.ialistannen.languageSystem;

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
	 * Matches two double quotes. To be used to remove them from the translated strings, if they should occure.
	 */
	private static final Pattern REMOVE_DOUBLE_QUOTES = Pattern.compile("''");
	
	private Locale locale;
	private Logger logger;
	
	private String[] categories;
	private String defaultPackage;
	private Path defaultFilePath;
	
	
	private Map<String, ResourceBundle> packageBundles = new LinkedHashMap<>();
	private Map<String, ResourceBundle> fileBundles = new LinkedHashMap<>();
	
	private Map<String, MessageFormat> formatCache = new HashMap<>();
	
	private FileClassLoader fileClassLoader;
	
	/**
	 * @param defaultPackage The default fully-qualified package name
	 * @param defaultFilePath The default file path
	 * @param locale The default locale
	 * @param logger The logger to use
	 * @param categories The categories to add to the map. Will be the only files read and processed from the given parts.
	 */
	public I18N(String defaultPackage, Path defaultFilePath, Locale locale, Logger logger, String... categories) {
		if(categories.length < 1) {
			throw new IllegalArgumentException("Must specify at least one category.");
		}
		
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
		if(fileClassLoader != null) {
			ResourceBundle.clearCache(fileClassLoader);
		}
		fileClassLoader = new FileClassLoader(defaultFilePath, defaultPackage);
		
		// initalize the bundles
		for (String string : categories) {
			packageBundles.put(string, ResourceBundle.getBundle(defaultPackage + "." + string, getLanguage()));
			fileBundles.put(string, ResourceBundle.getBundle(string, getLanguage(), fileClassLoader));
		}
	}
	
	@Override
	public Locale setLanguage(Locale locale) {
		if(!isAvailable(locale)) {
			logger.log(Level.WARNING, "Couldn't find the language: " + locale.getDisplayName());
			return getLanguage();
		}
		this.locale = locale;
		for (String string : packageBundles.keySet()) {
			packageBundles.put(string, ResourceBundle.getBundle(defaultPackage + "." + string, getLanguage()));
			fileBundles.put(string, ResourceBundle.getBundle(string, getLanguage(), fileClassLoader));
		}
		return getLanguage();
	}

	@Override
	public Locale getLanguage() {
		return locale;
	}
	
	/**
	 * @param key The key to get
	 * @param category The category the key is in
	 * @param objects The formatting objects
	 * @return The formatted String
	 */
	private String format(String key, String category, Object... objects) {
		String format = translate(key, category);
		MessageFormat messageFormat = formatCache.get(format);
		if(messageFormat == null) {
			try {
				messageFormat = new MessageFormat(format);
			} catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, LANGUAGE_LOG_PREFIX + "Invalid translation key for '" + key + "'", e);
				format = format.replaceAll("\\{(.*?)\\}", "\\[$1\\]");	// replace !all! Placeholders with []
				messageFormat = new MessageFormat(format);
			}
			formatCache.put(format, messageFormat);
		}
		
		return messageFormat.format(objects);
	}
	
	/**
	 * @param key The key
	 * @param category The category of the key
	 * @return The translated String
	 */
	private String translate(String key, String category) {
		try {
			try {
				return fileBundles.get(category).getString(key);
			} catch(MissingResourceException e) {
				return packageBundles.get(category).getString(key);
			}
		} catch(MissingResourceException e) {
			return "Couldn''t be translated. Key ''" + key + "'' not found";
		}
	}

	/**
	 * Defaults to the FIRST category
	 * 
	 * @param key The key to format
	 * @param formattingObjects The formatting objects
	 * @return The formatted String.
	 */
	@Override
	public String tr(String key, Object... formattingObjects) {
		return REMOVE_DOUBLE_QUOTES.matcher(format(key, categories[0], formattingObjects)).replaceAll("'");
	}
	
	/**
	 * @param key The key to translate
	 * @param category The category of the key
	 * @param formattingObjects The formatting objects
	 * @return The translated String.
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
	 * @param locale The locale to check
	 * @return True if the language is available
	 */
	private boolean isAvailable(Locale locale) {
		for (String string : packageBundles.keySet()) {
			try {
				ResourceBundle packageBundle = ResourceBundle.getBundle(defaultPackage + "." + string, locale);
				ResourceBundle fileBundle = ResourceBundle.getBundle(string, getLanguage(), fileClassLoader);
				return packageBundle.getLocale().equals(locale) || fileBundle.getLocale().equals(locale);
			} catch(MissingResourceException e) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * @param defaultPackage The package they are in
	 * @param targetDir The target directory
	 * @param overwrite If the existing files should be overwritten.
	 * @return True if the files were written, false otherwise
	 */
	public static boolean copyDefaultFiles(String defaultPackage, Path targetDir, boolean overwrite) {
		defaultPackage = defaultPackage.replace(".", "/");
		try {
			File jarFile = new File(I18N.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if(!jarFile.getAbsolutePath().endsWith(".jar")) {
				return false;
			}
			// try for the resource here. Just to close it.
			try (JarFile file = new JarFile(jarFile)) {
				Enumeration<JarEntry> entries = file.entries();
				while(entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if(entry.getName().startsWith(defaultPackage)) {
						Path copyTo = targetDir.resolve(entry.getName().replace(defaultPackage + "/", ""));
						if(Files.exists(copyTo) && !overwrite) {
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
		 * @param path The base path to read from
		 * @param defaultPackage The default package. Used for correctly mapping the two file structures. The path in the jar and outside.
		 */
		public FileClassLoader(Path path, String defaultPackage) {
			if(!Files.isDirectory(path)) {
				throw new IllegalArgumentException("Path can only be a directory.");
			}
			this.path = path;
			this.defaultPackage = defaultPackage.replace(".", "/");
		}
		
		@Override
		public URL getResource(String name) {
			Path resourcePath = path.resolve(name.replace(defaultPackage + "/", ""));
			if(!Files.exists(resourcePath)) {
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
			if(getResource(name) == null) {
				return null;
			}
			try {
				return Files.newInputStream(path.resolve(name.replace(defaultPackage + "/", "")), StandardOpenOption.READ);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
