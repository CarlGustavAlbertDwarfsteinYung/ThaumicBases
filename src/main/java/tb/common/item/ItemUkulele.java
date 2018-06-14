package tb.common.item;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;

import DummyCore.Utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import tb.common.entity.ai.EntityLivingAITempt;
import tb.common.event.TBEventHandler;
import tb.core.TBCore;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.potions.PotionWarpWard;

public class ItemUkulele extends ItemAmuletVis {

    public static final String[] types = new String[]{
            "simple",
            "knowledge",
            "calming",
            "electric",
            "resistance",
            "buffing",
            "confusion",
            "growth",
            "love"
    };

    public static final boolean[] isCostPerTick = new boolean[]{
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            true,
            true
    };

    public static final AspectList[] costs = new AspectList[]{
            new AspectList().add(Aspect.AIR, 0).add(Aspect.FIRE, 0).add(Aspect.ENTROPY, 0).add(Aspect.ORDER, 0).add(Aspect.EARTH, 0).add(Aspect.WATER, 0),
            new AspectList().add(Aspect.AIR, 200).add(Aspect.FIRE, 200).add(Aspect.ENTROPY, 50).add(Aspect.ORDER, 100).add(Aspect.EARTH, 50).add(Aspect.WATER, 50),
            new AspectList().add(Aspect.AIR, 100).add(Aspect.FIRE, 50).add(Aspect.ENTROPY, 20).add(Aspect.ORDER, 200).add(Aspect.EARTH, 100).add(Aspect.WATER, 100),
            new AspectList().add(Aspect.AIR, 50).add(Aspect.FIRE, 50).add(Aspect.ENTROPY, 25).add(Aspect.ORDER, 25).add(Aspect.EARTH, 5).add(Aspect.WATER, 10),
            new AspectList().add(Aspect.AIR, 2).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.ORDER, 8).add(Aspect.EARTH, 10).add(Aspect.WATER, 5),
            new AspectList().add(Aspect.AIR, 2000).add(Aspect.FIRE, 2000).add(Aspect.ENTROPY, 2000).add(Aspect.ORDER, 2000).add(Aspect.EARTH, 2000).add(Aspect.WATER, 2000),
            new AspectList().add(Aspect.AIR, 100).add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 200).add(Aspect.ORDER, 25).add(Aspect.EARTH, 25).add(Aspect.WATER, 50),
            new AspectList().add(Aspect.AIR, 2).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.ORDER, 10).add(Aspect.EARTH, 10).add(Aspect.WATER, 10),
            new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 5).add(Aspect.ORDER, 8).add(Aspect.EARTH, 8).add(Aspect.WATER, 10)
    };

    public static final int[] soundDelays = new int[]{
            27 * 20 * 2,
            4 * 20 * 2,
            20 * 20 * 2,
            9 * 20 * 2,
            9 * 20 * 2,
            10 * 20 * 2,
            16 * 20 * 2,
            8 * 20 * 2,
            5 * 20 * 2
    };

    public static final DecimalFormat formatForAspects = new DecimalFormat("###.##");

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.rare;
    }

    public AspectList getCostWithDiscount(ItemStack is, EntityPlayer p, float originalCost, AspectList al) {
        AspectList originalAL = al.copy();
        for (int i1 = 0; i1 < 6; ++i1) {
            Aspect a = originalAL.getAspects()[i1];
            int costi = originalAL.getAmount(a);
            originalAL.reduce(a, costi);
            float originalDiscount = originalCost;
            originalDiscount -= WandManager.getTotalVisDiscount(p, a);
            costi *= originalDiscount;

            originalAL.add(a, costi);
        }
        return originalAL;
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        Vec3 lookVec = player.getLookVec();
        AspectList cost = getCostWithDiscount(stack, player, 0.75F, costs[Math.min(stack.getItemDamage(), costs.length - 1)]);
        player.worldObj.spawnParticle("note", player.posX + lookVec.xCoord / 5 + MathUtils.randomDouble(itemRand) / 2, player.posY + 0.3 + lookVec.yCoord / 2 + MathUtils.randomDouble(itemRand) / 10 + 0.1D, player.posZ + lookVec.zCoord / 2 + MathUtils.randomDouble(itemRand) / 5, itemRand.nextDouble(), itemRand.nextDouble(), itemRand.nextDouble());

        if (player.worldObj.isRemote && TBEventHandler.clientUkuleleSoundPlayDelay <= 0) {
            TBEventHandler.clientUkuleleSoundPlayDelay = soundDelays[Math.min(stack.getItemDamage(), soundDelays.length - 1)];
            TBCore.proxy.playGuitarSound("thaumicbases:guitar." + types[Math.min(stack.getItemDamage(), types.length - 1)]);
        }

        // KNOWLEDGE
        if (stack.getItemDamage() == 1 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 80 == 0) {
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 8, player.posY - 8, player.posZ - 8, player.posX + 8, player.posY + 8, player.posZ + 8);
                List<EntityPlayer> players = player.worldObj.getEntitiesWithinAABB(EntityPlayer.class, aabb);

                for (int num = 0; num < players.size(); num++) {
                    EntityPlayer p = players.get(num);
                    boolean addAspect = p == player ? true : p.worldObj.rand.nextBoolean();

                    if (!addAspect)
                        continue;

                    int amount = p.worldObj.rand.nextInt(8) + 3;
                    if (!player.worldObj.isRemote && this.consumeAllVis(stack, player, cost, true, false)) {
                        EntityXPOrb xp = new EntityXPOrb(player.worldObj, player.posX, player.posY, player.posZ, amount);
                        player.worldObj.spawnEntityInWorld(xp);
                    }
                }
            }
        } //CALMING
        else if (stack.getItemDamage() == 2 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 20 == 0) {
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 4, player.posY - 4, player.posZ - 4, player.posX + 4, player.posY + 4, player.posZ + 4);
                List<EntityLivingBase> entities = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

                for (int num = 0; num < entities.size(); num++) {
                    EntityLivingBase entity = entities.get(num);

                    if (entity.isDead)
                            continue;

                    if (entity.isEntityUndead())
                        entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 1);
                    else if (!entity.isPotionActive(Potion.regeneration))
                        entity.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0, false));

                    if (entity instanceof EntityPlayer) {
                        EntityPlayer p = (EntityPlayer)entity;
                        if (this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                            boolean hasEffect = p.getActivePotionEffect(PotionWarpWard.instance) != null;
                            if (!hasEffect) {
                                if (!p.worldObj.isRemote)
                                    p.addPotionEffect(new PotionEffect(PotionWarpWard.instance.getId(), 200, 0, false));
                            } else {
                                PotionEffect effect = p.getActivePotionEffect(PotionWarpWard.instance);
                                try {
                                    Field dur = PotionEffect.class.getDeclaredFields()[2];
                                    dur.setAccessible(true);
                                    dur.setInt(effect, dur.getInt(effect) + 120);
                                    dur.setAccessible(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        }  // ELECTRIC
        else if (stack.getItemDamage() == 3 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            double dx = player.posX + MathUtils.randomDouble(itemRand) * 16;
            double dy = player.posY + MathUtils.randomDouble(itemRand) * 16;
            double dz = player.posZ + MathUtils.randomDouble(itemRand) * 16;
            if (player.worldObj.isRemote && player.worldObj.rand.nextDouble() <= 0.1D)
                Thaumcraft.proxy.arcLightning(player.worldObj, player.posX, player.posY - 1, player.posZ, dx, dy, dz, 0.2F, 0.5F, 1, 1);

            f:
            for (int i = 1; i <= 16; ++i) {
                double px = lookVec.xCoord * i + player.posX;
                double py = lookVec.yCoord * i + player.posY + player.getEyeHeight();
                double pz = lookVec.zCoord * i + player.posZ;
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(px - 0.5D, py - 0.5D, pz - 0.5D, px + 0.5D, py + 0.5D, pz + 0.5D);
                List<EntityLivingBase> mobs = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                for (int num = 0; num < mobs.size(); num++) {
                    EntityLivingBase e = mobs.get(num);

                    if (e == player)
                        continue;

                    if (e.isDead)
                        continue;

                    if (e.hurtTime > 0)
                        continue;

                    boolean attack = true;

                    if (!this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                        attack = false;
                    }

                    if (attack) {
                        if (e instanceof IAnimals || e instanceof EntityPlayer)
                            e.attackEntityFrom(DamageSource.causePlayerDamage(player), 3);
                        else
                            e.attackEntityFrom(DamageSource.causePlayerDamage(player), 9);

                        if (player.worldObj.isRemote)
                            Thaumcraft.proxy.arcLightning(player.worldObj, player.posX, player.posY - 1, player.posZ, e.posX, e.posY, e.posZ, 0.2F, 0.5F, 1, 1);

                        player.worldObj.playSound(player.posX, player.posY, player.posZ, "thaumcraft:jacobs", 1, player.worldObj.rand.nextFloat() * 2, false);
                        player.worldObj.playSound(e.posX, e.posY, e.posZ, "thaumcraft:jacobs", 1, player.worldObj.rand.nextFloat() * 2, false);

                    }
                    break f;
                }
            }
        } // RESISTANCE
        if (stack.getItemDamage() == 4 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 10 == 0) {
                for (int i = 0; i < cost.size(); ++i) {
                    cost.add(cost.getAspects()[i], cost.getAmount(cost.getAspects()[i]) * 9);
                }
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("playerhealth")) {
                    if (this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                        if (player.getHealth() < stack.getTagCompound().getDouble("playerhealth")) {
                            player.setHealth((float) stack.getTagCompound().getDouble("playerhealth"));
                        } else {
                            stack.getTagCompound().setDouble("playerhealth", player.getHealth());
                        }
                    }
                } else {
                    if (!stack.hasTagCompound())
                        stack.setTagCompound(new NBTTagCompound());

                    stack.getTagCompound().setDouble("playerhealth", player.getHealth());
                }

            }
        } // BUFFING
        if (stack.getItemDamage() == 5 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 100 == 0) {
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 4, player.posY - 4, player.posZ - 4, player.posX + 4, player.posY + 4, player.posZ + 4);
                List<EntityPlayer> players = player.worldObj.getEntitiesWithinAABB(EntityPlayer.class, aabb);
                for (EntityPlayer p : players) {
                    if (this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                        Potion[] pots = new Potion[]{Potion.damageBoost, Potion.moveSpeed, Potion.digSpeed, Potion.nightVision, Potion.waterBreathing, Potion.regeneration};
                        for (Potion pp : pots) {
                            boolean hasEffect = p.getActivePotionEffect(pp) != null;
                            if (!hasEffect) {
                                if (!p.worldObj.isRemote)
                                    p.addPotionEffect(new PotionEffect(pp.id, 600, 0, false));
                            } else {
                                PotionEffect effect = p.getActivePotionEffect(pp);
                                try {
                                    Field dur = PotionEffect.class.getDeclaredFields()[2];
                                    dur.setAccessible(true);
                                    dur.setInt(effect, dur.getInt(effect) + 600);
                                    dur.setAccessible(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        } // CONFUSION
        if (stack.getItemDamage() == 6 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 20 == 0)
                for (int i = 1; i <= 8; ++i) {
                    double px = lookVec.xCoord * i + player.posX;
                    double py = lookVec.yCoord * i + player.posY + player.getEyeHeight();
                    double pz = lookVec.zCoord * i + player.posZ;
                    AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(px - 0.5D, py - 0.5D, pz - 0.5D, px + 0.5D, py + 0.5D, pz + 0.5D);
                    List<EntityLivingBase> mobs = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    for (int num1 = 0; num1 < mobs.size(); num1++) {
                        EntityLivingBase e = mobs.get(num1);
                        if (e == player)
                            continue;

                        if (e.isDead)
                            continue;

                        boolean attack = true;

                        if (!this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                            attack = false;
                        }

                        if (attack) {
                            if (e instanceof EntityAnimal) {
                                EntityAnimal.class.cast(e).attackEntityFrom(DamageSource.causePlayerDamage(player), 0);
                            }

                            if (e instanceof IMob) {
                                AxisAlignedBB nearbyMobs = AxisAlignedBB.getBoundingBox(e.posX - 16, e.posY - 6, e.posZ - 16, e.posX + 16, e.posY + 6, e.posZ + 16);
                                List<EntityLivingBase> nMobs = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, nearbyMobs);

                                for (int num2 = 0; num2 < nMobs.size(); num2++) {
                                    EntityLivingBase nB = nMobs.get(num2);

                                    if (nB.isDead || nB == e) {
                                        nMobs.remove(num2);
                                    }
                                }

                                if (!nMobs.isEmpty()) {
                                    EntityLivingBase base = nMobs.get(player.worldObj.rand.nextInt(nMobs.size()));

                                    if (e instanceof EntityZombie) {
                                        EntityZombie zom = (EntityZombie)e;

                                        if (zom.tasks.taskEntries.size() >= 3) {
                                            EntityAITasks.EntityAITaskEntry task = (EntityAITasks.EntityAITaskEntry) zom.tasks.taskEntries.get(3);
                                            if (task.action != null && !(task.action instanceof EntityAIAttackOnCollide))
                                                zom.tasks.addTask(3, new EntityAIAttackOnCollide(zom, IMob.class, 1.0D, true));
                                        }

                                        if (zom.targetTasks.taskEntries.size() >= 3) {
                                            EntityAITasks.EntityAITaskEntry task = (EntityAITasks.EntityAITaskEntry) zom.targetTasks.taskEntries.get(3);
                                            if (task.action != null && !(task.action instanceof EntityAINearestAttackableTarget))
                                                zom.targetTasks.addTask(3, new EntityAINearestAttackableTarget(zom, IMob.class, 0, false));
                                        }
                                    }

                                    if (base instanceof EntityZombie) {
                                        EntityZombie zom = (EntityZombie)base;

                                        if (zom.tasks.taskEntries.size() >= 3) {
                                            EntityAITasks.EntityAITaskEntry task = (EntityAITasks.EntityAITaskEntry) zom.tasks.taskEntries.get(3);
                                            if (task.action != null && !(task.action instanceof EntityAIAttackOnCollide))
                                                zom.tasks.addTask(3, new EntityAIAttackOnCollide(zom, IMob.class, 1.0D, true));
                                        }

                                        if (zom.targetTasks.taskEntries.size() >= 3) {
                                            EntityAITasks.EntityAITaskEntry task = (EntityAITasks.EntityAITaskEntry) zom.targetTasks.taskEntries.get(3);
                                            if (task.action != null && !(task.action instanceof EntityAINearestAttackableTarget))
                                                zom.targetTasks.addTask(3, new EntityAINearestAttackableTarget(zom, IMob.class, 0, false));
                                        }
                                    }

                                    e.attackEntityFrom(DamageSource.causeMobDamage(base),0);
                                    e.setRevengeTarget(base);
                                    e.setLastAttacker(base);

                                    base.attackEntityFrom(DamageSource.causeMobDamage(e), 0);
                                    base.setRevengeTarget(e);
                                    base.setLastAttacker(e);


                                }
                            }

                            if (e instanceof EntityPlayer) {
                                e.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0, false));
                                e.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 0, false));
                            }
                        }
                    }
                }
        } // GROWTH
        if (stack.getItemDamage() == 7 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 10 == 0) {
                for (int i = 0; i < cost.size(); ++i) {
                    cost.add(cost.getAspects()[i], cost.getAmount(cost.getAspects()[i]) * 9);
                }
                if (this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                    for (int i = 0; i < 64; ++i) {
                        int dx = MathHelper.floor_double(player.posX + MathUtils.randomDouble(itemRand) * 6);
                        int dy = MathHelper.floor_double(player.posY - 0.5 + MathUtils.randomDouble(itemRand));
                        int dz = MathHelper.floor_double(player.posZ + MathUtils.randomDouble(itemRand) * 6);
                        Block b = player.worldObj.getBlock(dx, dy, dz);
                        if (!b.isAir(player.worldObj, dx, dy, dz)) {
                            if (b instanceof IGrowable) { //bonemeal effect
                                IGrowable igrowable = (IGrowable) b;
                                if (igrowable.func_149851_a(player.worldObj, dx, dy, dz, player.worldObj.isRemote)) {
                                    if (!player.worldObj.isRemote) {
                                        if (igrowable.func_149852_a(player.worldObj, itemRand, dx, dy, dz)) {
                                            igrowable.func_149853_b(player.worldObj, itemRand, dx, dy, dz);
                                        }
                                    }
                                }
                            } else if (b instanceof IPlantable) {
                                b.randomDisplayTick(player.worldObj, dx, dy, dz, player.worldObj.rand);
                            }
                        }
                    }
                    AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 16, player.posY - 4, player.posZ - 16, player.posX + 16, player.posY + 4, player.posZ + 16);
                    List entities = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    for (int an = 0; an < entities.size(); an++) {
                        if ((entities.get(an)) instanceof EntityAgeable) {
                            EntityAgeable ageable = (EntityAgeable) entities.get(an);

                            if (ageable.isDead)
                                continue;

                            if (ageable.isChild() && itemRand.nextFloat() < 0.2) {
                                ageable.addGrowth(1);
                            }
                        }
                    }
                }
            }
        } // LOVE
        if (stack.getItemDamage() == 8 && (this.getVis(stack, Aspect.AIR) > 0 && this.getVis(stack, Aspect.FIRE) > 0 && this.getVis(stack, Aspect.WATER) > 0 && this.getVis(stack, Aspect.EARTH) > 0 && this.getVis(stack, Aspect.ORDER) > 0 && this.getVis(stack, Aspect.ENTROPY) > 0)) {
            if (count % 10 == 0) {
                for (int i = 0; i < cost.size(); ++i) {
                    cost.add(cost.getAspects()[i], cost.getAmount(cost.getAspects()[i]) * 9);
                }
                if (this.consumeAllVis(stack, player, cost, !player.worldObj.isRemote, false)) {
                    AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 16, player.posY - 4, player.posZ - 16, player.posX + 16, player.posY + 4, player.posZ + 16);
                    List entities = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

                    for (int an = 0; an < entities.size(); an++) {
                        EntityLivingBase entity = (EntityLivingBase)entities.get(an);

                        if (entity.isDead)
                            continue;

                        if (entity instanceof EntityAnimal) {
                            EntityAnimal animal = (EntityAnimal)entity;

                            if (animal.isInLove())
                                entities.remove(an);
                            else
                                animal.func_146082_f(player);

                        } else if (entity instanceof EntityVillager) {
                            EntityVillager villager = (EntityVillager)entity;

                            if (!villager.tasks.taskEntries.contains(EntityAITempt.class))
                                villager.tasks.addTask(villager.tasks.taskEntries.size(), new EntityLivingAITempt(villager, 1.1D, this, false));
                        }
                    }
                }
            }
        }

        if (TBEventHandler.clientUkuleleSoundPlayDelay > 0)
            TBEventHandler.clientUkuleleSoundPlayDelay--;
    }

    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int count) {
        if (!itemstack.hasTagCompound())
            itemstack.setTagCompound(new NBTTagCompound());
        itemstack.getTagCompound().removeTag("playerhealth");
    }

    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.none;
    }

    public int getMaxItemUseDuration(ItemStack itemstack) {
        return Integer.MAX_VALUE;
    }

    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        player.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));

        return itemstack;
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return false;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return false;
    }

    @Override
    public int getMaxVis(ItemStack stack) {
        return 30000;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        AspectList cost = getCostWithDiscount(stack, player, 0.75F, costs[Math.min(stack.getItemDamage(), costs.length - 1)]);
        list.add(StatCollector.translateToLocal("tb.ukulele.type." + types[Math.min(stack.getItemDamage(), types.length - 1)]));
        list.add("");
        list.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("item.capacity.text") + " " + getMaxVis(stack) / 100);
        if (stack.hasTagCompound())
            for (Aspect aspect : Aspect.getPrimalAspects())
                if (stack.getTagCompound().hasKey(aspect.getTag())) {
                    String amount = formatForAspects.format(stack.getTagCompound().getInteger(aspect.getTag()) / 100.0F);
                    list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount + " (" + formatForAspects.format((float) cost.getAmount(aspect) / 100) + "/" + (isCostPerTick[stack.getItemDamage()] ? "tick" : "use") + ")");
                }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void getSubItems(Item itm, CreativeTabs tabs, List lst) {
        for (int i = 0; i < types.length; ++i) {
            lst.add(new ItemStack(itm, 1, i));
            ItemStack max = new ItemStack(itm, 1, i);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(Aspect.AIR.getTag(), this.getMaxVis(max));
            tag.setInteger(Aspect.FIRE.getTag(), this.getMaxVis(max));
            tag.setInteger(Aspect.WATER.getTag(), this.getMaxVis(max));
            tag.setInteger(Aspect.EARTH.getTag(), this.getMaxVis(max));
            tag.setInteger(Aspect.ENTROPY.getTag(), this.getMaxVis(max));
            tag.setInteger(Aspect.ORDER.getTag(), this.getMaxVis(max));
            max.setTagCompound(tag);
            lst.add(max);
        }
    }

}
