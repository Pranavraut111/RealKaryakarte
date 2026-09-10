import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mandal.util.DbConnectionManager;
import com.mandal.util.PasswordUtil;
import com.mandal.util.PasswordStore;
import com.mandal.model.User;
import com.mandal.dao.UserDao;

public class Migrate {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DbConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255)");
            System.out.println("Added password_hash column!");
            
            // Set a default password hash for all users whose hash is currently null, so they can login.
            // Password: "password123"
            String defaultHash = PasswordUtil.hashPassword("password123");
            
            int rows = stmt.executeUpdate("UPDATE users SET password_hash = '" + defaultHash + "' WHERE password_hash IS NULL");
            System.out.println("Updated " + rows + " users with default password 'password123'");
        }
    }
}
