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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.darkness.Darkness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.World;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @Shadow private boolean isDirty;
    @Shadow private MinecraftClient client;
    
    private float oldWeight = 1f;
    
    /** main purpose is to workaround mixin/loader bug that doesn't output refmap without an inject */
    @Inject(method = "update", at = @At(value = "HEAD"))
    private void computeWeight(CallbackInfo ci) {
        if(isDirty) {
            final World world = client.world;
            if(world != null) {
                oldWeight = Darkness.computeOldWeight(world);
            } else {
                oldWeight = 1f;
            }
        }
    }
    
    @ModifyVariable(method = "update", at = @At(value = "STORE"), ordinal = 1, allow = 1, require = 1)
    private float makeItDark(float oldAmbient) {
        return oldAmbient * oldWeight;
    }
    
    @ModifyConstant( constant = @Constant(floatValue = 0.05F),method = "update" )
    private float modifyAmbientMin(float originalValue) {
        return originalValue * oldWeight;
    }

    @ModifyConstant( constant = @Constant(floatValue = 0.35F),method = "update" )
    private float modifyRedGreenMin(float originalValue) {
        return originalValue * oldWeight;
    }
    
    @ModifyConstant( constant = @Constant(floatValue = 0.96F),method = "update" )
    private float modifyCorrectionFactor(float originalValue) {
        return originalValue + 0.03F * (1 - oldWeight);
    }
    
    @ModifyConstant( constant = @Constant(floatValue = 0.03F),method = "update" )
    private float modifyCorrectionMin(float originalValue) {
        return originalValue * oldWeight;
    }
    
    @ModifyArg(at = @At(value = "INVOKE", target="Lnet/minecraft/client/texture/NativeImage;setPixelRGBA(III)V"), method = "update", index = 2)
    private int hookSetPixel(int blockCounter, int skyCounter, int color) {
        return blockCounter == 0 && skyCounter == 0 ? 0xFF000000 : color;
    }
}
