package net.blazecode.sirens.data;

import net.blazecode.sirens.data.helpers.SirensMineData;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class GlobalSirensData extends PersistentState
{
    @Override
    public NbtCompound writeNbt(NbtCompound tag)
    {
        NbtList mineDataListTag = new NbtList();

        for (SirensMineData mineData : mineDataList)
        {
            NbtCompound mineTag = new NbtCompound();

            mineData.writeNbt(mineTag);

            mineDataListTag.add(mineTag);
        }

        tag.put("mine_list", mineDataListTag);

        return tag;
    }

    public static GlobalSirensData fromNbt(NbtCompound tag)
    {
        GlobalSirensData sirensData = new GlobalSirensData();

        NbtList mineDataListTag = tag.getList("mine_list", NbtType.COMPOUND);

        for (NbtElement nbtElement : mineDataListTag)
        {
            NbtCompound mineTag = (NbtCompound) nbtElement;

            SirensMineData mineData = SirensMineData.fromNbt(mineTag);

            sirensData.addMine(mineData);
        }

        return sirensData;
    }

    public GlobalSirensData()
    {
        mineDataList = new ArrayList<>();
    }

    public void addMine(SirensMineData mine)
    {
        mineDataList.add(mine);
        markDirty();
    }

    public void removeMine(SirensMineData mine)
    {
        if(mineDataList.contains(mine))
        {
            mineDataList.remove(mine);
            markDirty();
        }
    }

    public SirensMineData getMine(String name)
    {
        for(SirensMineData mineData : mineDataList)
        {
            if(mineData.getName().equals(name))
            {
                return mineData;
            }
        }

        return null;
    }

    public List<String> getMineNamesList()
    {
        List<String> mineNamesList = new ArrayList<>();

        for(SirensMineData mineData : mineDataList)
        {
            mineNamesList.add(mineData.getName());
        }

        return mineNamesList;
    }

    public void tick(MinecraftServer server)
    {
        for( SirensMineData mineData : mineDataList )
        {
            mineData.tick(server);
        }
    }

    private final List<SirensMineData> mineDataList;
}
