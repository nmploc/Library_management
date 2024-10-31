package library;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncoder {
    public static String hashedpassword(final String password) {
        try {
            final MessageDigest a = MessageDigest.getInstance("SHA-256");
            final byte[] hash = a.digest(password.getBytes("UTF-8"));
            final StringBuilder ans = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) ans.append('0');
                ans.append(hex);
            }
            return ans.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
