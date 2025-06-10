package com.sharp0802.horde.data;

import com.google.gson.JsonObject;
import com.sharp0802.horde.Time;

public abstract class When {

    private static class Tick {
        public static long from(String s) {
            if (s.isEmpty()) {
                return 0;
            }

            var factor = switch (s.charAt(s.length() - 1)) {
                case 'd' -> 24000;
                case 'h' -> 1000;
                default -> 0;
            };
            var value = Long.parseLong(factor == 0 ? s : s.substring(0, s.length() - 1));

            return factor * value;
        }

        public static String toString(long tick) {
            var day = tick / 24000;
            var hour = (tick % 24000) / 1000;
            var rTick = tick % 1000;

            var builder = new StringBuilder();
            if (day > 0) {
                builder.append(day).append("d ");
            }
            if (hour > 0) {
                builder.append(hour).append("h ");
            }
            if (rTick > 0) {
                builder.append(rTick).append("tick");
                if (rTick > 1) {
                    builder.append('s');
                }
                builder.append(' ');
            }

            builder.append('(').append(tick).append(" tick");
            if (tick > 0) {
                builder.append('s');
            }
            builder.append(')');

            return builder.toString();
        }
    }

    public static class Cyclic extends When {
        private final long _tick;
        private final long _offset;

        private Cyclic(JsonObject e) throws Json.Exception {
            var cycleMember = e.get("cycle");
            if (cycleMember == null) {
                throw Json.missingField("cyclic", "cycle");
            }

            var cycle = cycleMember.getAsString();
            _tick = Tick.from(cycle);

            var offsetMember = e.get("offset");
            if (offsetMember == null) {
                throw Json.missingField("cyclic", "offset");
            }

            var offset = offsetMember.getAsString();
            _offset = Tick.from(offset);
        }

        @Override
        public boolean satisfy() {
            return Time.getTick() % _tick == _offset;
        }

        @Override
        public String toString() {
            return "Cyclic: " + Tick.toString(_tick) + " after " + Tick.toString(_offset);
        }
    }

    public static When from(JsonObject e) throws Json.Exception {
        var typeMember = e.get("type");
        if (typeMember == null) {
            throw Json.missingField("when", "type");
        }

        var type = typeMember.getAsString();
        return switch (type) {
            case "cyclic" -> new Cyclic(e);
            default -> throw Json.unrecognizedValue("when", "type", type);
        };
    }

    public abstract boolean satisfy();
}
