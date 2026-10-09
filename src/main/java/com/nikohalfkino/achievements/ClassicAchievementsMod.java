package com.nikohalfkino.achievements;

import com.nikohalfkino.achievements.network.PacketHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ClassicAchievementsMod.MODID)
public class ClassicAchievementsMod {

    public static final String MODID = "achievements";
    public static final Logger LOGGER = LogManager.getLogger(ClassicAchievementsMod.class);

    public ClassicAchievementsMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ClassicAchievementsConfig.SPEC);
        PacketHandler.register();
    }
}
