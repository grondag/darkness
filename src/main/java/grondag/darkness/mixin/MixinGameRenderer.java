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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;

import grondag.darkness.Darkness;
import grondag.darkness.LightmapAccess;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
	@Shadow
	private MinecraftClient client;
	@Shadow
	private LightmapTextureManager lightmapTextureManager;

	@Inject(method = "renderWorld", at = @At(value = "HEAD"))
	private void onRenderWorld(float tickDelta, long nanos, MatrixStack matrixStack, CallbackInfo ci) {
		final LightmapAccess lightmap = (LightmapAccess) lightmapTextureManager;

		if (lightmap.darkness_isDirty()) {
			client.getProfiler().push("lightTex");
			Darkness.updateLuminance(tickDelta, client, (GameRenderer) (Object) this, lightmap.darkness_prevFlicker());
			client.getProfiler().pop();
		}
	}
}
