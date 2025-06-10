package com.sharp0802.horde;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import com.sharp0802.horde.data.Config;
import com.sharp0802.horde.data.Json;
import com.sharp0802.horde.data.Schedule;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Mod(Horde.MODID)
public class Horde {
    public static final String MODID = "horde";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Horde(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void reload() throws Json.Exception {
        Config.reset();
        Config.load();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        try {
            reload();
        } catch (Json.Exception e) {
            // it will be displayed on client error page
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        var players = event.getServer().getPlayerList().getPlayers();

        for (var schedule : Config.getSchedules()) {
            if (!schedule.satisfy()) {
                continue;
            }

            for (var player : players) {
                schedule.run(player);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("horde")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            try {
                                reload();
                                context.getSource().sendSuccess(() -> Component.literal("horde data reloaded!"), false);
                            } catch (Json.Exception e) {
                                context.getSource().sendFailure(Component.literal(e.toString()));
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("dump")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            var builder = new StringBuilder();
                            for (var schedule : Config.getSchedules()) {
                                builder.append(schedule);
                            }
                            var path = FMLPaths.GAMEDIR.get().resolve("horde.dump.txt");
                            try {
                                Files.writeString(path, builder.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                                context.getSource().sendSuccess(() -> Component.literal("Data dumped at '" + path + "'"), false);
                            } catch (IOException e) {
                                context.getSource().sendFailure(Component.literal(e.toString()));
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("run")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var name = StringArgumentType.getString(context, "name");

                                    Schedule schedule = null;
                                    for (var registered : Config.getSchedules()) {
                                        if (registered.getName().equalsIgnoreCase(name)) {
                                            schedule = registered;
                                            break;
                                        }
                                    }
                                    if (schedule == null) {
                                        context.getSource().sendFailure(Component.literal("Schedule not found!"));
                                        return 1;
                                    }

                                    var players = context.getSource().getServer().getPlayerList().getPlayers();
                                    for (var player : players) {
                                        schedule.run(player);
                                    }

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("tick")
                        .executes(context -> {
                            long tick = Time.getTick();
                            context.getSource().sendSuccess(() -> Component.literal(Long.toString(tick)), false);
                            return 1;
                        })
                )
        );
    }
}
