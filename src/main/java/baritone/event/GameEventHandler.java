/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.event;

import baritone.Baritone;
import baritone.api.event.events.*;
import baritone.api.event.events.type.EventState;
import baritone.api.event.listener.IEventBus;
import baritone.api.event.listener.IGameEventListener;
import baritone.api.utils.Helper;
import baritone.api.utils.Pair;
import baritone.cache.CachedChunk;
import baritone.cache.WorldProvider;
import baritone.process.MineProcess;
import baritone.utils.BlockStateInterface;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Brady
 * @since 7/31/2018
 */
public final class GameEventHandler implements IEventBus, Helper {

    private final Baritone baritone;

    private final List<IGameEventListener> listeners = new CopyOnWriteArrayList<>();

    public GameEventHandler(Baritone baritone) {
        this.baritone = baritone;
    }

    private int tickCounter = 1;
    private double[] lastPos = new double[] { 0, 0, 0 };
    public static boolean willMine = false;

    private static final String[] itemsToDrop = new String[] {
            "minecraft:dirt",
            "minecraft:cobblestone",
            "minecraft:gravel",
            "minecraft:sand",
            "minecraft:andesite",
            "minecraft:diorite",
            "minecraft:granite",
            "minecraft:stone",
            "minecraft:brown_mushroom",
            "minecraft:red_mushroom",
            "minecraft:flint",
            "minecraft:obsidian",
            "minecraft:coal",
            "minecraft:torch",
            "minecraft:mossy_cobblestone",
            "minecraft:farmland",
            "minecraft:grass_block",
            "minecraft:rail",
            "minecraft:clay",
            "minecraft:clay_ball",
            "minecraft:redstone",
            "minecraft:tuff",
            "minecraft:deepslate",
            "minecraft:dripstone_block",
            "minecraft:raw_copper",
            "minecraft:pointed_dripstone",
            "minecraft:coarse_dirt",
            "minecraft:rotten_flesh",
            "minecraft:gunpowder",
            "minecraft:bone_block",
            "minecraft:bone",
            "minecraft:amethyst_block",
            "minecraft:calcite",
            "minecraft:smooth_basalt",
            "minecraft:string",
            "minecraft:amethyst_shard",
            "minecraft:arrow",
            "minecraft:diamond_horse_armor",
            "minecraft:carrot",
            "minecraft:magma_block",
            "minecraft:spider_eye",
            "minecraft:tag",
            "minecraft:oak_sapling",
            "minecraft:spruce_sapling",
            "minecraft:birch_sapling",
            "minecraft:jungle_sapling",
            "minecraft:acacia_sapling",
            "minecraft:dark_oak_sapling",
            "minecraft:waxed_copper_block",
            "minecraft:waxed_oxidized_copper",
            "minecraft:chiseled_tuff_bricks",
            "minecraft:gold_nugget",
            "minecraft:blaze_rod",
            "minecraft:sandstone",
            "minecraft:deepslate_tiles",
            "minecraft:cracked_deepslate_tiles",
            "minecraft:deepslate_brick_stairs",
            "minecraft:deepslate_tile_stairs",
            "minecraft:deepslate_bricks",
            "minecraft:cracked_deepslate_bricks",
            "minecraft:glow_berries",
            "minecraft:azalea",
            "minecraft:moss_block",
            "minecraft:moss_carpet",
            "minecraft:tuff_bricks"
    };

    static double[] worldSpawnCoords = new double[] { 0, 0, 0 };

    public static String getWorldName() {
        double[] lobbyWorldCoords = new double[] { 0, 20, 0 };
        double[] hubWorldCoords = new double[] { 0, 184, 155 };
        double[] spawnWorldCoords = new double[] { 403, 65, 257 };
        double[] survivalWorldCoords = new double[] { 0, 95, 0 };
        double[] recursosWorldCoords = new double[] { 0, 68, 0 };

        if(Arrays.equals(worldSpawnCoords, lobbyWorldCoords)) {
            return "lobby";
        } else if(Arrays.equals(worldSpawnCoords, hubWorldCoords)) {
            return "hub";
        } else if(Arrays.equals(worldSpawnCoords, spawnWorldCoords)) {
            return "spawn";
        } else if(Arrays.equals(worldSpawnCoords, survivalWorldCoords)) {
            return "survival";
        } else if(Arrays.equals(worldSpawnCoords, recursosWorldCoords)) {
            return "recursos";
        } else {
            return "unknown";
        }
    }

    @Override
    public final void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.IN) {
            try {
                baritone.bsi = new BlockStateInterface(baritone.getPlayerContext(), true);
            } catch (Exception ex) {
                ex.printStackTrace();
                baritone.bsi = null;
            }
        } else {
            baritone.bsi = null;
        }

        // Todos devem ser multiplos do "DELAY_SECONDS"
        int DELAY_SECONDS = 5;
        int DELAY_DROP_SECONDS = 10;
        int DELAY_SAMEPOS_SECONDS = 30;
        tickCounter++;

        if (tickCounter % (20 * DELAY_SECONDS) == 0 && baritone.getPlayerContext().player() != null && Baritone.settings().tickMacros.value) { // Macros
            LocalPlayer player = baritone.getPlayerContext().player();
            BlockPos worldSpawnPos = baritone.getPlayerContext().world().getLevelData().getSpawnPos();

            double[] lobbyCoords = new double[] { 0.5, 20, -999.5 };
            double[] hubCoords = new double[] { 0.5, 20, 0.5 };
            double[] spawnCoords = new double[] { 403.5, 65, 257.5 };

            double[] coords = new double[] { player.getX(), player.getY(), player.getZ() };
            worldSpawnCoords = new double[] { worldSpawnPos.getX(), worldSpawnPos.getY(), worldSpawnPos.getZ() };

            logDirect("[" + String.format("%010d", tickCounter) + "] " + getWorldName());

            // Auto Login
            if(getWorldName().equals("lobby")) {
                logDirect("No Lobby, tentando logar");
                baritone.getPlayerContext().player().connection.sendChat(".macro login");
            } else if(getWorldName().equals("hub")) {
                logDirect("No Hub, tentando entrar");
                baritone.getPlayerContext().player().connection.sendChat(".macro hub");
            } else if(Arrays.equals(coords, spawnCoords)) {
                logDirect("No spawn, tentando minar");
                baritone.getPlayerContext().player().connection.sendChat(".macro home");
            }

            if(tickCounter % (20 * DELAY_DROP_SECONDS) == 0 && getWorldName().equals("recursos")) {
                int freeSlots = 0;
                boolean dropou = false;

                for(int i = 9; i <= 35; i++) {
                    String itemName = baritone.getPlayerContext().player().getInventory().getItem(i).toString();
                    itemName = itemName.substring(itemName.indexOf(" ") + 1);
                    int itemQuantity = baritone.getPlayerContext().player().getInventory().getItem(i).getCount();

                    if(itemName.equals("minecraft:air")) {
                        freeSlots++;
                    } else {
                        if(Arrays.asList(itemsToDrop).contains(itemName) && !dropou) {
                            dropou = true;
                            logDirect("["+i+"] Dropando " + itemName + " x" + itemQuantity);
                            baritone.getPlayerContext().player().connection.sendChat(".drop " + itemName);
                        }
                    }
                }

                if(freeSlots < 3) {
                    logDirect("Inventário cheio, voltando para casa");
                    baritone.getPlayerContext().player().connection.sendChat(".macro home");
                }
            }

            if(tickCounter % (20 * DELAY_SAMEPOS_SECONDS) == 0 && !getWorldName().equals("lobby") && !getWorldName().equals("hub")) {
                // Conferir se o X e Z sao iguais, ignorar o Y)
                if(coords[0] == lastPos[0] && coords[2] == lastPos[2] && coords[1] == lastPos[1]) {
                    logDirect("Player parado por " + DELAY_SAMEPOS_SECONDS + " segundos. Voltando para casa");
                    baritone.getPlayerContext().player().connection.sendChat(".macro home");
                    // Conferir se o range é menor que 5 blocos
                } else if(Math.abs(coords[0] - lastPos[0]) < 5 && Math.abs(coords[2] - lastPos[2]) < 5
                        && Math.abs(coords[1] - lastPos[1]) < 5 && getWorldName().equals("recursos")) {
                    logDirect("Minerando em circulo por " + DELAY_SAMEPOS_SECONDS + " segundos. Voltando para casa");
                    baritone.getPlayerContext().player().connection.sendChat(".macro home");
                }
                lastPos = coords;
            }
        }

        listeners.forEach(l -> l.onTick(event));
    }

    @Override
    public void onPostTick(TickEvent event) {
        listeners.forEach(l -> l.onPostTick(event));
    }

    @Override
    public final void onPlayerUpdate(PlayerUpdateEvent event) {
        listeners.forEach(l -> l.onPlayerUpdate(event));
    }

    @Override
    public final void onSendChatMessage(ChatEvent event) {
        listeners.forEach(l -> l.onSendChatMessage(event));
    }

    @Override
    public void onPreTabComplete(TabCompleteEvent event) {
        listeners.forEach(l -> l.onPreTabComplete(event));
    }

    @Override
    public void onChunkEvent(ChunkEvent event) {
        EventState state = event.getState();
        ChunkEvent.Type type = event.getType();

        Level world = baritone.getPlayerContext().world();

        // Whenever the server sends us to another dimension, chunks are unloaded
        // technically after the new world has been loaded, so we perform a check
        // to make sure the chunk being unloaded is already loaded.
        boolean isPreUnload = state == EventState.PRE
                && type == ChunkEvent.Type.UNLOAD
                && world.getChunkSource().getChunk(event.getX(), event.getZ(), null, false) != null;

        if (event.isPostPopulate() || isPreUnload) {
            baritone.getWorldProvider().ifWorldLoaded(worldData -> {
                LevelChunk chunk = world.getChunk(event.getX(), event.getZ());
                worldData.getCachedWorld().queueForPacking(chunk);
            });
        }


        listeners.forEach(l -> l.onChunkEvent(event));
    }

    @Override
    public void onBlockChange(BlockChangeEvent event) {
        if (Baritone.settings().repackOnAnyBlockChange.value) {
            final boolean keepingTrackOf = event.getBlocks().stream()
                    .map(Pair::second).map(BlockState::getBlock)
                    .anyMatch(CachedChunk.BLOCKS_TO_KEEP_TRACK_OF::contains);

            if (keepingTrackOf) {
                baritone.getWorldProvider().ifWorldLoaded(worldData -> {
                    final Level world = baritone.getPlayerContext().world();
                    ChunkPos pos = event.getChunkPos();
                    worldData.getCachedWorld().queueForPacking(world.getChunk(pos.x, pos.z));
                });
            }
        }

        listeners.forEach(l -> l.onBlockChange(event));
    }

    @Override
    public final void onRenderPass(RenderEvent event) {
        listeners.forEach(l -> l.onRenderPass(event));
    }

    @Override
    public final void onWorldEvent(WorldEvent event) {
        WorldProvider cache = baritone.getWorldProvider();

        if (event.getState() == EventState.POST) {
            cache.closeWorld();
            if (event.getWorld() != null) {
                cache.initWorld(event.getWorld());
            }
        }

        listeners.forEach(l -> l.onWorldEvent(event));
    }

    @Override
    public final void onSendPacket(PacketEvent event) {
        listeners.forEach(l -> l.onSendPacket(event));
    }

    @Override
    public final void onReceivePacket(PacketEvent event) {
        listeners.forEach(l -> l.onReceivePacket(event));
    }

    @Override
    public void onPlayerRotationMove(RotationMoveEvent event) {
        listeners.forEach(l -> l.onPlayerRotationMove(event));
    }

    @Override
    public void onPlayerSprintState(SprintStateEvent event) {
        listeners.forEach(l -> l.onPlayerSprintState(event));
    }

    @Override
    public void onBlockInteract(BlockInteractEvent event) {
        listeners.forEach(l -> l.onBlockInteract(event));
    }

    @Override
    public void onPlayerDeath() {
        listeners.forEach(IGameEventListener::onPlayerDeath);
    }

    @Override
    public void onPathEvent(PathEvent event) {
        listeners.forEach(l -> l.onPathEvent(event));
    }

    @Override
    public final void registerEventListener(IGameEventListener listener) {
        this.listeners.add(listener);
    }
}
