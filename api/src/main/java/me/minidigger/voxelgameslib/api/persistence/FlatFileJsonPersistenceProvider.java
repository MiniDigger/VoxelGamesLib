package me.minidigger.voxelgameslib.api.persistence;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import me.minidigger.voxelgameslib.api.lang.Locale;
import me.minidigger.voxelgameslib.api.signs.SignLocation;
import me.minidigger.voxelgameslib.api.user.UserData;

/**
 * Simple persistence provider which uses gson to save the stuff as json to a flat file<br>
 * <b>NOT RECOMMENDED FOR PRODUCTION</b>
 */
public class FlatFileJsonPersistenceProvider implements PersistenceProvider {

    @Inject
    private Gson gson;

    @Inject
    @Named("DataFolder")
    private File folder;

    private File userDataFile;
    private Map<UUID, UserData> userDataMap;

    private File localeFile;
    private List<Locale> localeList;

    private File signFile;
    private List<SignLocation> signList;

    @SuppressWarnings("Duplicates")
    @Override
    public void start() {
        if (!folder.exists()) {
            folder.mkdir();
        }

        // users
        userDataFile = new File(folder, "userdata.json");
        userDataMap = new HashMap<>();

        if (!userDataFile.exists()) {
            saveUsers();
        }

        try {
            String json = Files.readAllLines(userDataFile.toPath()).stream().collect(Collectors.joining());
            //noinspection unchecked
            userDataMap = gson.fromJson(json, Map.class);
            if (userDataMap == null) {
                userDataMap = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // locale
        localeFile = new File(folder, "locales.json");
        localeList = new ArrayList<>();

        if (!localeFile.exists()) {
            saveLocales();
        }

        try {
            String json = Files.readAllLines(localeFile.toPath()).stream().collect(Collectors.joining());
            //noinspection unchecked
            localeList = gson.fromJson(json, List.class);
            if (localeList == null) {
                localeList = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // signs
        signFile = new File(folder, "signs.json");
        signList = new ArrayList<>();

        if (!signFile.exists()) {
            saveSigns();
        }

        try {
            String json = Files.readAllLines(signFile.toPath()).stream().collect(Collectors.joining());
            //noinspection unchecked
            signList = gson.fromJson(json, List.class);
            if (signList == null) {
                signList = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        userDataMap.clear();
        localeList.clear();
        signList.clear();
    }

    @Override
    public void saveUserData(UserData user) {
        userDataMap.put(user.getUuid(), user);
        saveUsers();
    }

    private void saveUsers() {
        try (FileWriter fw = new FileWriter(userDataFile)) {
            fw.write(gson.toJson(userDataMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<UserData> loadUserData(UUID id) {
        return Optional.ofNullable(userDataMap.get(id));
    }

    @Override
    public void saveLocale(Locale locale) {
        localeList.add(locale);
        saveLocales();
    }

    private void saveLocales() {
        try (FileWriter fw = new FileWriter(localeFile)) {
            fw.write(gson.toJson(localeList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Locale> loadLocales() {
        return localeList;
    }

    @Override
    public List<SignLocation> loadSigns() {
        return signList;
    }

    @Override
    public void saveSigns(List<SignLocation> signs) {
        signList.addAll(signs);
        saveSigns();
    }

    @Override
    public void deleteSigns(List<SignLocation> signs) {
        signList.removeAll(signs);
        saveSigns();
    }

    private void saveSigns() {
        try (FileWriter fw = new FileWriter(signFile)) {
            fw.write(gson.toJson(signList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
