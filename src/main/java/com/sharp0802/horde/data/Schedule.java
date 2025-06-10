package com.sharp0802.horde.data;

import com.google.gson.JsonObject;
import com.sharp0802.horde.Horde;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Schedule {
    private final String _name;
    private final When[] _whens;
    private final Spawn[] _spawn;

    public Schedule(JsonObject json) throws Json.Exception {

        var nameMember = json.get("name");
        if (nameMember == null) {
            throw Json.missingField("schedule", "name");
        }

        _name = nameMember.getAsString();

        var whenMember = json.get("when");
        if (whenMember == null) {
            throw Json.missingField("schedule", "when");
        }

        var whens = whenMember.getAsJsonArray();
        _whens = new When[whens.size()];
        for (int i = 0; i < whens.size(); i++) {
            _whens[i] = When.from(whens.get(i).getAsJsonObject());
        }

        var spawnMember = json.get("spawn");
        if (spawnMember == null) {
            throw Json.missingField("schedule", "spawn");
        }

        var spawn = spawnMember.getAsJsonArray();
        var spawnList = new ArrayList<Spawn>();
        for (int i = 0; i < spawn.size(); i++) {

            var path = FMLPaths.CONFIGDIR.get()
                    .resolve(Horde.MODID)
                    .resolve(spawn.get(i).getAsString());
            String content;
            try {
                content = Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var scanner = new Scanner(content);
            while (scanner.hasNextLine()) {
                spawnList.add(new Spawn(scanner.nextLine()));
            }
            scanner.close();
        }

        _spawn = new Spawn[spawnList.size()];
        spawnList.toArray(_spawn);
    }

    public String getName() {
        return _name;
    }

    public boolean satisfy() {
        return Arrays.stream(_whens).allMatch(When::satisfy);
    }

    public void run(Player player) {
        player.sendSystemMessage(Component.literal("Schedule '" + _name + "' running..."));

        for (var spawn : _spawn) {
            spawn.run(player);
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("Schedule '").append(_name).append("'\n");
        builder.append("  - when:\n");
        for (var when : _whens) {
            builder.append("    + ").append(when.toString()).append("\n");
        }
        builder.append("  - spawn:\n");
        for (var spawn : _spawn) {
            builder.append("    + ").append(spawn.toString()).append("\n");
        }
        return builder.toString();
    }
}
