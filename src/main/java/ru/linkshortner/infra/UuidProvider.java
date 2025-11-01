package ru.linkshortner.infra;

import java.io.*;
import java.util.UUID;

public class UuidProvider {

    private static final String FILE = "user.uuid";

    public static UUID getOrCreateUserId() {
        try {
            File f = new File(FILE);
            if (f.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                    return UUID.fromString(r.readLine());
                }
            }
            UUID id = UUID.randomUUID();
            try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
                w.write(id.toString());
            }
            return id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
