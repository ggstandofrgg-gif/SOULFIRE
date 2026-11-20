package dev.soulfire.mixin;

import dev.soulfire.data.SoulfireDataAccess;
import dev.soulfire.data.SoulfirePlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements SoulfireDataAccess {
    @Unique private final SoulfirePlayerData soulfire$data = new SoulfirePlayerData();

    @Override public SoulfirePlayerData soulfire$getData(){return soulfire$data;}

    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void soulfire$write(NbtCompound n, CallbackInfo ci){
        NbtCompound data=new NbtCompound();soulfire$data.writeNbt(data);n.put("SoulfireData",data);
    }

    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void soulfire$read(NbtCompound n, CallbackInfo ci){
        if(n.contains("SoulfireData")) soulfire$data.readNbt(n.getCompound("SoulfireData").orElseGet(NbtCompound::new));
    }
}
