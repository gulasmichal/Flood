package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "game_user")
@NamedQueries({
        @NamedQuery(name = "GameUser.findByUsername",
                query = "SELECT u FROM GameUser u WHERE u.username = :username"),
        @NamedQuery(name = "GameUser.findByCredentials",
                query = "SELECT u FROM GameUser u WHERE u.username = :username AND u.password = :password")
})
public class GameUser implements Serializable {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    public GameUser() {}

    public GameUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
