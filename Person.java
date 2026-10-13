import java.io.Serializable;

/**
 * Common base entity for any person the system deals with.
 * Demonstrates INHERITANCE: specialized classes (Customer) are
 * derived from this common entity.
 */
public abstract class Person implements Serializable {
    protected String name;
    protected String phone;
    protected String email;

    public Person(String name, String phone, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits.");
        }
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    /** POLYMORPHISM: each subclass provides its own detail formatting. */
    public abstract String getDetails();
}
