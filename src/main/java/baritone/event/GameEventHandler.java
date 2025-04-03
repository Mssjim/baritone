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
import baritone.utils.BlockStateInterface;
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

    private int tickCounter = 0;
    private double[] lastPos = new double[] { 0, 0, 0 };

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
        tickCounter++;

        int DELAY_SECONDS = 3;
        int DELAY_DROP_SECONDS = DELAY_SECONDS*3;
        int DELAY_SAMEPOS = DELAY_SECONDS*5;
        if (tickCounter % (20 * DELAY_SECONDS) == 0 && baritone.getPlayerContext().player() != null) {
            // Tentativa macros
            double[] serversCoords = new double[] { 0.5, 20, 0.5 };
            double[] hubCoords = new double[] { 0.5, 20, -999.5 };
            double[] spawnCoords = new double[] { 403.5, 65, 257.5 };
            double[] coords = new double[] { baritone.getPlayerContext().player().getX(), baritone.getPlayerContext().player().getY(), baritone.getPlayerContext().player().getZ() };

            // Auto Login
            if(Arrays.equals(coords, hubCoords)) {
                logDirect("No hub, tentando logar");
                baritone.getPlayerContext().player().connection.sendChat(".macro login");
            } else if(Arrays.equals(coords, spawnCoords)) {
                logDirect("No spawn, tentando minar");
                baritone.getPlayerContext().player().connection.sendChat(".macro home");
            } else if(Arrays.equals(coords, serversCoords)) {
                logDirect("No Hub de Servers, tentando selecionar Survival");
                baritone.getPlayerContext().player().connection.sendChat(".macro hub");
            }

            if(tickCounter % (20 * DELAY_DROP_SECONDS) == 0 && !Arrays.equals(coords, serversCoords) && !Arrays.equals(coords, hubCoords) && !Arrays.equals(coords, spawnCoords)) {
                int freeSlots = 0;
                String[] itemsToDrop = new String[] {
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
                        "minecraft:spider_eye"
                };

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

            if(tickCounter % (20 * DELAY_SAMEPOS) == 0 && !Arrays.equals(coords, serversCoords) && !Arrays.equals(coords, hubCoords)) {
                if(Arrays.equals(coords, lastPos)) {
                    logDirect("Player parado por " + DELAY_SAMEPOS + " segundos. Voltando para casa");
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
