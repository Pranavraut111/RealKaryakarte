package com.mandal.service;

import com.mandal.dao.UserDao;
import com.mandal.model.Role;
import com.mandal.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * User management service — role changes, profile updates, member listing.
 */
public class UserService {

    private final UserDao userDao = new UserDao();

    public List<User> getAllUsers(Long mandalId) throws SQLException {
        return userDao.findAll(mandalId);
    }

    public User getUserById(Long id) throws SQLException {
        return userDao.findById(id);
    }

    public User getUserByPhone(String phone) throws SQLException {
        return userDao.findByPhone(phone);
    }

    /**
     * Create a new user (admin action).
     */
    public User createUser(User user) throws SQLException {
        // Ensure phone is unique
        User existing = userDao.findByPhone(user.getPhone());
        if (existing != null) {
            throw new IllegalArgumentException("A user with phone " + user.getPhone() + " already exists");
        }
        return userDao.insert(user);
    }

    /**
     * Change a user's role (admin only).
     */
    public void changeRole(Long userId, String role, Long requesterId, String requesterRole, Long mandalId)
            throws SQLException {
        if (!"ADMIN".equals(requesterRole)) {
            throw new SecurityException("Only admins can change roles");
        }
        Role newRole = Role.fromString(role);
        userDao.updateRole(userId, newRole, mandalId);
    }

    /**
     * Update a user's profile.
     * Users can update their own profile; admins can update anyone's.
     */
    public void updateProfile(Long userId, String name, String email,
                               String languagePref, String photoUrl,
                               Long requesterId, String requesterRole) throws SQLException {
        if (!userId.equals(requesterId) && !"ADMIN".equals(requesterRole)) {
            throw new SecurityException("You can only update your own profile");
        }
        userDao.updateProfile(userId, name, email, languagePref, photoUrl);
    }
}
