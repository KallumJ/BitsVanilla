package team.bits.vanilla.fabric.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.util.ServerInstance;

@Mixin(MinecraftDedicatedServer.class)
public class ServerInstanceMixin {

    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/resource/ServerResourceManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/server/dedicated/ServerPropertiesLoader;Lcom/mojang/datafixers/DataFixer;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfileRepository;Lnet/minecraft/util/UserCache;Lnet/minecraft/server/WorldGenerationProgressListenerFactory;)V",
            at = @At("TAIL")
    )
    public void onServerConstruct(
            Thread serverThread, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session,
            ResourcePackManager dataPackManager, ServerResourceManager serverResourceManager,
            SaveProperties saveProperties, ServerPropertiesLoader propertiesLoader, DataFixer dataFixer,
            MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache,
            WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {

        ServerInstance.set(MinecraftDedicatedServer.class.cast(this));
    }
}
