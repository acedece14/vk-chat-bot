/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * хранилка для настроек
 */
@Getter
@Setter
@Slf4j
public class AppSettings {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE_SETTINGS = new File("settings.json");
    private static AppSettings instance;

    private String accessToken = "set access token here";
    private int groupId = -1;

    private AppSettings() {}

    public static synchronized AppSettings getInstance() {
        if (instance == null)
            loadSettings();
        return instance;
    }

    private static void loadSettings() {
        if (!FILE_SETTINGS.exists()) {
            (instance = new AppSettings()).saveSettingsToFile();
            log.info(FILE_SETTINGS.getAbsolutePath() + " created, edit settings and restart app");
            System.exit(0);
        }
        try (var fr = new FileReader(FILE_SETTINGS)) {
            instance = gson.fromJson(fr, AppSettings.class);
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    public synchronized void saveSettingsToFile() {
        try {
            Files.writeString(FILE_SETTINGS.toPath(), gson.toJson(this));
        } catch (IOException e) {throw new RuntimeException(e);}
    }

}