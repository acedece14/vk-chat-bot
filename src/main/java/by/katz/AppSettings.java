package by.katz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        if (instance == null) {
            if (!FILE_SETTINGS.exists()) {
                instance = new AppSettings();
                instance.saveSettingsToFile();
                log.info(FILE_SETTINGS + " created, edit and restart app");
                System.exit(1);
            }
            try {
                var json = Files.readString(FILE_SETTINGS.toPath());
                instance = gson.fromJson(json, AppSettings.class);
            } catch (IOException e) {throw new RuntimeException(e);}
        }
        return instance;
    }

    public void saveSettingsToFile() {
        try {
            Files.writeString(FILE_SETTINGS.toPath(), gson.toJson(this));
        } catch (IOException e) {throw new RuntimeException(e);}
    }

}