package com.swift.birdsofafeather;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.swift.birdsofafeather.model.db.AppDatabase;
import com.swift.birdsofafeather.model.db.Class;
import com.swift.birdsofafeather.model.db.Student;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class NearbyUnitTest {
    private AppDatabase db;
    private MessageListener realListener;
    private FakeMessageListener fakeListener;
    private String testMessage;
    Context context;

    @Before
    public void init() {
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        realListener = new MessageListener() {
            @Override
            public void onFound(@NonNull Message message) {
                String messageContent = new String(message.getContent());
                String[] decodedMessage = messageContent.split(",");

                UUID studentUUID = UUID.fromString(decodedMessage[0]);
                String name = decodedMessage[1];
                String pictureURL = decodedMessage[2];
                Bitmap image = Utils.urlToBitmap(context, pictureURL);

                Student classmate = new Student(studentUUID, name, image);
                db.studentDao().insert(classmate);

                for(int i = 3; i < decodedMessage.length; i+=6) {
                    UUID classId = UUID.fromString(decodedMessage[i]);

                    int year = Integer.parseInt(decodedMessage[i + 1]);
                    String quarter = decodedMessage[i + 2];
                    String subject = decodedMessage[i + 3];
                    String courseNumber = decodedMessage[i + 4];
                    String courseSize = decodedMessage[i + 5];

                    Class newClass = new Class(classId, studentUUID, year, quarter, subject, courseNumber, courseSize);
                    db.classesDao().insert(newClass);
                }
            }

            @Override
            public void onLost(@NonNull Message message) {
                //Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
            }
        };

        testMessage = getTestMessage(context);
        fakeListener = new FakeMessageListener(realListener, 3, testMessage);
        Message myStudentData = new Message(testMessage.getBytes(StandardCharsets.UTF_8));
        Nearby.getMessagesClient(context).subscribe(fakeListener);
        Nearby.getMessagesClient(context).publish(myStudentData);
    }

    @Test
    public void test_entries_database() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Exception");
        }

        List<Student> students = db.studentDao().getAllStudents();
        List<Class> classes = db.classesDao().getAllClasses();

        Student first = students.get(0);
        Class c1 = classes.get(0).getQuarter().equals("wi") ? classes.get(0) : classes.get(1);
        Class c2 = classes.get(0).getQuarter().equals("wi") ? classes.get(1) : classes.get(0);

        assertEquals(1, students.size());
        assertEquals("Travis", first.getName());
        assertEquals("wi", c1.getQuarter());
        assertEquals("130", c1.getCourseNumber());
        assertEquals(2, classes.size());
    }

    private String getTestMessage(Context context) {
        UUID randomUUID = UUID.randomUUID();
        String testName = "Travis";
        String pictureURL = "https://riverlegacy.org/wp-content/uploads/2021/07/blank-profile-photo.jpeg";
        Bitmap testImage = Utils.urlToBitmap(context, pictureURL);

        Student testStudent = new Student(randomUUID, testName, testImage);

        UUID id1 = UUID.randomUUID();
        String quarter = "fa";
        int year = 2007;
        String courseNumber = "110";
        String subject = "cse";
        String courseSize = "Tiny";
        Class class1 = new Class(id1, randomUUID, year, quarter, subject, courseNumber, courseSize);

        UUID id2 = UUID.randomUUID();
        String quarter2 = "wi";
        int year2 = 2007;
        String courseNumber2 = "130";
        String subject2 = "cse";
        String courseSize2 = "Tiny";
        Class class2 = new Class(id2, randomUUID, year2, quarter2, subject2, courseNumber2, courseSize2);

        return randomUUID.toString() + "," + testName + "," + pictureURL
                + "," + class1 + "," + class2;
    }

    @After
    public void closeDb() {
        db.close();
    }
}
