package net.blazecode.sirens.mixins;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess
{

    @Inject( method = "tick", at = @At("RETURN"))
    void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci)
    {
        if(shouldKeepTicking.getAsBoolean())
        {
            tickDelta ++;
            if(tickDelta > 200)
            {
                tickDelta = 0;
            }

            if(tickDelta % 12000 == 0)
            {

            }

        }
    }

    private int tickDelta;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed)
    {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }
}
