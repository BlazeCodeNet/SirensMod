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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
                                .executes(ctx -> executeReset(ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(CommandManager.literal("remove")
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> executeRemove(ctx, StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(builder);
    }

    public static int executeRemove(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException
    {
        ServerPlayerEntity srvPlr = (ServerPlayerEntity) ctx.getSource().getPlayer();

        SirensMineData mineData = SirensAPI.getGlobalData(ctx.getSource().getServer()).getMine(name);

        if(mineData == null)
        {
            ctx.getSource().sendFeedback(VanillaUtils.getText("Invalid mine name.", Formatting.RESET), false);
            return Command.SINGLE_SUCCESS;
        }

        GlobalSirensData sirenData = SirensAPI.getGlobalData(ctx.getSource().getServer());
        sirenData.removeMine(mineData);

        srvPlr.sendMessage(VanillaUtils.getText("Removed mine '"+name+"'!", Formatting.LIGHT_PURPLE), false);

        return Command.SINGLE_SUCCESS;
    }

    public static int executeReset(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException
    {
        SirensMineData mineData = SirensAPI.getGlobalData(ctx.getSource().getServer()).getMine(name);

        if(mineData != null)
        {
            mineData.tick(ctx.getSource().getServer());
            ctx.getSource().sendFeedback(VanillaUtils.getText("Reset mine '"+mineData.getName()+"'! at "+mineData.getPosOne().toShortString(), Formatting.LIGHT_PURPLE), false);
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

        SirensMineData mineData = SirensAPI.getGlobalData(ctx.getSource().getServer()).getMine(name);
        if(mineData != null)
        {
            ctx.getSource().sendFeedback(VanillaUtils.getText("A mine already exists with the name '"+name+"'!", Formatting.LIGHT_PURPLE), false);
            return Command.SINGLE_SUCCESS;
        }

        mineData = new SirensMineData();
        mineData.setName(name);
        mineData.setCorners(WandaAPI.getPosOne(srvPlr), WandaAPI.getPosTwo(srvPlr));
        mineData.setWorldIdentifier(srvPlr.getServerWorld().getRegistryKey().getValue());
        HashMap<Block, Integer> blockList = new HashMap<>();

        BlockBox range = BlockBox.create(mineData.getPosOne(), mineData.getPosTwo());

        Iterator blockIterator = BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ()).iterator();

        BlockPos curPos;
        while(blockIterator.hasNext())
        {
            curPos = (BlockPos) blockIterator.next();

            BlockState state = srvPlr.getServerWorld().getBlockState(curPos);

            if(!state.isAir() && ( state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA && state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.LADDER ))
            {
                if(blockList.containsKey(state.getBlock()))
                {
                    int oldCount = blockList.get(state.getBlock());
                    blockList.replace(state.getBlock(), oldCount + 1);
                }
                else
                {
                    blockList.put(state.getBlock(), 1);
                }
            }
        }

        if(blockList.size() <= 0)
        {
            srvPlr.sendMessage(VanillaUtils.getText("Invalid mine setup: No blocks are in the mine area for a template!", Formatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        mineData.setBlockList(blockList);

        GlobalSirensData sirenData = SirensAPI.getGlobalData(ctx.getSource().getServer());
        sirenData.addMine(mineData);

        srvPlr.sendMessage(VanillaUtils.getText("Added mine '"+name+"'!", Formatting.LIGHT_PURPLE), false);

        return Command.SINGLE_SUCCESS;
    }
}
