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

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class Darkness {
    public static Logger LOG = LogManager.getLogger("Darkness");

    private static final boolean darkOverworld;
    private static final boolean darkDefault;
    private static final boolean darkNether;
    private static final double darkNetherFog;
    private static final boolean darkEnd;
    private static final double darkEndFog;
    private static final boolean darkSkyless;
    
    static {
        File configDir = FabricLoader.getInstance().getConfigDirectory();
        if (!configDir.exists()) {
            LOG.warn("[Darkness] Could not access configuration directory: " + configDir.getAbsolutePath());
        }

        File configFile = new File(configDir, "darkness.properties");
        Properties properties = new Properties();
        if (configFile.exists()) {
            try (FileInputStream stream = new FileInputStream(configFile)) {
                properties.load(stream);
            } catch (IOException e) {
                LOG.warn("[Darkness] Could not read property file '" + configFile.getAbsolutePath() + "'", e);
            }
        }
        
        darkOverworld = properties.computeIfAbsent("dark_overworld", (a) -> "true").equals("true");
        darkDefault = properties.computeIfAbsent("dark_default", (a) -> "true").equals("true");
        darkNether = properties.computeIfAbsent("dark_nether", (a) -> "true").equals("true");
        darkNetherFog = Double.parseDouble(properties.computeIfAbsent("dark_nether_fog", (a) -> "0.5").toString());
        darkEnd = properties.computeIfAbsent("dark_end", (a) -> "true").equals("true");
        darkEndFog = Double.parseDouble(properties.computeIfAbsent("dark_end_fog", (a) -> "0.0").toString());
        darkSkyless = properties.computeIfAbsent("dark_skyless", (a) -> "true").equals("true");
        
        try (FileOutputStream stream = new FileOutputStream(configFile)) {
            properties.store(stream, "Darkness properties file");
        } catch (IOException e) {
            LOG.warn("[Indigo] Could not store property file '" + configFile.getAbsolutePath() + "'", e);
        }
    }
    
    public static double darkNetherFog() { return darkNetherFog; }
    public static double darkEndFog() { return darkEndFog; }
    
    private static boolean isDark(World world) {
        final DimensionType dimType = world.dimension.getType();
        if (dimType == DimensionType.OVERWORLD) {
            return darkOverworld;
        } else if (dimType == DimensionType.THE_NETHER) {
            return darkNether;
        } else if (dimType == DimensionType.THE_END) {
            return darkEnd;
        } else if (world.dimension.hasSkyLight()) {
            return darkDefault;
        } else {
            return darkSkyless;
        }
    }
    
    private static float skyFactor(World world) {
        if (isDark(world)) {
            if (world.dimension.hasSkyLight()) {
                final float angle = world.getSkyAngle(0);
                if (angle > 0.25f && angle < 0.75f) {
                    final float oldWeight = Math.max(0,(Math.abs(angle - 0.5f) - 0.2f)) * 20;
                    final float moon = world.getMoonSize();
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
    public static final float[][] luminance = new float[16][16];

    public static int darken(int c, float luminance, int blockIndex, int skyIndex) {
        final float r = (c & 0xFF) / 255f;
        final float g = ((c >> 8) & 0xFF) / 255f;
        final float b = ((c >> 16) & 0xFF) / 255f;
        final float l = luminance(r, g, b, blockIndex, skyIndex);
        final float f = l > 0 ? (luminance / l) : 0;
        return 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8) | (Math.round(f * b * 255) << 16);
    }
    
    public static float luminance(float r, float g, float b, int blockIndex, int skyIndex) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }
    
    public static void updateLuminance(float tickDelta, MinecraftClient client, GameRenderer worldRenderer, float prevFlicker) {
        World world = client.world;
        if (world != null) {

            if (client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)
                    || (client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && client.player.method_3140() > 0) 
                    || world.getTicksSinceLightning() > 0) {
                enabled = false;
                return;
            } else {
                enabled = true;
            }

            final float dimSkyFactor = Darkness.skyFactor(world);
            final float ambient = world.getAmbientLight(1.0F);
            final float[] brightness = world.dimension.getLightLevelToBrightness();
            final boolean blockAmbient = !Darkness.isDark(world);
            
            for(int skyIndex = 0; skyIndex < 16; ++skyIndex) {
                float skyFactor = 1f - skyIndex / 15f;
                skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
                skyFactor *= dimSkyFactor;
                
                float min = skyFactor * 0.05f;
                final float rawAmbient = ambient * skyFactor;
                final float minAmbient = rawAmbient * (1-min) + min;
                final float skyBase = brightness[skyIndex] * minAmbient;

                min = 0.35f * skyFactor;
                float skyRed = skyBase * (rawAmbient * (1-min) + min);
                float skyGreen = skyBase * (rawAmbient * (1-min) + min);
                float skyBlue = skyBase;
                
                if (worldRenderer.getSkyDarkness(tickDelta) > 0.0F) {
                    final float skyDarkness = worldRenderer.getSkyDarkness(tickDelta);
                    skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
                    skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
                    skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
                }
                
                for(int blockIndex = 0; blockIndex < 16; ++blockIndex) {
                    float blockFactor = 1f;
                    if (!blockAmbient) {
                        blockFactor = 1f - blockIndex / 15f;
                        blockFactor = 1 - blockFactor * blockFactor * blockFactor * blockFactor;
                    }
                    
                    final float blockBase =  blockFactor * brightness[blockIndex] * (prevFlicker * 0.1F + 1.5F);
                    min = 0.4f * blockFactor;
                    final float blockGreen = blockBase * ((blockBase * (1-min) + min) * (1-min) + min);
                    final float blockBlue = blockBase * (blockBase * blockBase * (1-min) + min);
                    
                    float red = skyRed + blockBase;
                    float green = skyGreen + blockGreen;
                    float blue = skyBlue + blockBlue;
                    
                    final float f = Math.max(skyFactor, blockFactor);
                    min = 0.03f * f;
                    red = red * (0.99F - min) + min;
                    green = green * (0.99F - min) + min;
                    blue = blue * (0.99F - min) + min;
                    

                    if (world.dimension.getType() == DimensionType.THE_END) {
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

                    final float gamma = (float)client.options.gamma * f;
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
                    
                    luminance[blockIndex][skyIndex] = Darkness.luminance(red, green, blue, blockIndex, skyIndex);
                }
            }
        }
    }
}
