package com.ddf.structura4droid.structura;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.nbt.NbtUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessStructure {
    private int[] size;
    private int[] blocks;
    private List<NbtMap> palette;
    private int[][][] cube;

    public ProcessStructure(String path) {
        NbtMap tag;
        try (NBTInputStream nis = NbtUtils.createReaderLE(new FileInputStream(path))) {
            tag = (NbtMap) nis.readTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Integer> size1 = tag.getList("size", NbtType.INT);
        size = new int[size1.size()];
        for (int i = 0; i < size1.size(); i++) {
            size[i] = size1.get(i);
        }
        @SuppressWarnings("unchecked")
        NbtList<Integer> blocks1 = (NbtList<Integer>) tag.getCompound("structure").getList("block_indices", NbtType.LIST).get(0);
        blocks = new int[blocks1.size()];
        for (int i = 0; i < blocks1.size(); i++) {
            blocks[i] = blocks1.get(i);
        }
        palette = tag.getCompound("structure").getCompound("palette").getCompound("default").getList("block_palette", NbtType.COMPOUND);
        getBlockMap();
    }

    public void getBlockMap() {
        cube = new int[size[0]][size[1]][size[2]];
        int i = 0;
        for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    cube[x][y][z] = blocks[i++];
                }
            }
        }
    }

    public NbtMap getBlock(int x, int y, int z) {
        return palette.get(cube[x][y][z]);
    }

    public Map<String, Integer> getBlockList() {
        Map<String, Integer> blockCounter = new HashMap<>();
        for (int blockId : blocks) {
            String blockName = palette.get(blockId).getString("name");
            if (blockCounter.containsKey(blockName)) {
                blockCounter.put(blockName, blockCounter.get(blockName) + 1);
            } else {
                blockCounter.put(blockName, 1);
            }
        }
        return blockCounter;
    }

    public int[] getSize() {
        return size;
    }
}
