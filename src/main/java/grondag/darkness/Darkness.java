/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.darkness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import net.fabricmc.loader.api.FabricLoader;

public class Darkness {
	public static Logger LOG = LogManager.getLogger("Darkness");

	static boolean darkOverworld;
	static boolean darkDefault;
	static boolean darkNether;
	static double darkNetherFogEffective;
	static double darkNetherFogConfigured;
	static boolean darkEnd;
	static double darkEndFogEffective;
	static double darkEndFogConfigured;
	static boolean darkSkyless;
	static boolean blockLightOnly;
	static boolean ignoreMoonPhase;

	static {
		final File configFile = getConfigFile();
		final Properties properties = new Properties();

		if (configFile.exists()) {
			try (FileInputStream stream = new FileInputStream(configFile)) {
				properties.load(stream);
			} catch (final IOException e) {
				LOG.warn("[Darkness] Could not read property file '" + configFile.getAbsolutePath() + "'", e);
			}
		}

		ignoreMoonPhase = properties.computeIfAbsent("ignore_moon_phase", (a) -> "false").equals("true");
		blockLightOnly = properties.computeIfAbsent("only_affect_block_light", (a) -> "false").equals("true");
		darkOverworld = properties.computeIfAbsent("dark_overworld", (a) -> "true").equals("true");
		darkDefault = properties.computeIfAbsent("dark_default", (a) -> "true").equals("true");
		darkNether = properties.computeIfAbsent("dark_nether", (a) -> "true").equals("true");
		darkEnd = properties.computeIfAbsent("dark_end", (a) -> "true").equals("true");
		darkSkyless = properties.computeIfAbsent("dark_skyless", (a) -> "true").equals("true");

		try {
			darkNetherFogConfigured = Double.parseDouble(properties.computeIfAbsent("dark_nether_fog", (a) -> "0.5").toString());
			darkNetherFogConfigured = MathHelper.clamp(darkNetherFogConfigured, 0.0, 1.0);
		} catch (final Exception e) {
			darkNetherFogConfigured = 0.5;
			LOG.warn("[Darkness] Invalid configuration value for 'dark_nether_fog'. Using default value.");
		}


		try {
			darkEndFogConfigured = Double.parseDouble(properties.computeIfAbsent("dark_end_fog", (a) -> "0.0").toString());
			darkEndFogConfigured = MathHelper.clamp(darkEndFogConfigured, 0.0, 1.0);
		} catch (final Exception e) {
			darkEndFogConfigured = 0.0;
			LOG.warn("[Darkness] Invalid configuration value for 'dark_end_fog'. Using default value.");
		}

		computeConfigValues();

		saveConfig();
	}

	private static void computeConfigValues()  {
		darkNetherFogEffective = darkNether ? darkNetherFogConfigured : 1.0;
		darkEndFogEffective = darkEnd ? darkEndFogConfigured : 1.0;
	}

	private static File getConfigFile() {
		final File configDir = FabricLoader.getInstance().getConfigDirectory();
		if (!configDir.exists()) {
			LOG.warn("[Darkness] Could not access configuration directory: " + configDir.getAbsolutePath());
		}

		return  new File(configDir, "darkness.properties");
	}

	public static void saveConfig() {
		final File configFile = getConfigFile();
		final Properties properties = new Properties();

		properties.put("only_affect_block_light", Boolean.toString(blockLightOnly));
		properties.put("ignore_moon_phase", Boolean.toString(ignoreMoonPhase));
		properties.put("dark_overworld", Boolean.toString(darkOverworld));
		properties.put("dark_default", Boolean.toString(darkDefault));
		properties.put("dark_nether", Boolean.toString(darkNether));
		properties.put("dark_nether_fog", Double.toString(darkNetherFogConfigured));
		properties.put("dark_end", Boolean.toString(darkEnd));
		properties.put("dark_end_fog", Double.toString(darkEndFogConfigured));
		properties.put("dark_skyless", Boolean.toString(darkSkyless));


		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			properties.store(stream, "Darkness properties file");
		} catch (final IOException e) {
			LOG.warn("[Darkness] Could not store property file '" + configFile.getAbsolutePath() + "'", e);
		}
	}

	public static boolean blockLightOnly() {
		return blockLightOnly;
	}

	public static double darkNetherFog() {
		return darkNetherFogEffective;
	}

	public static double darkEndFog() {
		return darkEndFogEffective;
	}

	private static boolean isDark(World world) {
		final RegistryKey<World> dimType = world.getRegistryKey();
		if (dimType == World.OVERWORLD) {
			return darkOverworld;
		} else if (dimType == World.NETHER) {
			return darkNether;
		} else if (dimType == World.END) {
			return darkEnd;
		} else if (world.getDimension().hasSkyLight()) {
			return darkDefault;
		} else {
			return darkSkyless;
		}
	}

	private static float skyFactor(World world) {
		if (!blockLightOnly && isDark(world)) {
			if (world.getDimension().hasSkyLight()) {
				final float angle = world.getSkyAngle(0);
				if (angle > 0.25f && angle < 0.75f) {
					final float oldWeight = Math.max(0, (Math.abs(angle - 0.5f) - 0.2f)) * 20;
					final float moon = ignoreMoonPhase ? 0 : world.getMoonSize();
					return MathHelper.lerp(oldWeight * oldWeight * oldWeight, moon * moon, 1f);
				} else {
					return 1;
				}
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	public static boolean enabled = false;
	private static final float[][] LUMINANCE = new float[16][16];

	public static int darken(int c, int blockIndex, int skyIndex) {
		final float lTarget = LUMINANCE[blockIndex][skyIndex];
		final float r = (c & 0xFF) / 255f;
		final float g = ((c >> 8) & 0xFF) / 255f;
		final float b = ((c >> 16) & 0xFF) / 255f;
		final float l = luminance(r, g, b);
		final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

		return f == 1f ? c : 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8) | (Math.round(f * b * 255) << 16);
	}

	public static float luminance(float r, float g, float b) {
		return r * 0.2126f + g * 0.7152f + b * 0.0722f;
	}

	public static void updateLuminance(float tickDelta, MinecraftClient client, GameRenderer worldRenderer, float prevFlicker) {
		final ClientWorld world = client.world;
		if (world != null) {

			if (!isDark(world) || client.player.hasStatusEffect(StatusEffects.NIGHT_VISION) || (client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && client.player.getUnderwaterVisibility() > 0) || world.getLightningTicksLeft() > 0) {
				enabled = false;
				return;
			} else {
				enabled = true;
			}

			final float dimSkyFactor = Darkness.skyFactor(world);
			final float ambient = world.method_23783(1.0F);
			final DimensionType dim = world.getDimension();
			final boolean blockAmbient = !Darkness.isDark(world);

			for (int skyIndex = 0; skyIndex < 16; ++skyIndex) {
				float skyFactor = 1f - skyIndex / 15f;
				skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
				skyFactor *= dimSkyFactor;

				float min = skyFactor * 0.05f;
				final float rawAmbient = ambient * skyFactor;
				final float minAmbient = rawAmbient * (1 - min) + min;
				final float skyBase = dim.getBrightness(skyIndex) * minAmbient;

				min = 0.35f * skyFactor;
				float skyRed = skyBase * (rawAmbient * (1 - min) + min);
				float skyGreen = skyBase * (rawAmbient * (1 - min) + min);
				float skyBlue = skyBase;

				if (worldRenderer.getSkyDarkness(tickDelta) > 0.0F) {
					final float skyDarkness = worldRenderer.getSkyDarkness(tickDelta);
					skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
					skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
					skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
				}

				for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
					float blockFactor = 1f;
					if (!blockAmbient) {
						blockFactor = 1f - blockIndex / 15f;
						blockFactor = 1 - blockFactor * blockFactor * blockFactor * blockFactor;
					}

					final float blockBase = blockFactor * dim.getBrightness(blockIndex) * (prevFlicker * 0.1F + 1.5F);
					min = 0.4f * blockFactor;
					final float blockGreen = blockBase * ((blockBase * (1 - min) + min) * (1 - min) + min);
					final float blockBlue = blockBase * (blockBase * blockBase * (1 - min) + min);

					float red = skyRed + blockBase;
					float green = skyGreen + blockGreen;
					float blue = skyBlue + blockBlue;

					final float f = Math.max(skyFactor, blockFactor);
					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

					if (world.getRegistryKey() == World.END) {
						red = skyFactor * 0.22F + blockBase * 0.75f;
						green = skyFactor * 0.28F + blockGreen * 0.75f;
						blue = skyFactor * 0.25F + blockBlue * 0.75f;
					}

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

					final float gamma = (float) client.options.gamma * f;
					float invRed = 1.0F - red;
					float invGreen = 1.0F - green;
					float invBlue = 1.0F - blue;
					invRed = 1.0F - invRed * invRed * invRed * invRed;
					invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
					invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
					red = red * (1.0F - gamma) + invRed * gamma;
					green = green * (1.0F - gamma) + invGreen * gamma;
					blue = blue * (1.0F - gamma) + invBlue * gamma;

					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

					if (red < 0.0F) {
						red = 0.0F;
					}

					if (green < 0.0F) {
						green = 0.0F;
					}

					if (blue < 0.0F) {
						blue = 0.0F;
					}

					LUMINANCE[blockIndex][skyIndex] = Darkness.luminance(red, green, blue);
				}
			}
		}
	}
}
