package me.ialistannen.nbt;

import me.ialistannen.bukkitutil.commandsystem.PluginMain;
import me.ialistannen.bukkitutil.commandsystem.util.ReflectionUtil;
import me.ialistannen.nbt.NBTWrappers.INBTBase;
import me.ialistannen.nbt.NBTWrappers.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Objects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * A utility to modify Entities NBT-tags. Uses reflection and scans through all methods to find the right ones,
 * so it might change in future releases.
 * <p>
 * The methods must only be called when at least one world is loaded, as it needs to spawn a sample entity (ArmorStand).
 * <br>It will be enforced by throwing an {@link IllegalStateException}.
 */
@SuppressWarnings("unused") // just by me...
public class EntityNBTUtil {

	private static Method loadFromNbtMethod, saveToNbtMethod, getHandle;
	private static boolean error = false;

	static {
		getLoadingMethods();
		Optional<Method> getHandleOpt = ReflectionUtil.getMethod(
				ReflectionUtil.getCraftbukkitClass("CraftEntity", "entity"),
				"getHandle");
		if (getHandleOpt.isPresent()) {
			getHandle = getHandleOpt.get();
		}
		else {
			PluginMain.getInstance().getLogger()
					.warning("getHandle not found: "
							+ Bukkit.getServer().getClass().getName());
			error = true;
		}
	}

	/**
	 * Gets the NMS handle of a bukkit entity
	 *
	 * @param entity The Bukkit entity
	 *
	 * @return The NMS entity
	 *
	 * @throws IllegalStateException if {@link #ensureNoError()} throws it
	 */
	private static Object toNMSEntity(Entity entity) {
		ensureNoError();
		return ReflectionUtil.invokeMethod(getHandle, entity);
	}

	/**
	 * @throws IllegalStateException If {@link #error} is true
	 */
	private static void ensureNoError() {
		if (error) {
			throw new IllegalStateException("A critical, non recoverable error occurred earlier.");
		}
	}

	/**
	 * Gets the NBT-Tag of an entity
	 *
	 * @param entity The entity to get the nbt tag for
	 *
	 * @return The NBTTag of the entity
	 *
	 * @throws NullPointerException  if {@code entity} is null
	 * @throws IllegalStateException if a critical, non recoverable error occurred earlier (loading methods).
	 */
	@SuppressWarnings("WeakerAccess") // util,...
	public static NBTTagCompound getNbtTag(Entity entity) {
		Objects.ensureNotNull(entity);

		ensureNoError();

		Object nmsEntity = toNMSEntity(entity);
		NBTTagCompound entityNBT = new NBTTagCompound();

		{
			Object nbtNMS = entityNBT.toNBT();
			ReflectionUtil.invokeMethod(saveToNbtMethod, nmsEntity, nbtNMS);
			if (nbtNMS == null) {
				throw new NullPointerException("SaveToNBT method nulled Nbt tag. Version incompatible?"
						+ nmsEntity.getClass());
			}
			entityNBT = (NBTTagCompound) INBTBase.fromNBT(nbtNMS);
		}

		return entityNBT;
	}

	/**
	 * Applies the {@link NBTTagCompound} tp the passed {@link Entity}
	 *
	 * @param entity   The entity to modify the nbt tag
	 * @param compound The {@link NBTTagCompound} to set it to
	 *
	 * @throws NullPointerException  if {@code entity} or {@code compound} is null
	 * @throws IllegalStateException if a critical, non recoverable error occurred earlier (loading methods).
	 */
	@SuppressWarnings("WeakerAccess") // util...
	public static void setNbtTag(Entity entity, NBTTagCompound compound) {
		Objects.ensureNotNull(entity);
		Objects.ensureNotNull(compound);

		ensureNoError();

		Object nmsEntity = toNMSEntity(entity);

		ReflectionUtil.invokeMethod(loadFromNbtMethod, nmsEntity, compound.toNBT());
	}

	/**
	 * Appends the {@link NBTTagCompound} to the entities NBT tag, overwriting already set values
	 *
	 * @param entity   The entity whose NbtTag to change
	 * @param compound The {@link NBTTagCompound} whose values you want to add
	 *
	 * @throws NullPointerException  if {@code entity} or {@code compound} is null
	 * @throws IllegalStateException if a critical, non recoverable error occurred earlier (loading methods).
	 */
	public static void appendNbtTag(Entity entity, NBTTagCompound compound) {
		// yes, getNbtTag would throw them as well.
		Objects.ensureNotNull(entity);
		Objects.ensureNotNull(compound);

		ensureNoError();

		NBTTagCompound entityData = getNbtTag(entity);

		for (Entry<String, INBTBase> entry : compound.getAllEntries().entrySet()) {
			entityData.set(entry.getKey(), entry.getValue());
		}

		setNbtTag(entity, entityData);
	}


	private static void getLoadingMethods() {
		if (Bukkit.getWorlds().isEmpty()) {
			throw new IllegalStateException("Called me before at least one world was loaded...");
		}
		Entity sample = Bukkit.getWorlds().get(0)
				.spawnEntity(Bukkit.getWorlds().get(0).getSpawnLocation(), EntityType.ARMOR_STAND);
		Object nmsSample = ReflectionUtil.invokeMethod(sample, "getHandle", new Class[0]);

		Class<?> entityClass = ReflectionUtil.getNMSClass("Entity");
		if (entityClass == null) {
			error = true;
			PluginMain.getInstance().getLogger()
					.warning("Couldn't find entity class");
			sample.remove();
			return;
		}

		if (ReflectionUtil.getMajorVersion() > 1 || ReflectionUtil.getMinorVersion() > 8) {
			initializeHigherThan1_9(entityClass, sample, nmsSample);
		}
		else {
			initializeLowerThan1_9(entityClass, sample, nmsSample);
		}


		if (saveToNbtMethod == null || loadFromNbtMethod == null) {
			PluginMain.getInstance().getLogger()
					.warning("Couldn't find the methods. This could help: "
							+ entityClass.getName()
							+ " save " + (saveToNbtMethod == null)
							+ " load " + (loadFromNbtMethod == null));
			error = true;
		}
		sample.remove();
	}

	private static void initializeHigherThan1_9(Class<?> entityClass, Entity sample, Object nmsSample) {
		// load the loading method
		initializeLowerThan1_9(entityClass, sample, nmsSample);

		for (Method method : entityClass.getMethods()) {
			// the save method : "public NBTTagCompound(final NBTTagCompound compound)"
			if (method.getReturnType().equals(ReflectionUtil.getNMSClass("NBTTagCompound"))
					&& method.getParameterTypes().length == 1
					&& method.getParameterTypes()[0].equals(ReflectionUtil.getNMSClass("NBTTagCompound"))
					&& Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())) {

				Object testCompound = new NBTTagCompound().toNBT();
				ReflectionUtil.invokeMethod(method, nmsSample, testCompound);

				NBTTagCompound compound = (NBTTagCompound) INBTBase.fromNBT(testCompound);

				if (compound == null) {
					continue;
				}

				if (!compound.isEmpty()) {
					if (saveToNbtMethod != null) {
						saveToNbtMethod = null;
						PluginMain.getInstance().getLogger()
								.warning("Couldn't find the saving method for an entity. This should help: "
										+ entityClass.getName());
						error = true;
						return;
					}
					saveToNbtMethod = method;
				}
			}
		}
	}

	private static void initializeLowerThan1_9(Class<?> entityClass, Entity sample, Object nmsSample) {

		for (Method method : entityClass.getMethods()) {
			if (method.getReturnType().equals(Void.TYPE)
					&& method.getParameterTypes().length == 1
					&& method.getParameterTypes()[0].equals(ReflectionUtil.getNMSClass("NBTTagCompound"))
					&& Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())) {

				Object testCompound = new NBTTagCompound().toNBT();
				ReflectionUtil.invokeMethod(method, nmsSample, testCompound);

				NBTTagCompound compound = (NBTTagCompound) INBTBase.fromNBT(testCompound);
				if (compound == null) {
					continue;
				}

				if (compound.isEmpty()) {
					if (loadFromNbtMethod != null) {
						PluginMain.getInstance().getLogger()
								.warning("Couldn't find the loading method for an entity. This should help: "
										+ entityClass.getName()
										+ " found methods: " + loadFromNbtMethod + " " + method);
						loadFromNbtMethod = null;
						error = true;
						return;
					}
					loadFromNbtMethod = method;
				}
				else {
					if (saveToNbtMethod != null) {
						PluginMain.getInstance().getLogger()
								.warning("Couldn't find the saving method for an entity. This should help: "
										+ entityClass.getName()
										+ " found methods: " + saveToNbtMethod + " " + method);
						error = true;
						saveToNbtMethod = null;
						return;
					}
					saveToNbtMethod = method;
				}
			}
		}
	}

}
