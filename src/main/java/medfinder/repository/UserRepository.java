package medfinder.repository;

import java.util.List;

import medfinder.model.User;

/**
 * Storage contract for user accounts (pharmacists and the administrator).
 *
 * <p>Every operation throws the unchecked {@code DataStorageException}
 * when the underlying storage cannot be read or written.
 */
public interface UserRepository {

    /**
     * Finds a user by id.
     *
     * @param id the user id
     * @return the user, or null if there is none with that id
     */
    User findById(int id);

    /**
     * Finds a user by e-mail address, ignoring upper/lower case.
     *
     * @param email the e-mail address to look for
     * @return the user, or null if there is none with that e-mail
     */
    User findByEmail(String email);

    /**
     * Lists every user.
     *
     * @return a copy of the list of all users; empty if there are none
     */
    List<User> findAll();

    /**
     * Inserts a new user, or updates the user that has the same id.
     *
     * @param user the user to store
     */
    void save(User user);

    /**
     * Gives the next free user id.
     *
     * @return the largest existing id plus 1 (1 when there are no users)
     */
    int nextId();
}
