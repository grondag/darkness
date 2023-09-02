/*
 * This file is part of True Darkness and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.darkness.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.LightTexture;

import grondag.darkness.Darkness;
import grondag.darkness.LightmapAccess;

@Mixin(LightTexture.class)
public class MixinLightTexture implements LightmapAccess {
	@Shadow
	private NativeImage lightPixels;
	@Shadow
	private float blockLightRedFlicker;
	@Shadow
	private boolean updateLightTexture;

	@Inject(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"))
	private void onUpload(CallbackInfo ci) {
		if (Darkness.enabled && lightPixels != null) {
			for (int b = 0; b < 16; b++) {
				for (int s = 0; s < 16; s++) {
					final int color = Darkness.darken(lightPixels.getPixelRGBA(b, s), b, s);
					lightPixels.setPixelRGBA(b, s, color);
				}
			}
		}
	}

	@Override
	public float darkness_prevFlicker() {
		return blockLightRedFlicker;
	}

	@Override
	public boolean darkness_isDirty() {
		return updateLightTexture;
	}
}
