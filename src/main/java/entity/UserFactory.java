package entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserFactory {

    public User create(String name, String password) {
        return new User(
                name,
                password,
                Instant.now(),
                new ArrayList<>(),  // visits list
                new ArrayList<>()   // private notes list
        );
    }

    public User create(String name,
                       String password,
                       Instant createdAt,
                       List<Visit> visits,
                       List<Note> privateNotes) {

        return new User(name, password, createdAt, visits, privateNotes);
    }
}
