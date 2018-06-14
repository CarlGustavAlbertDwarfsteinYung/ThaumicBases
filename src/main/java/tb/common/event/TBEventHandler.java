package tb.common.event;

import DummyCore.Utils.Coord3D;
import DummyCore.Utils.Pair;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import tb.common.item.ItemHerobrinesScythe;
import tb.common.item.ItemUkulele;
import tb.core.TBCore;
import tb.utils.TBUtils;

public class TBEventHandler {
    public static int clientUkuleleSoundPlayDelay = 0;

    @SubscribeEvent
    public void clientWorldTickEvent(ClientTickEvent event) {
        World world = TBCore.proxy.clientWorld();
        if (event.side == Side.CLIENT && event.phase == Phase.END && world != null && world.provider != null) {
            System.out.println(clientUkuleleSoundPlayDelay);

            --clientUkuleleSoundPlayDelay;
            if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.ticksExisted % 20 * 20 == 0) {
                TBUtils.loadedClientSpawners.clear();
                if (world.loadedTileEntityList != null) {
                    for (int i = 0; i < world.loadedTileEntityList.size(); ++i) {
                        Object obj = world.loadedTileEntityList.get(i);
                        if (obj instanceof TileEntity && obj instanceof TileEntityMobSpawner && !TileEntity.class.cast(obj).isInvalid() && TileEntity.class.cast(obj) instanceof ITickable) {
                            TileEntity tile = TileEntity.class.cast(obj);
                            Coord3D coords = new Coord3D(tile.xCoord, tile.yCoord, tile.yCoord);
                            Pair<Integer, Coord3D> tileCoords = new Pair<Integer, Coord3D>(world.provider.dimensionId, coords);
                            TBUtils.loadedClientSpawners.add(tileCoords);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void useItemTickEvent(PlayerUseItemEvent.Tick event) {
        if (event.item.getItem() instanceof ItemUkulele && event.duration <= 1) {
            event.duration = 2;
        }
    }

    @SubscribeEvent
    public void nameFormatEvent(NameFormat event) {
        if (event.entityPlayer != null && event.entityPlayer.getCurrentEquippedItem() != null && event.entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemHerobrinesScythe)
            event.displayname = "Herobrine";
    }

}
