package dev.soulfire.data;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public final class SoulfirePlayerData {
    private int souls;
    private int level = 1;
    private int unspentPoints;
    private int vigor, endurance, strength, dexterity, intelligence, faith, vitality;
    private int spark = 100;
    private float stamina = 100.0f;
    private int maxSpark = 100;
    private int lostSouls;
    private BlockPos lastBonfire;
    private boolean combatMode;
    private boolean wallGrab;
    private boolean ropeMode;

    public static void register() { }

    public static SoulfirePlayerData get(ServerPlayerEntity player) {
        return ((SoulfireDataAccess) player).soulfire$getData();
    }

    public void readNbt(NbtCompound n) {
        souls = n.getInt("Souls");
        level = Math.max(1, n.getInt("Level"));
        unspentPoints = n.getInt("Points");
        vigor=n.getInt("Vigor"); endurance=n.getInt("Endurance");
        strength=n.getInt("Strength"); dexterity=n.getInt("Dexterity");
        intelligence=n.getInt("Intelligence"); faith=n.getInt("Faith");
        vitality=n.getInt("Vitality"); spark=(n.contains("Spark") ? n.getInt("Spark") : 100);
        stamina = n.contains("Stamina") ? Math.max(0.0f, Math.min(maxStamina(), n.getFloat("Stamina"))) : maxStamina();
        maxSpark=Math.max(100,(n.contains("MaxSpark") ? n.getInt("MaxSpark") : 100));
        lostSouls=n.getInt("LostSouls");
        combatMode=n.getBoolean("CombatMode");
        wallGrab=n.getBoolean("WallGrab");
        ropeMode=n.getBoolean("RopeMode");
        if (n.contains("BonfireX") && n.contains("BonfireY") && n.contains("BonfireZ"))
            lastBonfire = new BlockPos(n.getInt("BonfireX"),n.getInt("BonfireY"),n.getInt("BonfireZ"));
    }

    public void writeNbt(NbtCompound n) {
        n.putInt("Souls",souls); n.putInt("Level",level); n.putInt("Points",unspentPoints);
        n.putInt("Vigor",vigor); n.putInt("Endurance",endurance); n.putInt("Strength",strength);
        n.putInt("Dexterity",dexterity); n.putInt("Intelligence",intelligence); n.putInt("Faith",faith);
        n.putInt("Vitality",vitality); n.putInt("Spark",spark); n.putInt("MaxSpark",maxSpark);
        n.putFloat("Stamina", stamina);
        n.putInt("LostSouls",lostSouls); n.putBoolean("CombatMode",combatMode);
        n.putBoolean("WallGrab",wallGrab); n.putBoolean("RopeMode",ropeMode);
        if(lastBonfire!=null){n.putInt("BonfireX",lastBonfire.getX());n.putInt("BonfireY",lastBonfire.getY());n.putInt("BonfireZ",lastBonfire.getZ());}
    }

    public int getSouls(){return souls;} public void addSouls(int n){souls=Math.max(0,souls+n);}
    public boolean spendSouls(int n){if(souls<n)return false;souls-=n;return true;}
    public int getLevel(){return level;} public int getPoints(){return unspentPoints;}
    public int levelCost(){return (int)Math.ceil(100*Math.pow(level,1.35));}
    public boolean buyLevel(){int c=levelCost();if(!spendSouls(c))return false;level++;unspentPoints++;return true;}
    public boolean spendPoint(String stat){
        if(unspentPoints<=0)return false;
        switch(stat){
            case "vigor" -> vigor++; case "endurance" -> { endurance++; stamina = Math.min(maxStamina(), stamina + 10.0f); }
            case "strength" -> strength++; case "dexterity" -> dexterity++;
            case "intelligence" -> intelligence++; case "faith" -> faith++;
            case "vitality" -> vitality++; default -> {return false;}
        }
        unspentPoints--; return true;
    }
    public int stat(String s){return switch(s){case "vigor"->vigor;case "endurance"->endurance;case "strength"->strength;case "dexterity"->dexterity;case "intelligence"->intelligence;case "faith"->faith;case "vitality"->vitality;default->0;};}
    public int getSpark(){return spark;} public int getMaxSpark(){return maxSpark;}
    public float getStamina(){return stamina;}
    public float maxStamina(){return 100.0f + endurance * 10.0f;}
    public void tickStamina(boolean sprinting, boolean grounded){
        if (sprinting) stamina = Math.max(0.0f, stamina - 0.45f);
        else if (grounded) stamina = Math.min(maxStamina(), stamina + 0.65f);
        else stamina = Math.min(maxStamina(), stamina + 0.15f);
    }
    public boolean spendStamina(float amount){
        if (amount < 0 || stamina < amount) return false;
        stamina -= amount;
        return true;
    }
    public boolean spendSpark(int n){if(spark<n)return false;spark-=n;return true;}
    public void restoreSpark(){spark=maxSpark;}
    public void addSparkCapacity(int n){maxSpark+=n;spark=maxSpark;}
    public int getLostSouls(){return lostSouls;} public void setLostSouls(int n){lostSouls=n;}
    public BlockPos getLastBonfire(){return lastBonfire;} public void setLastBonfire(BlockPos p){lastBonfire=p.toImmutable();}
    public boolean toggleCombat(){combatMode=!combatMode;return combatMode;}
    public boolean isCombat(){return combatMode;}
    public boolean toggleWallGrab(){wallGrab=!wallGrab;return wallGrab;}
    public boolean isWallGrab(){return wallGrab;}
    public boolean toggleRope(){ropeMode=!ropeMode;return ropeMode;}
    public boolean isRope(){return ropeMode;}
}
