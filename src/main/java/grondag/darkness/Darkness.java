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

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Darkness {
    public static float computeAmbient(World world, float oldWeight, float oldAmbient) {
        final float moon = world.getMoonSize();
        final float result = MathHelper.lerp(oldWeight, oldAmbient * moon * moon, oldAmbient);
        return result;
    }
    
    public static float computeOldWeight(World world) {
        final float angle = world.getSkyAngle(0);
      if(angle > 0.25f && angle < 0.75f) {
          final float oldWeight = Math.abs(angle - 0.5f) * 4;
          return oldWeight * oldWeight * oldWeight;
      } else {
          return 1f;
      }
    }
}
