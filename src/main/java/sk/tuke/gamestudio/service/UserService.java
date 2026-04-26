package sk.tuke.gamestudio.service;

public interface UserService {
    void register(String username, String password) throws UserException;
    boolean login(String username, String password) throws UserException;
}
