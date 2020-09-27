package net.environmentz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.environmentz.effect.ColdEffect;
import net.environmentz.init.ConfigInit;
import net.environmentz.init.EffectInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
  private int coldnessTimer;
  private int warmingTimer;

  public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(method = "tick", at = @At("TAIL"))
  public void tickMixin(CallbackInfo info) {
    Object object = this;
    PlayerEntity playerEntity = (PlayerEntity) object;
    if (!playerEntity.isCreative()) {
      if (this.world.getBiome(this.getBlockPos()).getTemperature() <= 0.0F && !ColdEffect.isWarmBlockNearBy(this)) {
        if (!ColdEffect.hasWarmClothing(this)) {
          coldnessTimer++;
          if (coldnessTimer >= ConfigInit.CONFIG.cold_tick_interval) {
            this.addStatusEffect(new StatusEffectInstance(EffectInit.COLDNESS,
                ConfigInit.CONFIG.cold_damage_effect_time, 0, false, false, true));
            coldnessTimer = 0;
          }
        }
      } else if (coldnessTimer > 0) {
        coldnessTimer = 0;
      }
      if (this.hasStatusEffect(EffectInit.COLDNESS)) {
        if (ColdEffect.isWarmBlockNearBy(this)) {
          warmingTimer++;
          if (warmingTimer >= ConfigInit.CONFIG.heating_up_interval) {
            int coldDuration = this.getStatusEffect(EffectInit.COLDNESS).getDuration();
            this.removeStatusEffect(EffectInit.COLDNESS);
            this.addStatusEffect(new StatusEffectInstance(EffectInit.COLDNESS,
                coldDuration - ConfigInit.CONFIG.heating_up_cold_tick_decrease, 0, false, false, true));
            warmingTimer = 0;
          }
        }
      }
    }
  }
}
