package data_access;

import entity.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

public class JsonUserDataAccessObject implements UserDataAccessInterface {

    private final File jsonFile;
    private final Map<String, User> accounts = new HashMap<>();
    private final UserFactory userFactory;

    // NEW: optional Landmark DAO
    private final LandmarkDataAccessInterface landmarkDAO;

    private String currentUsername;

    /**
     * Original constructor — kept for compatibility.
     * Uses no Landmark DAO (falls back to stub landmarks).
     */
    public JsonUserDataAccessObject(String csvPath, UserFactory userFactory) {
        this(csvPath, userFactory, null);
    }

    /**
     * NEW constructor that also receives a Landmark DAO.
     */
    public JsonUserDataAccessObject(String csvPath,
                                    UserFactory userFactory,
                                    LandmarkDataAccessInterface landmarkDAO) {
        this.jsonFile = new File(csvPath);
        this.userFactory = userFactory;
        this.landmarkDAO = landmarkDAO;

        if (!jsonFile.exists() || jsonFile.length() == 0) {
            saveAll();
        } else {
            loadAll();
        }
    }

    /** ============================ LOAD ============================ **/
    private void loadAll() {
        try {
            String content = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return; // nothing to load
            }

            JSONArray arr = new JSONArray(content);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String username = obj.getString("username");
                String password = obj.getString("password");

                Instant createdAt = obj.has("createdAt")
                        ? Instant.parse(obj.getString("createdAt"))
                        : Instant.now();

                // =============== LOAD VISITS ===============
                List<Visit> visits = new ArrayList<>();
                if (obj.has("visits")) {
                    JSONArray vArr = obj.getJSONArray("visits");

                    for (int j = 0; j < vArr.length(); j++) {
                        JSONObject vObj = vArr.getJSONObject(j);

                        String visitId = vObj.getString("visitId");
                        String landmarkName = vObj.getString("landmarkName");
                        Instant visitedAt = Instant.parse(vObj.getString("visitedAt"));

                        Landmark landmark;
                        if (landmarkDAO != null) {
                            try {
                                landmark = landmarkDAO.findByName(landmarkName);
                            } catch (RuntimeException ex) {
                                // Fallback to stub if not found
                                landmark = new Landmark(
                                        landmarkName,
                                        landmarkName,
                                        null,
                                        new LandmarkInfo("", "", "", ""),
                                        0
                                );
                            }
                        } else {
                            // No DAO injected -> stub
                            landmark = new Landmark(
                                    landmarkName,
                                    landmarkName,
                                    null,
                                    new LandmarkInfo("", "", "", ""),
                                    0
                            );
                        }

                        Visit visit = new Visit(visitId, landmark, visitedAt);
                        visits.add(visit);
                    }
                }

                // =============== LOAD PRIVATE NOTES ===============
                List<Note> privateNotes = new ArrayList<>();
                if (obj.has("privateNotes")) {
                    JSONArray nArr = obj.getJSONArray("privateNotes");

                    for (int j = 0; j < nArr.length(); j++) {
                        JSONObject nObj = nArr.getJSONObject(j);

                        String noteId = nObj.getString("noteId");
                        String content_note = nObj.getString("content");
                        String landmarkName = nObj.getString("landmarkName");

                        Instant createdAtNote = Instant.parse(nObj.getString("createdAt"));
                        Instant updatedAtNote = Instant.parse(nObj.getString("updatedAt"));

                        Landmark landmark;
                        if (landmarkDAO != null) {
                            try {
                                landmark = landmarkDAO.findByName(landmarkName);
                            } catch (RuntimeException ex) {
                                landmark = new Landmark(
                                        landmarkName,
                                        landmarkName,
                                        null,
                                        new LandmarkInfo("", "", "", ""),
                                        0
                                );
                            }
                        } else {
                            landmark = new Landmark(
                                    landmarkName,
                                    landmarkName,
                                    null,
                                    new LandmarkInfo("", "", "", ""),
                                    0
                            );
                        }

                        Note note = new Note(
                                noteId,
                                landmark,
                                content_note,
                                createdAtNote,
                                updatedAtNote
                        );
                        privateNotes.add(note);
                    }
                }

                // CREATE FULL USER OBJECT
                User user = userFactory.create(
                        username,
                        password,
                        createdAt,
                        visits,
                        privateNotes
                );

                accounts.put(username, user);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read users JSON file", e);
        } catch (Exception e) {
            throw new RuntimeException("Malformed JSON in users file", e);
        }
    }

    /** ============================ SAVE ============================ **/
    private void saveAll() {
        try {
            JSONArray arr = new JSONArray();

            for (User user : accounts.values()) {
                JSONObject obj = new JSONObject();
                obj.put("username", user.getUsername());
                obj.put("password", user.getPassword());
                obj.put("createdAt", user.getCreatedAt().toString());

                // --- VISITS ---
                JSONArray visitsArr = new JSONArray();
                for (Visit v : user.getVisits()) {
                    JSONObject vObj = new JSONObject();
                    vObj.put("visitId", v.getVisitId());
                    vObj.put("landmarkName", v.getLandmark().getLandmarkName());
                    vObj.put("visitedAt", v.getVisitedAt().toString());
                    visitsArr.put(vObj);
                }
                obj.put("visits", visitsArr);

                // --- PRIVATE NOTES ---
                JSONArray notesArr = new JSONArray();
                for (Note n : user.getPrivateNotes()) {
                    JSONObject nObj = new JSONObject();
                    nObj.put("noteId", n.getNoteId());
                    nObj.put("landmarkName", n.getLandmark().getLandmarkName());
                    nObj.put("content", n.getContent());
                    nObj.put("createdAt", n.getCreatedAt().toString());
                    nObj.put("updatedAt", n.getUpdatedAt().toString());
                    notesArr.put(nObj);
                }
                obj.put("privateNotes", notesArr);

                arr.put(obj);
            }

            Files.writeString(jsonFile.toPath(), arr.toString(4), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users JSON file", e);
        }
    }

    // === UserDataAccessInterface methods (names unchanged) ===

    @Override
    public void save(User user) {
        accounts.put(user.getUsername(), user);
        saveAll();
    }

    @Override
    public User get(String username) {
        return accounts.get(username);
    }

    @Override
    public void setCurrentUsername(String name) {
        currentUsername = name;
    }

    @Override
    public String getCurrentUsername() {
        return currentUsername;
    }

    @Override
    public boolean existsByName(String identifier) {
        return accounts.containsKey(identifier);
    }
}