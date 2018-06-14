package tb.network.proxy;

import DummyCore.Client.GuiCommon;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import tb.client.RevolverEvents;
import tb.client.gui.GuiRevolver;
import tb.client.gui.GuiThaumicAnvil;
import tb.client.gui.GuiVoidAnvil;
import tb.client.render.block.BrazierRenderer;
import tb.client.render.block.CampfireRenderer;
import tb.client.render.block.ThaumicRelocatorRenderer;
import tb.client.render.entity.RenderBullet;
import tb.client.render.item.*;
import tb.client.render.tile.RenderEntityDeconstructor;
import tb.client.render.tile.RenderNodeLinker;
import tb.client.render.tile.RenderNodeManipulator;
import tb.client.render.tile.RenderOverchanter;
import tb.common.entity.EntityRevolverBullet;
import tb.common.event.TBEventHandler;
import tb.common.inventory.ContainerOverchanter;
import tb.common.item.ItemUkulele;
import tb.common.tile.TileEntityDeconstructor;
import tb.common.tile.TileNodeLinker;
import tb.common.tile.TileNodeManipulator;
import tb.common.tile.TileOverchanter;
import tb.init.TBBlocks;
import tb.init.TBItems;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.client.fx.particles.FXSparkle;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class TBClient extends TBServer {


    public void playGuitarSound(String sound) {
        MovingSound snd = new GuitarSound(new ResourceLocation(sound));
        Minecraft.getMinecraft().getSoundHandler().playSound(snd);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        if (ID == 0x421922) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile != null) {
                if (tile instanceof TileOverchanter) {
                    return new GuiCommon(new ContainerOverchanter(player.inventory, tile), tile);
                }
            }
        } else {
            if (ID == 0x421921)
                return new GuiThaumicAnvil(player.inventory, world, x, y, z);

            if (ID == 0x421920)
                return new GuiVoidAnvil(player.inventory, world, x, y, z);

            if (ID == 0x421919)
                return new GuiRevolver(player.inventory, world, x, y, z);
        }

        return null;
    }

    @Override
    public void registerRenderInformation() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDeconstructor.class, new RenderEntityDeconstructor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileOverchanter.class, new RenderOverchanter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNodeManipulator.class, new RenderNodeManipulator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNodeLinker.class, new RenderNodeLinker());

        MinecraftForgeClient.registerItemRenderer(TBItems.nodeFoci, new NodeFociRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TBBlocks.nodeManipulator), new NodeManipulatorItemRenderer());
        MinecraftForgeClient.registerItemRenderer(TBItems.revolver, new RenderRevolver());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TBBlocks.nodeLinker), new NodeLinkerItemRenderer());
        MinecraftForgeClient.registerItemRenderer(TBItems.castingBracelet, new CastingBraceletRenderer());
        MinecraftForgeClient.registerItemRenderer(TBItems.ukulele, new UkuleleRenderer());

        RenderingRegistry.registerBlockHandler(new ThaumicRelocatorRenderer());
        RenderingRegistry.registerBlockHandler(new CampfireRenderer());
        RenderingRegistry.registerBlockHandler(new BrazierRenderer());
        RenderingRegistry.registerEntityRenderingHandler(EntityRevolverBullet.class, new RenderBullet());

        MinecraftForge.EVENT_BUS.register(new RevolverEvents());
        MinecraftForge.EVENT_BUS.register(new TBEventHandler());
    }

    @Override
    public void lightning(World world, double sx, double sy, double sz, double ex, double ey, double ez, int dur, float curve, int speed, int type) {
        FXLightningBolt bolt = new FXLightningBolt(world, sx, sy, sz, ex, ey, ez, world.rand.nextLong(), dur, curve, speed);

        bolt.defaultFractal();
        bolt.setType(type);
        bolt.setWidth(0.125F);
        bolt.finalizeBolt();
    }

    public void sparkle(World w, double x, double y, double z, double dx, double dy, double dz, int color, float scale) {
        FXSparkle fx = new FXSparkle(w, x, y, z, dx, dy, dz, scale, color, 1);
        fx.noClip = true;
        ParticleEngine.instance.addEffect(w, fx);
    }

    public World clientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    public static class GuitarSound extends MovingSound {
        public int notUsingTicks = 0;

        protected GuitarSound(ResourceLocation snd) {
            super(snd);
        }

        @Override
        public void update() {
            if (Minecraft.getMinecraft().thePlayer == null || !Minecraft.getMinecraft().thePlayer.isUsingItem() || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem() == null || Minecraft.getMinecraft().thePlayer.isDead || Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() == null || !(Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemUkulele)) {
                ++notUsingTicks;
                if (notUsingTicks >= 8) {
                    TBEventHandler.clientUkuleleSoundPlayDelay = 0;
                    this.donePlaying = true;
                    notUsingTicks = 0;
                }
            } else {
                notUsingTicks = 0;
                this.xPosF = (float) Minecraft.getMinecraft().thePlayer.posX;
                this.yPosF = (float) Minecraft.getMinecraft().thePlayer.posY;
                this.zPosF = (float) Minecraft.getMinecraft().thePlayer.posZ;
            }
        }

    }

}
