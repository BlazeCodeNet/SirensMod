package net.blazecode.sirens.api;

import net.blazecode.sirens.data.GlobalSirensData;
import net.minecraft.server.MinecraftServer;

public class SirensAPI
{
    public static GlobalSirensData getGlobalData(MinecraftServer server)
    {
        return server.getOverworld().getPersistentStateManager().getOrCreate(GlobalSirensData::fromNbt, GlobalSirensData::new, "global_sirens_data");
    }
}
