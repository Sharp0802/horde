package com.sharp0802.horde.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class Spawn {
    private final ResourceLocation _entity;
    private final float _multiplier;
    private final float _probability;
    private final float _number;

    public Spawn(String line) {
        var terms = line.split(",");

        _entity = ResourceLocation.parse(terms[0]);
        _number = Float.parseFloat(terms[1]);

        if (terms.length <= 2) {
            _probability = 1;
        } else if (terms[2].endsWith("%")) {
            terms[2] = terms[2].substring(0, terms[2].length() - 1);
            _probability = Float.parseFloat(terms[2]) * 0.01f;
        } else {
            _probability = Float.parseFloat(terms[2]);
        }

        if (terms.length <= 3) {
            _multiplier = 1;
        } else {
            _multiplier = Float.parseFloat(terms[3]);
        }
    }

    private static boolean isFilled(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.WATER) || state.isSolidRender(level, pos);
    }

    public void run(Player player) {
        var type = ForgeRegistries.ENTITY_TYPES.getValue(_entity);
        if (type == null) {
            player.sendSystemMessage(Component.literal("error: entity '" + _entity.toString() + "' not found"));
            return;
        }
        if (!type.canSummon()) {
            player.sendSystemMessage(Component.literal("error: entity '" + _entity.toString() + "' is not summonable"));
            return;
        }

        var level = player.level();
        var position = player.blockPosition();

        for (var i = 0; i < _multiplier; i++) {
            if (Math.random() > _probability) {
                return;
            }

            var dX = (int)((Math.random() * 0.3 + 0.7) * 35);
            var dZ = (int)((Math.random() * 0.3 + 0.7) * 35);

            var dP = position.offset(dX, 0, dZ);
            if (!isFilled(level, dP.below())) {
                for (int dY = 0; dY < 1000; dY++) {
                    dP = dP.below();
                    if (isFilled(level, dP)) {
                        break;
                    }
                }
            } else if (isFilled(level, dP)) {
                for (int dY = 0; dY < 1000; dY++) {
                    dP = dP.above();
                    if (isFilled(level, dP)) {
                        break;
                    }
                }
            }

            player.sendSystemMessage(Component.literal("spawning " + _entity + " at " + dP));

            for (var j = 0; j < _number; j++) {
                var entity = type.create(level);
                if (entity == null) {
                    player.sendSystemMessage(Component.literal("error: entity '" + _entity + "' cannot be summoned"));
                    return;
                }

                entity.moveTo(dP.getX() + 0.5f, dP.getY(), dP.getZ() + 0.5f);
                level.addFreshEntity(entity);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s : %.1fx @%.1f%%, %.1f each", _entity.toString(), _multiplier, _probability, _number);
    }
}
