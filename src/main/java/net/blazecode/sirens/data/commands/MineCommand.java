package net.blazecode.sirens.data.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.blazecode.sirens.api.SirensAPI;
import net.blazecode.sirens.data.GlobalSirensData;
import net.blazecode.sirens.data.helpers.SirensMineData;
import net.blazecode.vanillify.api.VanillaUtils;
import net.blazecode.wanda.api.WandaAPI;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.RegistryWorldView;

import java.util.ArrayList;
import java.util.List;

public class MineCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("mine")
                .requires( src -> Permissions.require("sirens.command.mine" ).test(src) || src.hasPermissionLevel(4) )
                    .then(CommandManager.literal("create")
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> executeCreate(ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(CommandManager.literal("reset")
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> executeReset(ctx, StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(builder);
    }

    public static int executeReset(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException
    {
        SirensMineData mineData = SirensAPI.getGlobalData(ctx.getSource().getServer()).getMine(name);

        if(mineData != null)
        {
            mineData.tick(ctx.getSource().getServer());
            ctx.getSource().sendFeedback(VanillaUtils.getText("Reset mine '"+mineData.getName()+"'!", Formatting.LIGHT_PURPLE), false);
        }
        else
        {
            ctx.getSource().sendFeedback(VanillaUtils.getText("Invalid mine name.", Formatting.RESET), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int executeCreate(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException
    {
        ServerPlayerEntity srvPlr = (ServerPlayerEntity) ctx.getSource().getPlayer();

        SirensMineData mineData = new SirensMineData();
        mineData.setName(name);
        mineData.setCorners(WandaAPI.getPosOne(srvPlr), WandaAPI.getPosTwo(srvPlr));
        mineData.setWorldIdentifier(srvPlr.getServerWorld().getRegistryKey().getValue());
        List<Block> blockList = new ArrayList<>();
        blockList.add(Blocks.STONE);
        blockList.add(Blocks.STONE);
        blockList.add(Blocks.STONE);
        blockList.add(Blocks.STONE);
        blockList.add(Blocks.IRON_ORE);
        mineData.setBlockList(blockList);

        GlobalSirensData sirenData = SirensAPI.getGlobalData(ctx.getSource().getServer());
        sirenData.addMine(mineData);

        srvPlr.sendMessage(VanillaUtils.getText("Added mine '"+name+"'!", Formatting.LIGHT_PURPLE), false);

        return Command.SINGLE_SUCCESS;
    }
}
