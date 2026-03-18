import java.sql.*;

public class TestDB {

    public static void main(String[] args) {

        try {

            // Load Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect Database
            Connection con =
                    DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/placement_tracker",
                            "root",
                            "Root@2005");   // change password if needed

            System.out.println("✅ Database Connected Successfully");

            // Fetch Data
            PreparedStatement ps =
                    con.prepareStatement("select * from students");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                System.out.println(
                        rs.getString("roll") + " " +
                        rs.getString("name") + " " +
                        rs.getString("branch")
                );
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}