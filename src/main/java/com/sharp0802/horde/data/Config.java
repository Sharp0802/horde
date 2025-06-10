package com.sharp0802.horde.data;

import com.google.gson.JsonArray;
import com.sharp0802.horde.Horde;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Config
{
    private static final Gson GSON = new Gson();

    private static final List<Schedule> _schedules = new ArrayList<>();

    private static void handleScheduleJson(JsonArray json) throws Json.Exception {
        for (var element: json) {
            var object = element.getAsJsonObject();
            _schedules.add(new Schedule(object));
        }
    }

    public static void loadSchedule(Path file) throws Json.Exception {
        if (!Files.exists(file)) {
            return;
        }

        try (var reader = Files.newBufferedReader(file)) {
            var json = GSON.fromJson(reader, JsonArray.class);
            handleScheduleJson(json);
        } catch (IOException e) {
            throw Json.couldOpen(file.toString(), e);
        }
    }

    public static void load() throws Json.Exception {
        var condStr = FMLPaths.CONFIGDIR.get().resolve(Horde.MODID).resolve("default.json");
        loadSchedule(condStr);
    }

    public static void reset() {
        _schedules.clear();
    }

    public static List<Schedule> getSchedules() {
        return _schedules;
    }
}
