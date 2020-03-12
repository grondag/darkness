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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.TheEndDimension;

import grondag.darkness.Darkness;

@Mixin(TheEndDimension.class)
public class MixinTheEndDimension {
	private static double MIN = 0.029999999329447746D;

	@Inject(method = "modifyFogColor", at = @At(value = "RETURN"), cancellable = true)
	private void onModifyFogColor(CallbackInfoReturnable<Vec3d> ci) {
		final double factor = Darkness.darkEndFog();

		if (factor != 1.0) {

			Vec3d result = ci.getReturnValue();
			result = new Vec3d(Math.max(MIN, result.x * factor), Math.max(MIN, result.y * factor), Math.max(MIN, result.z * factor));

			ci.setReturnValue(result);
		}
	}
}
