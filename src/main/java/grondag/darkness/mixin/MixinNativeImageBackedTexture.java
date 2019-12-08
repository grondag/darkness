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

package grondag.darkness.mixin;

import static grondag.darkness.Darkness.enabled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import grondag.darkness.Darkness;
import grondag.darkness.TextureAccess;

@Mixin(NativeImageBackedTexture.class)
public class MixinNativeImageBackedTexture implements TextureAccess {
	@Shadow
	NativeImage image;

	private boolean enableHook = false;

	@Inject(method = "upload", at = @At(value = "HEAD"))
	private void onRenderWorld(CallbackInfo ci) {
		if (enableHook && enabled) {
			final NativeImage img = image;
			for (int b = 0; b < 16; b++) {
				for (int s = 0; s < 16; s++) {
					final int color = Darkness.darken(img.getPixelRgba(b, s), b, s);
					img.setPixelRgba(b, s, color);
				}
			}
		}
	}

	@Override
	public void darkness_enableUploadHook() {
		enableHook = true;
	}
}
