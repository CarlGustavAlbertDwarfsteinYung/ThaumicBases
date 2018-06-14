package tb.client.render.item;

import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public class UkuleleRenderer implements IItemRenderer {

    public static final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("thaumicbases", "models/ukulele/ukulele.obj"));
    public static final ResourceLocation base = new ResourceLocation("thaumicbases", "textures/items/ukulele/ukulelebase.png");
    public static final ResourceLocation strings = new ResourceLocation("thaumicbases", "textures/items/ukulele/ukulelestrings.png");
    public static final ResourceLocation handle = new ResourceLocation("thaumicbases", "textures/items/ukulele/ukulelehandle.png");

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) { return true; }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        //RenderHelper.disableStandardItemLighting();
        GL11.glScaled(0.45, 0.45, 0.45);
        GL11.glTranslated(1, 0.5, 1);

        if (type == ItemRenderType.INVENTORY) {
            GL11.glRotated(45, 0, 1, 0);
            GL11.glScaled(1, 1, 1);
        }

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glScaled(2, 2, 2);
            GL11.glTranslated(2, 0.5, -1);
            GL11.glRotated(45, 1, 0, 0);
            GL11.glRotated(45, 0, 0, 1);

             GL11.glPushMatrix();

            Minecraft.getMinecraft().getTextureManager().bindTexture(Minecraft.getMinecraft().thePlayer.getLocationSkin());

            GL11.glPushMatrix();

            GL11.glTranslated(0, 1, 2);
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(-90, 0, 0, 1);
            GL11.glTranslated(0, -1.3, 0);
            GL11.glRotated(20, 1, 0, 0);
            GL11.glRotated(-20, 0, 0, 1);
            GL11.glScaled(1.5, 1.5, 1.5);

            if (Minecraft.getMinecraft().thePlayer.isUsingItem()) {
                float mOffset = Minecraft.getMinecraft().thePlayer.ticksExisted % 30 * 12;
                float nOffset = Minecraft.getMinecraft().thePlayer.ticksExisted % 20 * 18;
                float oOffset = Minecraft.getMinecraft().thePlayer.ticksExisted % 10 * 36;
                GL11.glTranslated(0.3 + Math.sin(Math.toRadians(mOffset)) / 6, 0, 0);
                GL11.glTranslated(0, Math.sin(Math.toRadians(nOffset)) / 10, 0);
                GL11.glTranslated(0, 0, Math.sin(Math.toRadians(oOffset)) / 20);
            }

            GL11.glPopMatrix();

            GL11.glPushMatrix();

            GL11.glTranslated(-0.5, 0.8, -1);
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(-90, 0, 0, 1);
            GL11.glTranslated(0, -1.3, 0);
            GL11.glRotated(20, 1, 0, 0);
            GL11.glRotated(-50, 0, 0, 1);
            GL11.glScaled(1.5, 3, 1.5);

            if (Minecraft.getMinecraft().thePlayer.isUsingItem()) {
                float mOffset = System.currentTimeMillis() / 40 % 20 * 18;
                float oOffset = Minecraft.getMinecraft().thePlayer.ticksExisted % 10 * 36;
                GL11.glTranslated(Math.sin(Math.toRadians(-mOffset)) / 6, 0, 0);
                GL11.glTranslated(0, 0, -0.03 + Math.sin(Math.toRadians(-oOffset)) / 100);
            }

            GL11.glPopMatrix();

            GL11.glPopMatrix();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(base);
        model.renderPart("base_Cube.001");
        Minecraft.getMinecraft().renderEngine.bindTexture(strings);
        model.renderPart("strings_Cube.003");
        Minecraft.getMinecraft().renderEngine.bindTexture(handle);
        model.renderPart("hand_Cube.002");
        //RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }
}
