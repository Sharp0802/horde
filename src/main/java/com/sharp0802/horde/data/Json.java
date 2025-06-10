package com.sharp0802.horde.data;

import java.io.IOException;

public class Json {
    public static class Exception extends java.lang.Exception {
        public Exception(String message) {
            super(message);
        }

        public Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static Exception missingField(String type, String field) {
        return new Exception("Missing required field '" + field + "' for type '" + type + "'");
    }

    public static Exception invalidRef(String type, String field, String ref, IOException e) {
        return new Exception("Invalid reference '" + ref + "' in member '" + field + "' for type '" + type + "'", e);
    }

    public static Exception unrecognizedValue(String type, String field, String value) {
        return new Exception("Unrecognized value '" + value + "' of member '" + field + "' for type '" + type + "'");
    }

    public static Exception couldOpen(String file, IOException e) {
        return new Exception("Could not open file '" + file + "'", e);
    }
}
