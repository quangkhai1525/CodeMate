package org.example.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="username", nullable = false)
    private String username;

    @Column(name ="fullname", nullable = false)
    private String fullname;

    @Column(name ="password", nullable = false)
    private String password;

    @Column(name ="email", nullable = false)
    private String email;

    @Column(name ="otp", nullable = false)
    private int otp;

    public User(){}

    public User(String fullname,String username, String password, String email, int otp) {
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.otp = otp;
    }
}
