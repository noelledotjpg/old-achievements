package com.nikohalfkino.achievements;

import com.nikohalfkino.achievements.network.PacketHandler;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(OldAchievements.MODID)
public class OldAchievements {
    public static final String MODID = "achievements";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OldAchievements(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, OldAchievementsConfig.SPEC);
        modEventBus.addListener(PacketHandler::register);
    }
}
