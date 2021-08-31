package net.blazecode.sirens;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.blazecode.sirens.data.commands.MineCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

@Environment( EnvType.SERVER )
public class SirensMod implements DedicatedServerModInitializer
{
	public static final String MODID = "sirens";

	@Override
	public void onInitializeServer( )
	{
		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

		CommandRegistrationCallback.EVENT.register( (dispatcher, dedicated) ->
		{
			if(dedicated)
			{
				MineCommand.register(dispatcher);
			}
		});
	}

	public static ModConfig getConfig()
	{
		if (config == null)
		{
			config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		}
		return config;
	}

	private static ModConfig config;

	@Config(name = MODID)
	public static class ModConfig implements ConfigData
	{
		@Comment("Toggles the entire mod on or off")
		boolean enabled = true;
	}
}
