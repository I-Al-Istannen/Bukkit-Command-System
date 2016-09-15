package me.ialistannen.other;

import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil.color;

/**
 * Pages something.
 */
@SuppressWarnings("unused")
public class Pager {

	/**
	 * Returns the wanted page.
	 * <p>The different language keys are explained here: {@link Page#send(CommandSender, MessageProvider)}
	 *
	 * @param options The options.
	 * @param all     All the Strings
	 *
	 * @return The resulting page
	 */
	@NotNull
	public static Page getPageFromStrings(@NotNull Options options, @NotNull List<String> all) {
		return getPageFromFilterable(options, all.stream().map(StringFilterable::new).collect(Collectors.toList()));
	}

	/**
	 * Returns the wanted page.
	 * <p>The different language keys are explained here: {@link Page#send(CommandSender, MessageProvider)}
	 *
	 * @param options The options.
	 * @param all     All the Strings
	 *
	 * @return The resulting page
	 */
	@SuppressWarnings("WeakerAccess")
	@NotNull
	public static Page getPageFromFilterable(@NotNull Options options, @NotNull List<PagerFilterable> all) {
		List<PagerFilterable> list = filter(options, all);
		return slice(list, options.getEntriesPerPage(), options.getPageIndex());
	}

	/**
	 * Returns the page out of the list.
	 *
	 * @param all            All of the Strings
	 * @param entriesPerPage The entries per page
	 * @param pageIndex      Zero based page number. Will be corrected if too small or big.
	 *
	 * @return The resulting page
	 */
	@NotNull
	private static Page slice(@NotNull List<PagerFilterable> all, int entriesPerPage, int pageIndex) {
		int pageAmount = (int) Math.ceil(all.size() / (double) entriesPerPage);

		if (pageAmount == 0) {
			return new Page(1, 0, Collections.emptyList());
		}

		if (pageIndex < 0 || pageIndex >= pageAmount) {
			pageIndex = pageIndex < 0 ? 0 : pageAmount - 1;
		}

		List<PagerFilterable> entries = all.subList(
				pageIndex * entriesPerPage,
				Math.min((pageIndex + 1) * entriesPerPage, all.size()));

		return new Page(pageAmount, pageIndex,
				entries.stream()
						.flatMap(filterable -> filterable.getAllLines().stream())
						.collect(Collectors.toList()));
	}

	/**
	 * @param options The options to use
	 * @param all     All the {@link PagerFilterable} to filter
	 *
	 * @return The filtered list
	 */
	private static List<PagerFilterable> filter(Options options, @NotNull List<PagerFilterable> all) {
		return all.stream().filter(pagerFilterable -> pagerFilterable.accepts(options)).collect(Collectors.toList());
	}

	/**
	 * An object filterable by the Pager
	 */
	public interface PagerFilterable {
		/**
		 * @param options The options to use
		 *
		 * @return True if this object should pass
		 */
		boolean accepts(Options options);

		/**
		 * @return All the lines this object has
		 */
		@NotNull List<String> getAllLines();
	}

	/**
	 * A small wrapper for a normal String
	 */
	private static class StringFilterable implements PagerFilterable {
		private String string;

		/**
		 * @param string The String
		 */
		private StringFilterable(String string) {
			this.string = string;
		}

		@Override
		public boolean accepts(@NotNull Options options) {
			return options.matchesPattern(string);
		}

		@NotNull
		@Override
		public List<String> getAllLines() {
			return Collections.singletonList(string);
		}
	}

	/**
	 * The options class. Use the {@link Options.Builder} class to obtain one ({@link #builder()}).
	 */
	@SuppressWarnings("WeakerAccess")
	public static class Options {
		private int entriesPerPage;
		private int pageIndex;
		private Set<SearchMode> searchModes;
		private String searchPattern;

		private Options(int entriesPerPage, int pageIndex,
		                @NotNull Set<SearchMode> searchModes, String searchPattern) {

			this.entriesPerPage = entriesPerPage;
			this.pageIndex = pageIndex;
			this.searchModes = EnumSet.copyOf(searchModes);
			this.searchPattern = searchPattern;
		}

		/**
		 * The amount of entries on one page
		 *
		 * @return The entries per page
		 */
		public int getEntriesPerPage() {
			return entriesPerPage;
		}

		/**
		 * The index of the page
		 *
		 * @return The index of the page
		 */
		public int getPageIndex() {
			return pageIndex;
		}

		/**
		 * Checks if the String is accepted by the search pattern
		 *
		 * @param test The String to test
		 *
		 * @return True if the string matched one (or more) pattern(s)
		 */
		public boolean matchesPattern(String test) {
			return searchModes.stream().anyMatch(mode -> mode.accepts(test, searchPattern));
		}

		/**
		 * Creates a new Builder
		 *
		 * @return The Builder
		 */
		@NotNull
		public static Builder builder() {
			return new Builder();
		}

		@Override
		public String toString() {
			return "Options{" +
					"entriesPerPage=" + entriesPerPage +
					", pageIndex=" + pageIndex +
					", searchModes=" + searchModes +
					", searchPattern='" + searchPattern + '\'' +
					'}';
		}

		/**
		 * The Builder of the {@link Options} object.
		 */
		public static final class Builder {

			private int entriesPerPage = 10;
			private int pageIndex = 0;
			private Set<SearchMode> searchModes = EnumSet.of(SearchMode.CONTAINS);
			private String searchPattern = "";

			/**
			 * No instantiation from outside
			 */
			private Builder() {
			}

			/**
			 * The entries per page
			 *
			 * @param entriesPerPage The entries per page
			 *
			 * @return This Builder
			 */
			@NotNull
			public Builder setEntriesPerPage(int entriesPerPage) {
				this.entriesPerPage = entriesPerPage;

				return this;
			}

			/**
			 * The index of the page. 0 - max pages
			 *
			 * @param pageIndex The page index
			 *
			 * @return This Builder
			 */
			@NotNull
			public Builder setPageIndex(int pageIndex) {
				this.pageIndex = pageIndex;

				return this;
			}

			/**
			 * Sets the {@link SearchMode}s. If any of these match, it will be shown.
			 *
			 * @param searchModes The {@link SearchMode}s. Must not be empty.
			 *
			 * @return This Builder
			 *
			 * @throws IllegalArgumentException if searchModes is empty.
			 * @throws NullPointerException     if searchModes is null
			 */
			@NotNull
			public Builder setSearchModes(@NotNull Set<SearchMode> searchModes) {
				Objects.requireNonNull(searchModes);

				if (searchModes.isEmpty()) {
					throw new IllegalArgumentException("searchModes is empty");
				}
				this.searchModes = EnumSet.copyOf(searchModes);

				return this;
			}

			/**
			 * Sets the {@link SearchMode}s. If any of these match, it will be shown.
			 *
			 * @param first The first search mode
			 * @param rest  The other search modes
			 *
			 * @return This Builder
			 *
			 * @throws NullPointerException if first or rest is null
			 * @see #setSearchModes(Set)
			 */
			@NotNull
			public Builder setSearchModes(@NotNull SearchMode first, SearchMode... rest) {
				Objects.requireNonNull(first);
				Objects.requireNonNull(rest);

				setSearchModes(EnumSet.of(first, rest));

				return this;
			}

			/**
			 * Adds a {@link SearchMode}. If any of these match, it will be shown.
			 *
			 * @param mode The {@link SearchMode} to add
			 *
			 * @return This Builder
			 *
			 * @throws NullPointerException if mode is null
			 */
			@NotNull
			public Builder addSearchMode(SearchMode mode) {
				Objects.requireNonNull(mode);
				searchModes.add(mode);

				return this;
			}

			/**
			 * The pattern to search. Will be searched for using the specified {@link SearchMode}s
			 *
			 * @param searchPattern The pattern to search
			 *
			 * @return This Builder
			 */
			@NotNull
			public Builder setSearchPattern(String searchPattern) {
				this.searchPattern = searchPattern;

				return this;
			}

			/**
			 * Builds the options.
			 *
			 * @return The resulting Options
			 */
			@NotNull
			public Options build() {
				return new Options(entriesPerPage, pageIndex, searchModes, searchPattern);
			}
		}
	}

	/**
	 * The search mode
	 */
	@SuppressWarnings("WeakerAccess")
	public enum SearchMode {
		/**
		 * The string is contained
		 */
		CONTAINS(String::contains),
		/**
		 * The string is contained, ignoring case
		 */
		CONTAINS_IGNORE_CASE((test, pattern) -> test.toLowerCase().contains(pattern.toLowerCase())),
		/**
		 * The strings are equal
		 */
		EQUALS(String::equals),
		/**
		 * The strings are equal, ignoring case
		 */
		EQUALS_IGNORE_CASE(String::equalsIgnoreCase),
		/**
		 * The regular expression matches
		 */
		REGEX_MATCHES(String::matches),
		/**
		 * The regular expression matches, no matter the case
		 */
		REGEX_MATCHES_CASE_INSENSITIVE((test, pattern) -> Pattern
				.compile(pattern, Pattern.CASE_INSENSITIVE)
				.matcher(test)
				.matches()),
		/**
		 * The regular expression can be found in the string
		 */
		REGEX_FIND((test, pattern) -> Pattern
				.compile(pattern)
				.matcher(test)
				.find()),
		/**
		 * The regular expression can be found in the string, no matter the case
		 */
		REGEX_FIND_CASE_INSENSITIVE((test, pattern) -> Pattern
				.compile(pattern, Pattern.CASE_INSENSITIVE)
				.matcher(test)
				.find());

		/**
		 * The first one is the String to test, the second the pattern
		 */
		private BiFunction<String, String, Boolean> accept;

		/**
		 * @param accept Whether the String is accepted, using the second param as pattern
		 */
		SearchMode(BiFunction<String, String, Boolean> accept) {
			this.accept = accept;
		}

		/**
		 * Checks if this {@link SearchMode} matches a String
		 *
		 * @param string  The String to test
		 * @param pattern The pattern to match against
		 *
		 * @return True if it matches using this {@link SearchMode}
		 */
		public boolean accepts(String string, String pattern) {
			return accept.apply(string, pattern);
		}
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
		private Page(int maxPages, int pageIndex, List<String> entries) {
			this.maxPages = maxPages;
			this.pageIndex = pageIndex;
			this.entries = entries;
		}

		/**
		 * Returns all the entries of the page
		 *
		 * @return The entries of the page. Unmodifiable
		 */
		@NotNull
		public List<String> getEntries() {
			return Collections.unmodifiableList(entries);
		}

		/**
		 * Returns the index of this page
		 *
		 * @return The index of this page
		 */
		public int getPageIndex() {
			return pageIndex;
		}

		/**
		 * Returns the number of pages
		 *
		 * @return The amount of pages
		 */
		public int getMaxPages() {
			return maxPages;
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
		 * <li>"command_help_padding_char" ==> The padding char for the children. Will be repeated twice per
		 * level</li>
		 * </ul></li>
		 * </ul>
		 *
		 * @param sender   The {@link CommandSender} to send to
		 * @param language The {@link MessageProvider} to use
		 */
		@SuppressWarnings("WeakerAccess")
		public void send(@NotNull CommandSender sender, @NotNull MessageProvider language) {
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
