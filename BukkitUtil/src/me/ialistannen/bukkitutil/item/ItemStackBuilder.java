package me.ialistannen.bukkitutil.item;

import me.ialistannen.bukkitutil.commandsystem.util.CommandSystemUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An itemstack builder
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemStackBuilder {

	/**
	 * Colourable materials
	 */
	private static final Set<Material> COLOURABLE = EnumSet.of(Material.WOOL, Material.STAINED_CLAY,
			Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.CARPET);

	private ItemStack item;
	private ItemMeta itemMeta;

	private ItemStackBuilder(Material material) {
		item = new ItemStack(material);
		itemMeta = item.getItemMeta();
	}

	private ItemStackBuilder(ItemStack item) {
		this.item = item.clone();
		itemMeta = item.getItemMeta().clone();
	}

	/**
	 * @param mat The Material of the itemstack
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setType(Material mat) {
		item.setItemMeta(itemMeta);
		item.setType(mat);
		itemMeta = item.getItemMeta();
		return this;
	}

	/**
	 * @param amount The amount of the item
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setAmount(int amount) {
		item.setAmount(amount);
		return this;
	}

	/**
	 * @param lore The Lore
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setLore(List<String> lore) {
		itemMeta.setLore(lore.stream().map(CommandSystemUtil::color).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return this;
	}

	/**
	 * @param lore The Lore
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setLore(String... lore) {
		return setLore(Arrays.asList(lore));
	}

	/**
	 * @param loreLine The next line of the lore.
	 *
	 * @return This builder
	 */
	public ItemStackBuilder addLore(String loreLine) {
		List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
		lore.add(loreLine);
		return setLore(lore);
	}

	/**
	 * @param name The name of the item
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setName(String name) {
		itemMeta.setDisplayName(CommandSystemUtil.color(name));
		item.setItemMeta(itemMeta);
		return this;
	}

	/**
	 * Sets the author of a book
	 *
	 * @param name The name of the author
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setAuthor(String name) {
		if (item.getType() != Material.WRITTEN_BOOK && item.getType() != Material.BOOK_AND_QUILL) {
			return this;
		}

		BookMeta meta = (BookMeta) itemMeta;
		meta.setAuthor(name);
		item.setItemMeta(meta);

		return this;
	}

	/**
	 * Only colors if this item is colourable. else does nothing
	 *
	 * @param color The Color of the item
	 *
	 * @return This builder
	 */
	@SuppressWarnings("deprecation")
	public ItemStackBuilder setColor(DyeColor color) {
		if (!COLOURABLE.contains(item.getType())) {
			return this;
		}
		item.setDurability(color.getData());
		return this;
	}

	/**
	 * @param durability The durability
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setDurability(short durability) {
		item.setDurability(durability);
		return this;
	}

	/**
	 * Automatically uses addUnsafeEnchantment if needed.
	 *
	 * @param enchantment The enchantment to add
	 * @param level       The level of the enchantment
	 *
	 * @return This builder
	 */
	public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
		if (level <= 0) {
			return this;
		}

		if (level <= enchantment.getMaxLevel() && enchantment.canEnchantItem(item)) {
			item.addEnchantment(enchantment, level);
		}
		else {
			item.addUnsafeEnchantment(enchantment, level);
		}

		itemMeta = item.getItemMeta().clone();

		return this;
	}

	/**
	 * Only sets the owner if this Material is a skull
	 *
	 * @param name The Skull owner
	 *
	 * @return This builder
	 */
	public ItemStackBuilder setSkullOwner(String name) {
		if (item.getType() != Material.SKULL_ITEM) {
			return this;
		}

		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(name);
		item.setItemMeta(meta);
		itemMeta = item.getItemMeta();

		return this;
	}

	/**
	 * @return The resulting ItemStack
	 */
	public ItemStack build() {
		item.setItemMeta(itemMeta);
		return item;
	}

	/**
	 * @param material The Material to use for the stack
	 *
	 * @return A new {@link ItemStackBuilder}
	 */
	public static ItemStackBuilder builder(Material material) {
		return new ItemStackBuilder(material);
	}

	/**
	 * @param itemStack The Item to build upon
	 *
	 * @return A new {@link ItemStackBuilder}
	 */
	public static ItemStackBuilder builder(ItemStack itemStack) {
		return new ItemStackBuilder(itemStack);
	}
}
