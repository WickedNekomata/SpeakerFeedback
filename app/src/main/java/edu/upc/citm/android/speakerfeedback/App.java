package edu.upc.citm.android.speakerfeedback;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    static class Room
    {
        String name;
        String id;

        public Room(String name, String id) {
            this.name = name;
            this.id = id;
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
        super.onTerminate();
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
}