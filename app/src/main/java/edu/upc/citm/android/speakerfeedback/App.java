package edu.upc.citm.android.speakerfeedback;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App extends Application {

    static class Room
    {
        private String name;
        private String id;

        public Room(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }

    public static final String CHANNEL_ID = "SpeakerFeedback";

    public static final String ROOMKEY = "roomId";
    public static final String USERKEY = "userId";

    private String roomId;
    private String userId;

    public List<Room> recentRooms;

    @Override
    public void onCreate() {
        super.onCreate();

        recentRooms = new ArrayList<>();
        readRoomsList();

        readSharedPreferences();
        createNotificationChannels();
    }

    @Override
    public void onTerminate() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERKEY, userId);
        editor.putString(ROOMKEY, roomId);
        editor.commit();

        saveRoomsList();

        super.onTerminate();
    }

    private void saveRoomsList() {
        try {
            FileOutputStream outputStream = openFileOutput("recentRooms.txt", MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            for (int i = 0; i < recentRooms.size(); i++) {
                Room room = recentRooms.get(i);
                writer.write(String.format("%s;%s\n", room.getName(), room.getId()));
            }
            writer.close();
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "saveRoomsList: FileNotFoundException");
        }
        catch (IOException e) {
            Log.e("SpeakerFeedback", "saveRoomsList: IOException");
        }
    }

    private void readRoomsList() {
        try {
            FileInputStream inputStream = openFileInput("recentRooms.txt");
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                recentRooms.add(new Room(parts[0], parts[1]));
            }
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "readRoomsList: FileNotFoundException");
        }
    }

    public void readSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString(USERKEY, null);
        roomId = prefs.getString(ROOMKEY, null);
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                                        "SpeakerFeedback channel",
                                        NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public boolean addRoom(Room room)
    {
        for (Room currentRoom : recentRooms) {
            if (currentRoom.id.equals(room.id))
                return false;
        }

        if (recentRooms.size() == 5)
            recentRooms.remove(4);

        recentRooms.add(room);
        return true;
    }

    public boolean deleteRoom(Room room)
    {
        if (!recentRooms.contains(room))
            return false;

        recentRooms.remove(room);
        return true;
    }
}