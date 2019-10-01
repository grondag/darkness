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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.darkness.LightmapAccess;
import grondag.darkness.TextureAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImageBackedTexture;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager implements LightmapAccess {

	@Shadow
	private NativeImageBackedTexture texture;
	@Shadow
	private float prevFlicker;
	@Shadow
	private boolean isDirty;

	@Inject(method = "<init>*", at = @At(value = "RETURN"))
	private void afterInit(GameRenderer gameRenderer, MinecraftClient minecraftClient, CallbackInfo ci) {
		((TextureAccess) texture).darkness_enableUploadHook();
	}

	@Override
	public float darkness_prevFlicker() {
		return prevFlicker;
	}

	@Override
	public boolean darkness_isDirty() {
		return isDirty;
	}
}
