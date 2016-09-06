package me.ialistannen.nbt;

import me.ialistannen.bukkitutil.commandsystem.util.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

/**
 * A Util to save NBT data to ItemStacks
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemNBTUtil {

	private static final Class<?> CRAFT_ITEM_STACK_CLASS = ReflectionUtil
			.getCraftbukkitClass("CraftItemStack", "inventory");

	/**
	 * @param itemStack The {@link ItemStack} to convert
	 *
	 * @return The NMS Item stack
	 */
	private static Object asNMSCopy(ItemStack itemStack) {
		return ReflectionUtil.invokeMethod(CRAFT_ITEM_STACK_CLASS, null, "asNMSCopy",
				new Class[]{ItemStack.class}, itemStack);
	}

	/**
	 * Only pass a NMS Itemstack!
	 *
	 * @param nmsItem The NMS item to convert
	 *
	 * @return The converted Item
	 */
	private static ItemStack asBukkitCopy(Object nmsItem) {
		return (ItemStack) ReflectionUtil.invokeMethod(CRAFT_ITEM_STACK_CLASS, null, "asBukkitCopy",
				new Class[]{ReflectionUtil.getNMSClass("ItemStack")}, nmsItem);
	}

	/**
	 * Sets the NBT tag of an item
	 *
	 * @param tag       The new tag
	 * @param itemStack The ItemStack
	 *
	 * @return The modified itemStack
	 */
	public static ItemStack setNBTTag(NBTWrappers.NBTTagCompound tag, ItemStack itemStack) {
		Object nbtTag = tag.toNBT();
		Object nmsItem = asNMSCopy(itemStack);
		ReflectionUtil.invokeMethod(nmsItem, "setTag",
				new Class[]{ReflectionUtil.getNMSClass("NBTTagCompound")}, nbtTag);

		return asBukkitCopy(nmsItem);
	}

	/**
	 * Gets the NBTTag of an item. In case of any error it returns a blank one.
	 *
	 * @param itemStack The ItemStack to get the tag for
	 *
	 * @return The NBTTagCompound of the ItemStack or a new one if it had none or an error occurred
	 */
	public static NBTWrappers.NBTTagCompound getTag(ItemStack itemStack) {
		Object nmsItem = asNMSCopy(itemStack);
		Object tag = ReflectionUtil.invokeMethod(nmsItem, "getTag", new Class[0]);
		if (tag == null) {
			return new NBTWrappers.NBTTagCompound();
		}
		NBTWrappers.INBTBase base = NBTWrappers.INBTBase.fromNBT(tag);
		if (base == null || base.getClass() != NBTWrappers.NBTTagCompound.class) {
			return new NBTWrappers.NBTTagCompound();
		}

		return (NBTWrappers.NBTTagCompound) base;
	}
}

