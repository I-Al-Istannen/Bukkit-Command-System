package me.ialistannen.nbt;

import me.ialistannen.bukkitutil.commandsystem.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides wrapper objects to abstract the NBT versions. Probably way too complicated...
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NBTWrappers {

	/**
	 * A base class for the essential methods
	 */
	public static abstract class INBTBase {
		public INBTBase() {
		}

		abstract Object toNBT();

		/**
		 * @param nbtObject The NBT object
		 *
		 * @return The correct {@link INBTBase} or null if the tag is not supported
		 */
		public static INBTBase fromNBT(Object nbtObject) {
			switch (nbtObject.getClass().getSimpleName()) {
				case "NBTTagByte": {
					return NBTTagByte.fromNBT(nbtObject);
				}
				case "NBTTagShort": {
					return NBTTagShort.fromNBT(nbtObject);
				}
				case "NBTTagInt": {
					return NBTTagInt.fromNBT(nbtObject);
				}
				case "NBTTagLong": {
					return NBTTagLong.fromNBT(nbtObject);
				}
				case "NBTTagFloat": {
					return NBTTagFloat.fromNBT(nbtObject);
				}
				case "NBTTagDouble": {
					return NBTTagDouble.fromNBT(nbtObject);
				}
				case "NBTTagByteArray": {
					return NBTTagByteArray.fromNBT(nbtObject);
				}
				case "NBTTagIntArray": {
					return NBTTagIntArray.fromNBT(nbtObject);
				}
				case "NBTTagString": {
					return NBTTagString.fromNBT(nbtObject);
				}
				case "NBTTagCompound": {
					return NBTTagCompound.fromNBT(nbtObject);
				}
				case "NBTTagList": {
					return NBTTagList.fromNBT(nbtObject);
				}
			}
			return null;
		}
	}

	/**
	 * A NBTTagString
	 */
	public static class NBTTagString extends INBTBase {
		private static final Constructor<?> NBT_TAG_STRING_CONSTRUCTOR = ReflectionUtil
				.getConstructor(ReflectionUtil.getNMSClass("NBTTagString"), String.class);


		private String string;

		/**
		 * @param string The String value
		 */
		public NBTTagString(String string) {
			this.string = string;
		}

		/**
		 * @param string The new value
		 */
		public void setString(String string) {
			this.string = string;
		}

		/**
		 * @return The String value
		 */
		public String getString() {
			return string;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_STRING_CONSTRUCTOR, getString());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			return new NBTTagString((String) ReflectionUtil.getInstanceField(nbtObject, "data"));
		}

		@Override
		public String toString() {
			return "NBTTagString{" +
					"string='" + string + '\'' +
					'}';
		}
	}

	/**
	 * A NBTTagCompound
	 */
	public static class NBTTagCompound extends INBTBase {
		private static final Constructor<?> NBT_TAG_COMPOUND_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagCompound")
		);

		private Map<String, INBTBase> map = new HashMap<>();

		public void set(String key, INBTBase value) {
			map.put(key, value);
		}

		public void setByte(String key, byte value) {
			map.put(key, new NBTTagByte(value));
		}

		public void setShort(String key, short value) {
			map.put(key, new NBTTagShort(value));
		}

		public void setInt(String key, int value) {
			map.put(key, new NBTTagInt(value));
		}

		public void setLong(String key, long value) {
			map.put(key, new NBTTagLong(value));
		}

		public void setFloat(String key, float value) {
			map.put(key, new NBTTagFloat(value));
		}

		public void setDouble(String key, double value) {
			map.put(key, new NBTTagDouble(value));
		}

		public void setString(String key, String value) {
			map.put(key, new NBTTagString(value));
		}

		public void setByteArray(String key, byte[] value) {
			map.put(key, new NBTTagByteArray(value));
		}

		public void setIntArray(String key, int[] value) {
			map.put(key, new NBTTagIntArray(value));
		}

		public void setBoolean(String key, boolean value) {
			setByte(key, (byte) (value ? 1 : 0));
		}

		public boolean hasKey(String key) {
			return map.containsKey(key);
		}

		public boolean hasKeyOfType(String key, Class<? extends INBTBase> type) {
			return map.containsKey(key) && map.get(key).getClass() == type;
		}

		/**
		 * @param key The key
		 *
		 * @return The assigned {@link INBTBase} or null if none
		 */
		public INBTBase get(String key) {
			return map.get(key);
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public byte getByte(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagByte.class)) {
				return 0;
			}
			return ((NBTTagByte) get(key)).getAsByte();
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public short getShort(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagShort.class)) {
				return 0;
			}
			return ((NBTTagShort) get(key)).getAsShort();
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public int getInt(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagInt.class)) {
				return 0;
			}
			return ((NBTTagInt) get(key)).getAsInt();
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public long getLong(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagLong.class)) {
				return 0;
			}
			return ((NBTTagLong) get(key)).getAsLong();
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public float getFloat(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagFloat.class)) {
				return 0;
			}
			return ((NBTTagFloat) get(key)).getAsFloat();
		}

		/**
		 * @param key The key
		 *
		 * @return The number or 0 if not found.
		 */
		public double getDouble(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagDouble.class)) {
				return 0;
			}
			return ((NBTTagDouble) get(key)).getAsDouble();
		}

		/**
		 * @param key The key
		 *
		 * @return The String or null if not found.
		 */
		public String getString(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagString.class)) {
				return null;
			}
			return ((NBTTagString) get(key)).getString();
		}

		/**
		 * @param key The key
		 *
		 * @return The byte array or null if not found or wrong type.
		 */
		public byte[] getByteArray(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagByteArray.class)) {
				return null;
			}
			return ((NBTTagByteArray) get(key)).getValue();
		}

		/**
		 * @param key The key
		 *
		 * @return The byte int or null if not found or wrong type.
		 */
		public int[] getIntArray(String key) {
			if (!hasKey(key) || !hasKeyOfType(key, NBTTagIntArray.class)) {
				return null;
			}
			return ((NBTTagIntArray) get(key)).getValue();
		}

		/**
		 * @param key The key
		 *
		 * @return The boolean
		 */
		public boolean getBoolean(String key) {
			return getByte(key) != 0;
		}


		@Override
		public Object toNBT() {
			Object compound = ReflectionUtil.instantiate(NBT_TAG_COMPOUND_CONSTRUCTOR);

			for (Map.Entry<String, INBTBase> entry : map.entrySet()) {
				ReflectionUtil.invokeMethod(compound, "set", new Class[]{String.class,
						ReflectionUtil.getNMSClass("NBTBase")}, entry.getKey(), entry.getValue().toNBT());
			}

			return compound;
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Collection<String> keys = new HashSet<>();
			for (Method method : nbtObject.getClass().getMethods()) {
				if (Modifier.isPublic(method.getModifiers()) && Set.class.isAssignableFrom(method.getReturnType())) {
					@SuppressWarnings("unchecked")
					Collection<? extends String> collection = (Collection<? extends String>)
							ReflectionUtil.invokeMethod(method, nbtObject);
					if (collection != null) {
						keys.addAll(collection);
					}
				}
			}
			NBTTagCompound compound = new NBTTagCompound();

			for (String key : keys) {
				Object value = ReflectionUtil.invokeMethod(nbtObject, "get", new Class[]{String.class}, key);
				INBTBase base = INBTBase.fromNBT(value);
				if (base != null) {
					compound.set(key, base);
				}
			}

			return compound;
		}

		@Override
		public String toString() {
			return "NBTTagCompound{" +
					"map=" + map +
					'}';
		}
	}

	/**
	 * A NBTTagList.
	 */
	public static class NBTTagList extends INBTBase {
		private static final Constructor<?> NBT_TAG_LIST_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagList")
		);

		private List<INBTBase> list = new ArrayList<>();

		/**
		 * Adds the {@link INBTBase}, if the type of the list is correct or the list is empty
		 *
		 * @param base The {@link INBTBase} to add
		 *
		 * @return True if it was added.
		 */
		public boolean add(INBTBase base) {
			return isType(base.getClass()) && list.add(base);
		}

		/**
		 * @param type The type to check for
		 *
		 * @return True if the list is empty or this type
		 */
		public boolean isType(Class<? extends INBTBase> type) {
			return list.isEmpty() || list.get(0).getClass() == type;
		}


		@Override
		public Object toNBT() {
			Object nbtList = ReflectionUtil.instantiate(NBT_TAG_LIST_CONSTRUCTOR);
			for (INBTBase inbtBase : list) {
				ReflectionUtil.invokeMethod(nbtList, "add",
						new Class[]{ReflectionUtil.getNMSClass("NBTBase")}, inbtBase.toNBT());
			}
			return nbtList;
		}

		public static INBTBase fromNBT(Object nbtObject) {
			NBTTagList list = new NBTTagList();
			List<?> savedList = (List<?>) ReflectionUtil.getInstanceField(nbtObject, "list");
			if (savedList == null) {
				return list;
			}
			for (Object entry : savedList) {
				list.add(INBTBase.fromNBT(entry));
			}
			return list;
		}

		@Override
		public String toString() {
			return "NBTTagList{" +
					"list=" + list +
					'}';
		}
	}

	/**
	 * A number.
	 */
	public static abstract class INBTNumber extends INBTBase {
		/**
		 * @return The number as an int
		 */
		public int getAsInt() {
			return (int) Math.round(getAsDouble());
		}

		/**
		 * @return The number as a long.
		 */
		public long getAsLong() {
			return Math.round(getAsDouble());
		}

		/**
		 * @return The number as a double.
		 */
		public abstract double getAsDouble();

		/**
		 * @return The number as a float
		 */
		public float getAsFloat() {
			return (float) getAsDouble();
		}

		/**
		 * @return The number as a byte
		 */
		public byte getAsByte() {
			return (byte) getAsInt();
		}

		/**
		 * @return The number as a short
		 */
		public short getAsShort() {
			return (short) getAsInt();
		}

		/**
		 * Sets the value
		 *
		 * @param number The new value
		 */
		public abstract void set(Number number);
	}

	/**
	 * A NBTTagDouble
	 */
	public static class NBTTagDouble extends INBTNumber {
		private static final Constructor<?> NBT_TAG_DOUBLE_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagDouble"), double.class
		);

		private double value;

		/**
		 * @param value The Double value
		 */
		public NBTTagDouble(double value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.doubleValue();
		}

		/**
		 * @return The Double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_DOUBLE_CONSTRUCTOR, getAsDouble());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Double value = (Double) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagDouble"), double.class), nbtObject
			);
			return value == null ? new NBTTagDouble(-1) : new NBTTagDouble(value);
		}

		@Override
		public String toString() {
			return "NBTTagDouble{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * A NBTTagInt
	 */
	public static class NBTTagInt extends INBTNumber {
		private static final Constructor<?> NBT_TAG_INT_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagInt"), int.class
		);

		private int value;

		/**
		 * @param value The Int value
		 */
		public NBTTagInt(int value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.intValue();
		}

		/**
		 * @return The double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_INT_CONSTRUCTOR, getAsInt());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Integer value = (Integer) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagInt"), int.class), nbtObject
			);

			return new NBTTagInt(value == null ? 0 : value);
		}

		@Override
		public String toString() {
			return "NBTTagInt{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * A NBTTagIntArray
	 */
	public static class NBTTagIntArray extends INBTBase {
		private static final Constructor<?> NBT_TAG_INT_ARRAY_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagIntArray"), int[].class
		);

		private int[] value;

		public NBTTagIntArray() {
		}

		/**
		 * @param value The Int value
		 */
		public NBTTagIntArray(int[] value) {
			this.value = value;
		}

		/**
		 * @return The saved integer array
		 */
		public int[] getValue() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_INT_ARRAY_CONSTRUCTOR, (Object) getValue());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			int[] data = null;
			for (Method method : nbtObject.getClass().getMethods()) {
				if (method.getReturnType() == int[].class) {
					data = (int[]) ReflectionUtil.invokeMethod(method, nbtObject);
				}
			}
			return new NBTTagIntArray(data);
		}

		@Override
		public String toString() {
			return "NBTTagIntArray{" +
					"value=" + Arrays.toString(value) +
					'}';
		}
	}

	/**
	 * A NBTTagByte
	 */
	public static class NBTTagByte extends INBTNumber {
		private static final Constructor<?> NBT_TAG_BYTE_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagByte"), byte.class
		);

		private byte value;

		/**
		 * @param value The Byte value
		 */
		public NBTTagByte(byte value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.byteValue();
		}

		/**
		 * @return The double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_BYTE_CONSTRUCTOR, getAsByte());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Byte value = (Byte) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagByte"), byte.class), nbtObject
			);
			return new NBTTagByte(value == null ? 0 : value);
		}

		@Override
		public String toString() {
			return "NBTTagByte{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * A NBTTagByteArray
	 */
	public static class NBTTagByteArray extends INBTBase {
		private static final Constructor<?> NBT_TAG_BYTE_ARRAY_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagByteArray"), byte[].class
		);

		private byte[] value;

		public NBTTagByteArray() {
		}

		/**
		 * @param value The Byte value
		 */
		public NBTTagByteArray(byte[] value) {
			this.value = value;
		}

		/**
		 * @return The saved bytes
		 */
		public byte[] getValue() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_BYTE_ARRAY_CONSTRUCTOR, (Object) getValue());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			byte[] data = null;
			for (Method method : nbtObject.getClass().getMethods()) {
				if (method.getReturnType() == byte[].class) {
					data = (byte[]) ReflectionUtil.invokeMethod(method, nbtObject);
				}
			}
			return new NBTTagByteArray(data);
		}

		@Override
		public String toString() {
			return "NBTTagByteArray{" +
					"value=" + Arrays.toString(value) +
					'}';
		}
	}

	/**
	 * A NBTTagShort
	 */
	public static class NBTTagShort extends INBTNumber {
		private static final Constructor<?> NBT_TAG_SHORT_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagShort"), short.class
		);

		private short value;

		/**
		 * @param value The Short value
		 */
		public NBTTagShort(short value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.shortValue();
		}

		/**
		 * @return The double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_SHORT_CONSTRUCTOR, getAsShort());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Short value = (Short) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagShort"), short.class), nbtObject
			);
			return new NBTTagShort(value == null ? 0 : value);
		}

		@Override
		public String toString() {
			return "NBTTagShort{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * A NBTTagLong
	 */
	public static class NBTTagLong extends INBTNumber {
		private static final Constructor<?> NBT_TAG_LONG_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagLong"), long.class
		);

		private long value;

		/**
		 * @param value The Long value
		 */
		public NBTTagLong(long value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.longValue();
		}

		/**
		 * @return The double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_LONG_CONSTRUCTOR, getAsLong());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Long value = (Long) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagLong"), long.class), nbtObject
			);
			return new NBTTagLong(value == null ? 0 : value);
		}

		@Override
		public String toString() {
			return "NBTTagLong{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * A NBTTagFloat
	 */
	public static class NBTTagFloat extends INBTNumber {
		private static final Constructor<?> NBT_TAG_LONG_CONSTRUCTOR = ReflectionUtil.getConstructor(
				ReflectionUtil.getNMSClass("NBTTagFloat"), float.class
		);

		private float value;

		/**
		 * @param value The Float value
		 */
		public NBTTagFloat(float value) {
			this.value = value;
		}

		/**
		 * @param value The new value
		 */
		@Override
		public void set(Number value) {
			this.value = value.floatValue();
		}

		/**
		 * @return The double value
		 */
		@Override
		public double getAsDouble() {
			return value;
		}

		@Override
		public Object toNBT() {
			return ReflectionUtil.instantiate(NBT_TAG_LONG_CONSTRUCTOR, getAsFloat());
		}

		public static INBTBase fromNBT(Object nbtObject) {
			Float value = (Float) ReflectionUtil.invokeMethod(
					findNBTNumberGetMethod(ReflectionUtil.getNMSClass("NBTTagFloat"), float.class), nbtObject
			);
			return new NBTTagFloat(value == null ? 0 : value);
		}

		@Override
		public String toString() {
			return "NBTTagFloat{" +
					"value=" + value +
					'}';
		}
	}

	/**
	 * Returns the method also existing in the Superclass
	 *
	 * @param clazz       The Class to invoke it on
	 * @param returnClass The Return class it should have
	 *
	 * @return The found Method
	 */
	private static Method findNBTNumberGetMethod(Class<?> clazz, Class<?> returnClass) {
		for (Method method : clazz.getMethods()) {
			if (!method.getReturnType().equals(returnClass)) {
				continue;
			}
			if (method.getName().equals("hashCode")) {
				continue;
			}
			try {
				//noinspection ConfusingArgumentToVarargsMethod // The array is desired.
				clazz.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
				return method;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
