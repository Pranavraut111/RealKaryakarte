import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
public class RunDb {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ganpati_mandal", "mandal_app", "mandal_pass");
        Statement s = c.createStatement();
        s.execute("ALTER TABLE contributions ADD COLUMN collected_by_text VARCHAR(150);");
        s.close();
        c.close();
        System.out.println("Success!");
    }
}
