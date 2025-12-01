package library0;

import java.util.Objects;

/**
 * Represents a user of the library (admin or customer).
 */
public class User {

    private final String id;      // مثلاً "U1", "U2" ...
    private final String name;
    private final String email;
    private final Role role;
    private final String password; // للتبسيط، نخزنها plain text في الفايل (مش حقيقي بس للمشروع)

    public User(String id, String name, String email, Role role, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}