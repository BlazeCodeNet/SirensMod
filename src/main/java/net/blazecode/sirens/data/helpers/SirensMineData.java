package net.blazecode.sirens.data.helpers;

import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.*;

public class SirensMineData
{
    public SirensMineData()
    {
        posOne = null;
        posTwo = null;

        name = "NULL";

        blockList = new HashMap<>();
    }

    public void tick(MinecraftServer server)
    {
        ServerWorld world = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, worldIdentifier));

        BlockBox range = BlockBox.create(posOne, posTwo);

        Iterator blockIterator = BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ()).iterator();

        BlockPos curPos;

        List<Block> rawList = getBlockList();

        while(blockIterator.hasNext())
        {
            curPos = (BlockPos) blockIterator.next();

            BlockState state = world.getBlockState(curPos);
            if(state.isAir() || ( state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.LADDER ))
            {
                int blIndex = world.random.nextInt(rawList.size());
                world.setBlockState(curPos, rawList.get(blIndex).getDefaultState());
            }
        }
    }

    public static SirensMineData fromNbt(NbtCompound tag)
    {
        SirensMineData mineData = new SirensMineData();

        // Corners
        NbtCompound cornersTag = tag.getCompound("corners");

        NbtCompound cornerOneTag = cornersTag.getCompound("cornerOne");
        NbtCompound cornerTwoTag = cornersTag.getCompound("cornerTwo");

        BlockPos cornerOne = NbtHelper.toBlockPos(cornerOneTag);
        BlockPos cornerTwo = NbtHelper.toBlockPos(cornerTwoTag);

        mineData.setCorners(cornerOne, cornerTwo);

        // World
        mineData.setWorldIdentifier(Identifier.tryParse(tag.getString("world")));

        // Name
        mineData.setName(tag.getString("name"));

        // Block List
        NbtList list = tag.getList("block_list", NbtType.COMPOUND);

        HashMap<Block, Integer> blockList = new HashMap<>();
        for(NbtElement nbtElement : list)
        {
            NbtCompound blockEntryTag = (NbtCompound)nbtElement;

            int count = blockEntryTag.getInt("block_count");

            Identifier ident = Identifier.tryParse(blockEntryTag.getString("block_id"));
            Block blk = Registry.BLOCK.get(ident);

            if(blockList.containsKey(blk))
            {
                int oldCount = blockList.get(blk);
                blockList.replace(blk, oldCount + count);
            }
            else
            {
                blockList.put(blk, count);
            }
        }

        mineData.setBlockList(blockList);

        return mineData;
    }

    public NbtCompound writeNbt(NbtCompound tag)
    {
        // Corners
        NbtCompound cornerOneTag = NbtHelper.fromBlockPos(posOne);
        NbtCompound cornerTwoTag = NbtHelper.fromBlockPos(posTwo);

        NbtCompound cornersTag = new NbtCompound();
        cornersTag.put("cornerOne", cornerOneTag);
        cornersTag.put("cornerTwo", cornerTwoTag);

        tag.put("corners", cornersTag);

        // World
        tag.putString("world", worldIdentifier.toString());

        // Name
        tag.putString("name", name);

        // Block List
        NbtList list = new NbtList();
        for(Block b : blockList.keySet())
        {
            NbtCompound blockEntryTag = new NbtCompound();

            blockEntryTag.putInt("block_count", blockList.get(b));
            blockEntryTag.putString("block_id", Registry.BLOCK.getId(b).toString());

            list.add(blockEntryTag);
        }

        tag.put("block_list", list);

        return tag;
    }

    public void setCorners(BlockPos posOne, BlockPos posTwo)
    {
        this.posOne = posOne;
        this.posTwo = posTwo;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setBlockList(HashMap<Block, Integer> blockList)
    {
        this.blockList = blockList;
    }
    public void setWorldIdentifier(Identifier world)
    {
        this.worldIdentifier = world;
    }

    public BlockPos getPosOne()
    {
        return posOne;
    }
    public BlockPos getPosTwo()
    {
        return posTwo;
    }

    public Identifier getWorldIdentifier()
    {
        return worldIdentifier;
    }
    public String getName()
    {
        return name;
    }
    public List<Block> getBlockList()
    {
        List<Block> rawBlockList = new ArrayList<>();

        for(Block b : blockList.keySet())
        {
            int count = blockList.get(b);
            for(int i = 0; i < count; i++)
            {
                rawBlockList.add(b);
            }
        }

        return rawBlockList;
    }

    private BlockPos posOne;
    private BlockPos posTwo;

    private Identifier worldIdentifier;

    private String name;

    private HashMap<Block, Integer> blockList;
}
