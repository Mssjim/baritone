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

package baritone.utils.player;

import baritone.api.utils.Helper;
import baritone.api.utils.IPlayerController;
import net.minecraft.client.entity.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

/**
 * Implementation of {@link IPlayerController} that chains to the primary player controller's methods
 *
 * @author Brady
 * @since 12/14/2018
 */
public enum PrimaryPlayerController implements IPlayerController, Helper {

    INSTANCE;

    @Override
    public boolean onPlayerDamageBlock(BlockPos pos, Direction side) {
        return mc.playerController.onPlayerDamageBlock(pos, side);
    }

    @Override
    public void resetBlockRemoving() {
        mc.playerController.resetBlockRemoving();
    }

    @Override
    public ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, PlayerEntity player) {
        return mc.playerController.windowClick(windowId, slotId, mouseButton, type, player);
    }

    @Override
    public void setGameType(GameType type) {
        mc.playerController.setGameType(type);
    }

    @Override
    public GameType getGameType() {
        return mc.playerController.getCurrentGameType();
    }

    @Override
    public EnumActionResult processRightClickBlock(ClientPlayerEntity player, World world, BlockPos pos, Direction direction, Vec3d vec, EnumHand hand) {
        // primaryplayercontroller is always in a ClientWorld so this is ok
        return mc.playerController.processRightClickBlock(player, (ClientWorld) world, pos, direction, vec, hand);
    }

    @Override
    public EnumActionResult processRightClick(ClientPlayerEntity player, World world, EnumHand hand) {
        return mc.playerController.processRightClick(player, world, hand);
    }
}
