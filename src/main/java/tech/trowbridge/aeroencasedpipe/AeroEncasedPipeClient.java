package tech.trowbridge.aeroencasedpipe;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import tech.trowbridge.aeroencasedpipe.ponder.AddonPonderPlugin;

@Mod(value = AeroEncasedPipe.MOD_ID, dist = Dist.CLIENT)
public class AeroEncasedPipeClient {

    public AeroEncasedPipeClient() {
        PonderIndex.addPlugin(new AddonPonderPlugin());
    }
}
