package dev.soulfire.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.soulfire.SoulfireMod;
import dev.soulfire.data.SoulfirePlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import static net.minecraft.server.command.CommandManager.*;

public final class SoulfireCommands {
    public static void register(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("soulfire")
            .then(literal("help").executes(c->help(c.getSource())))
            .then(literal("stats").executes(c->stats(c.getSource())))
            .then(literal("levelup").executes(c->level(c.getSource())))
            .then(literal("upgrade").then(literal("vigor").executes(c->upgrade(c.getSource(),"vigor")))
                .then(literal("endurance").executes(c->upgrade(c.getSource(),"endurance")))
                .then(literal("strength").executes(c->upgrade(c.getSource(),"strength")))
                .then(literal("dexterity").executes(c->upgrade(c.getSource(),"dexterity")))
                .then(literal("intelligence").executes(c->upgrade(c.getSource(),"intelligence")))
                .then(literal("faith").executes(c->upgrade(c.getSource(),"faith")))
                .then(literal("vitality").executes(c->upgrade(c.getSource(),"vitality"))))
            .then(literal("combat").executes(c->combat(c.getSource())))
            .then(literal("roll").executes(c->roll(c.getSource())))
            .then(literal("dash").executes(c->dash(c.getSource())))
            .then(literal("wallgrab").executes(c->wall(c.getSource())))
            .then(literal("rope").executes(c->rope(c.getSource())))
            .then(literal("spark").then(literal("pulse").executes(c->pulse(c.getSource())))
                .then(literal("cleanse").executes(c->cleanse(c.getSource())))
                .then(literal("crystal").executes(c->crystal(c.getSource()))))
            .then(literal("warp").executes(c->warp(c.getSource())))
            .then(literal("recover").executes(c->recover(c.getSource())))
            .then(literal("souls").requires(s->s.hasPermissionLevel(2))
                .then(argument("amount",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,1000000))
                    .executes(c->{int a=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"amount");SoulfirePlayerData.get(c.getSource().getPlayerOrThrow()).addSouls(a);c.getSource().sendFeedback(()->Text.literal("§e+"+a+" душ"),false);return 1;})))
        );
    }
    private static int help(ServerCommandSource s){
        s.sendFeedback(()->Text.literal("§6SOULFIRE §8| §f/soulfire stats, levelup, upgrade <stat>, combat, roll, dash, wallgrab, rope, spark <pulse|cleanse|crystal>, warp, recover"),false);return 1;
    }
    private static int stats(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        s.sendFeedback(()->Text.literal("§6SOULFIRE §8— §7Уровень §e"+x.getLevel()+" §7| Души §e"+x.getSouls()+" §7| Очки §e"+x.getPoints()+" §7| Искра §b"+x.getSpark()+"/"+x.getMaxSpark()),false);
        for(String k:new String[]{"vigor","endurance","strength","dexterity","intelligence","faith","vitality"})s.sendFeedback(()->Text.literal("§7"+k+": §e"+x.stat(k)),false);return 1;
    }
    private static int level(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var x=SoulfirePlayerData.get(s.getPlayerOrThrow());int cost=x.levelCost();
        if(!x.buyLevel()){s.sendError(Text.literal("Нужно душ: "+cost));return 0;}
        s.sendFeedback(()->Text.literal("§6Уровень повышен. §7Доступно очков: §e"+x.getPoints()),false);return 1;
    }
    private static int upgrade(ServerCommandSource s,String stat)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendPoint(stat)){s.sendError(Text.literal("Нет очков характеристик или неверная характеристика."));return 0;}
        if(stat.equals("vigor"))p.setHealth(Math.min(p.getMaxHealth(),p.getHealth()+2));
        s.sendFeedback(()->Text.literal("§a"+stat+" повышена до "+x.stat(stat)+"§7. Осталось очков: "+x.getPoints()),false);return 1;
    }
    private static int combat(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var x=SoulfirePlayerData.get(s.getPlayerOrThrow());boolean on=x.toggleCombat();
        s.sendFeedback(()->Text.literal(on?"§cБоевой режим включён.":"§7Боевой режим выключен."),false);return 1;
    }
    private static int roll(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendSpark(12)){s.sendError(Text.literal("Недостаточно Искры."));return 0;}
        Vec3d look=p.getRotationVec(1.0f);Vec3d v=new Vec3d(look.x,0,look.z).normalize().multiply(1.25).add(0,0.28,0);
        p.setVelocity(v);p.velocityModified=true;p.getWorld().playSound(null,p.getBlockPos(),net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,net.minecraft.sound.SoundCategory.PLAYERS,0.5f,1.4f);
        s.sendFeedback(()->Text.literal("§bПерекат! §7Искра: "+x.getSpark()),false);return 1;
    }
    private static int dash(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendSpark(18)){s.sendError(Text.literal("Недостаточно Искры."));return 0;}
        Vec3d look=p.getRotationVec(1).normalize();p.setVelocity(look.multiply(1.7).add(0,0.35,0));p.velocityModified=true;
        s.sendFeedback(()->Text.literal("§bРывок!"),false);return 1;
    }
    private static int wall(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);boolean on=x.toggleWallGrab();
        if(on){p.setVelocity(p.getVelocity().x,0.08,p.getVelocity().z);p.velocityModified=true;}
        s.sendFeedback(()->Text.literal(on?"§bРежим захвата включён. Подойди к стене и используй прыжок.":"§7Захват выключен."),false);return 1;
    }
    private static int rope(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var x=SoulfirePlayerData.get(s.getPlayerOrThrow());boolean on=x.toggleRope();
        s.sendFeedback(()->Text.literal(on?"§bРежим каната включён.":"§7Режим каната выключен."),false);return 1;
    }
    private static int pulse(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendSpark(25)){s.sendError(Text.literal("Недостаточно Искры."));return 0;}
        var w=p.getWorld();var pos=p.getBlockPos();
        w.playSound(null,pos,net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE,net.minecraft.sound.SoundCategory.PLAYERS,1,1.5f);
        p.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.GLOWING,100,0));
        s.sendFeedback(()->Text.literal("§bИмпульс Искры выпущен."),false);return 1;
    }
    private static int cleanse(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendSpark(35)){s.sendError(Text.literal("Недостаточно Искры."));return 0;}
        p.clearStatusEffects();s.sendFeedback(()->Text.literal("§bИскра очистила твои негативные эффекты."),false);return 1;
    }
    private static int crystal(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        if(!x.spendSpark(50)){s.sendError(Text.literal("Недостаточно Искры."));return 0;}
        x.addSparkCapacity(10);s.sendFeedback(()->Text.literal("§bКристалл создан: максимальная Искра +10."),false);return 1;
    }
    private static int warp(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var pos=SoulfirePlayerData.get(p).getLastBonfire();
        if(pos==null){s.sendError(Text.literal("Сначала активируй костёр."));return 0;}
        p.teleport(p.getServerWorld(),pos.getX()+0.5,pos.getY()+1.0,pos.getZ()+0.5,java.util.Set.of(),p.getYaw(),p.getPitch(),false);
        s.sendFeedback(()->Text.literal("§6Ты вернулся к костру."),false);return 1;
    }
    private static int recover(ServerCommandSource s)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=s.getPlayerOrThrow();var x=SoulfirePlayerData.get(p);
        int amount=x.getLostSouls();if(amount<=0){s.sendError(Text.literal("Нет потерянных душ."));return 0;}
        x.addSouls(amount);x.setLostSouls(0);s.sendFeedback(()->Text.literal("§eВозвращено душ: "+amount),false);return 1;
    }
}
