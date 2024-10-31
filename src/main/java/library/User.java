package library;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User
{
    private static int ID;
    private static String username;
    private static String password;
    private static String userFullName;
    private static String role;
    private static String gmail;
    private static String phoneNumber;
    //todo: how about date of birth ?
    private static String avatar;

    public static void loadUserData(ResultSet resultSet)
    {
        try {
            ID = resultSet.getInt("userID");
            username = resultSet.getString("username");
            password = resultSet.getString("hashedPassword");
            userFullName = resultSet.getString("userFullName");
            role = resultSet.getString("role");
            gmail = resultSet.getString("gmail");
            phoneNumber = resultSet.getString("phoneNumber");
            avatar = resultSet.getString("avatar");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getID() {
        return ID;
    }

    public static void setID(int newID) {
        ID = newID;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String newUsername) {
        username = newUsername;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String newPassword) {
        password = newPassword;
    }

    public static String getUserFullName() {
        return userFullName;
    }

    public static void setUserFullName(String newFullName) {
        userFullName = newFullName;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String newRole) {
        role = newRole;
    }

    public static String getGmail() {
        return gmail;
    }

    public static void setGmail(String newGmail) {
        gmail = newGmail;
    }

    public static String getPhoneNumber() {
        return phoneNumber;
    }

    public static void setPhoneNumber(String newPhoneNumber) {
        phoneNumber = newPhoneNumber;
    }

    public static String getAvatar() {
        return avatar;
    }

    public static void setAvatar(String newAvatar) {
        avatar = newAvatar;
    }
}
