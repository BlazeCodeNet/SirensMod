package net.blazecode.sirens.data.helpers;

import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SirensMineData
{
    public SirensMineData()
    {
        posOne = null;
        posTwo = null;

        name = "NULL";

        blockList = new ArrayList<>();
    }

    public void tick(MinecraftServer server)
    {
        ServerWorld world = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, worldIdentifier));

        BlockBox range = BlockBox.create(posOne, posTwo);

        Iterator blockIterator = BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ()).iterator();

        BlockPos curPos;
        while(blockIterator.hasNext())
        {
            curPos = (BlockPos) blockIterator.next();

            BlockState state = world.getBlockState(curPos);
            if(state.isAir())
            {
                int blIndex = world.random.nextInt(blockList.size());
                world.setBlockState(curPos, blockList.get(blIndex).getDefaultState());
            }
        }
    }

    public static SirensMineData fromNbt(NbtCompound tag)
    {
        SirensMineData mineData = new SirensMineData();

        // Corners
        NbtCompound cornersTag = tag.getCompound("corners");

        NbtCompound cornerOneTag = tag.getCompound("cornerOne");
        NbtCompound cornerTwoTag = tag.getCompound("cornerTwo");

        BlockPos cornerOne = NbtHelper.toBlockPos(cornerOneTag);
        BlockPos cornerTwo = NbtHelper.toBlockPos(cornerTwoTag);

        mineData.setCorners(cornerOne, cornerTwo);

        // World
        mineData.setWorldIdentifier(Identifier.tryParse(tag.getString("world")));

        // Name
        mineData.setName(tag.getString("name"));

        // Block List
        NbtList list = tag.getList("block_list", NbtType.STRING);

        List<Block> blockList = new ArrayList<>();
        list.forEach( tag1 ->
        {
            Identifier ident = Identifier.tryParse(tag1.asString());
            Block blk = Registry.BLOCK.get(ident);

            blockList.add(blk);
        });

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
        tag.putString("world", worldIdentifier.getPath());

        // Name
        tag.putString("name", name);

        // Block List
        NbtList list = new NbtList();
        for(Block b : getBlockList())
        {
            list.add(NbtString.of(Registry.BLOCK.getId(b).getPath()));
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
    public void setBlockList(List<Block> blockList)
    {
        this.blockList = blockList;
    }
    public void setWorldIdentifier(Identifier world)
    {
        this.worldIdentifier = world;
    }

    public Map<BlockPos, BlockPos> getCorners()
    {
        return Map.of(posOne, posTwo);
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
        return blockList;
    }

    private BlockPos posOne;
    private BlockPos posTwo;

    private Identifier worldIdentifier;

    private String name;

    private List<Block> blockList;
}
