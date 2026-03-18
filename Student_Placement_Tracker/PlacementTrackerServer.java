import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class PlacementTrackerServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ===== CSS STATIC HANDLER =====
        server.createContext("/style.css", ex -> {

            File file = new File("static/style.css");

            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());

            ex.getResponseHeaders().add("Content-Type", "text/css");
            ex.sendResponseHeaders(200, bytes.length);

            OutputStream os = ex.getResponseBody();
            os.write(bytes);
            os.close();
        });

        // DASHBOARD
        server.createContext("/", ex -> {

            String html =
                    pageStart("Dashboard") +

                            card("Add Student",
                                    "<form method='post' action='/addStudent'>" +
                                            "<input name='name' placeholder='Student Name' required>" +
                                            "<input name='roll' placeholder='Roll Number' required>" +
                                            "<input name='branch' placeholder='Branch' required>" +
                                            "<button>Add Student</button></form>") +

                            card("Add Company",
                                    "<form method='post' action='/addCompany'>" +
                                            "<input name='name' placeholder='Company Name' required>" +
                                            "<input name='role' placeholder='Role' required>" +
                                            "<button>Add Company</button></form>") +

                            pageEnd();

            send(ex, html);
        });

        // ADD STUDENT
        server.createContext("/addStudent", ex -> {

            if ("POST".equals(ex.getRequestMethod())) {

                String[] d = read(ex);

                try {
                    Connection con = DBConnection.getConnection();

                    PreparedStatement ps = con.prepareStatement(
                            "insert into students(roll,name,branch) values(?,?,?)"
                    );

                    ps.setString(1, d[1]);
                    ps.setString(2, d[0]);
                    ps.setString(3, d[2]);

                    ps.executeUpdate();
                    con.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            redirect(ex, "/viewStudents");
        });

        // ADD COMPANY
        server.createContext("/addCompany", ex -> {

            if ("POST".equals(ex.getRequestMethod())) {

                String[] d = read(ex);

                try {
                    Connection con = DBConnection.getConnection();

                    PreparedStatement ps = con.prepareStatement(
                            "insert into companies(name,role) values(?,?)"
                    );

                    ps.setString(1, d[0]);
                    ps.setString(2, d[1]);

                    ps.executeUpdate();
                    con.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            redirect(ex, "/viewCompanies");
        });

        // VIEW STUDENTS
        server.createContext("/viewStudents", ex -> {

            StringBuilder rows = new StringBuilder();

            try {
                Connection con = DBConnection.getConnection();
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from students");

                while (rs.next()) {

                    rows.append("<tr>")
                            .append("<td>").append(rs.getString("name")).append("</td>")
                            .append("<td>").append(rs.getString("roll")).append("</td>")
                            .append("<td>").append(rs.getString("branch")).append("</td>")
                            .append("<td>")
                            .append(rs.getBoolean("placed")
                                    ? "<span class='badge-green'>Placed</span>"
                                    : "<span class='badge-red'>Unplaced</span>")
                            .append("</td>")
                            .append("</tr>");
                }

                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String html =
                    pageStart("Students") +
                            table("<tr><th>Name</th><th>Roll</th><th>Branch</th><th>Status</th></tr>", rows.toString()) +
                            pageEnd();

            send(ex, html);
        });

        // VIEW COMPANIES
        server.createContext("/viewCompanies", ex -> {

            StringBuilder rows = new StringBuilder();

            try {
                Connection con = DBConnection.getConnection();
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from companies");

                while (rs.next()) {

                    rows.append("<tr>")
                            .append("<td>").append(rs.getString("name")).append("</td>")
                            .append("<td>").append(rs.getString("role")).append("</td>")
                            .append("</tr>");
                }

                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String html =
                    pageStart("Companies") +
                            table("<tr><th>Company</th><th>Role</th></tr>", rows.toString()) +
                            pageEnd();

            send(ex, html);
        });

        // PLACEMENT PAGE
        server.createContext("/placement", ex -> {

            StringBuilder sOpt = new StringBuilder();
            StringBuilder cOpt = new StringBuilder();

            try {
                Connection con = DBConnection.getConnection();
                Statement st = con.createStatement();

                ResultSet rs1 = st.executeQuery("select roll,name from students");

                while (rs1.next())
                    sOpt.append("<option value='")
                            .append(rs1.getString("roll"))
                            .append("'>")
                            .append(rs1.getString("name"))
                            .append("</option>");

                ResultSet rs2 = st.executeQuery("select name from companies");

                while (rs2.next())
                    cOpt.append("<option value='")
                            .append(rs2.getString("name"))
                            .append("'>")
                            .append(rs2.getString("name"))
                            .append("</option>");

                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String html =
                    pageStart("Placement") +
                            card("Place Student",
                                    "<form method='post' action='/doPlacement'>" +
                                            "<select name='student'>" + sOpt + "</select>" +
                                            "<select name='company'>" + cOpt + "</select>" +
                                            "<button>Place</button></form>") +
                            pageEnd();

            send(ex, html);
        });

        // DO PLACEMENT
        server.createContext("/doPlacement", ex -> {

            if ("POST".equals(ex.getRequestMethod())) {

                String[] d = read(ex);

                try {
                    Connection con = DBConnection.getConnection();

                    PreparedStatement ps = con.prepareStatement(
                            "update students set placed=true, company=? where roll=?"
                    );

                    ps.setString(1, d[1]);
                    ps.setString(2, d[0]);

                    ps.executeUpdate();
                    con.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            redirect(ex, "/placementStatus");
        });

        // DASHBOARD STATUS
        server.createContext("/placementStatus", ex -> {

            int placed = 0;
            int total = 0;

            StringBuilder rows = new StringBuilder();

            try {
                Connection con = DBConnection.getConnection();
                Statement st = con.createStatement();

                ResultSet r1 = st.executeQuery("select count(*) from students");
                if (r1.next()) total = r1.getInt(1);

                ResultSet r2 = st.executeQuery("select count(*) from students where placed=true");
                if (r2.next()) placed = r2.getInt(1);

                ResultSet r3 = st.executeQuery(
                        "select company,count(*) as c from students where placed=true group by company"
                );

                while (r3.next())
                    rows.append("<tr><td>")
                            .append(r3.getString("company"))
                            .append("</td><td>")
                            .append(r3.getInt("c"))
                            .append("</td></tr>");

                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String html =
                    pageStart("Placement Dashboard") +
                            stats(placed, total - placed) +
                            table("<tr><th>Company</th><th>Placed Students</th></tr>", rows.toString()) +
                            pageEnd();

            send(ex, html);
        });

        server.start();
        System.out.println("Server Running http://localhost:8080");
    }

    // ===== UI METHODS =====

    static String pageStart(String title) {
        return "<html><head><link rel='stylesheet' href='/style.css'></head><body>" +
                "<div class='sidebar'>" +
                "<h2>Placement Tracker</h2>" +
                "<a href='/'>Dashboard</a>" +
                "<a href='/viewStudents'>Students</a>" +
                "<a href='/viewCompanies'>Companies</a>" +
                "<a href='/placement'>Placement</a>" +
                "<a href='/placementStatus'>Status</a>" +
                "</div>" +
                "<div class='main'><div class='header'>" + title + "</div>";
    }

    static String pageEnd() { return "</div></body></html>"; }

    static String card(String t, String b) {
        return "<div class='card'><h3>" + t + "</h3>" + b + "</div>";
    }

    static String table(String h, String r) {
        return "<table>" + h + r + "</table>";
    }

    static String stats(int p, int u) {
        return "<div class='card'><h3>Placed : " + p + "</h3><h3>Unplaced : " + u + "</h3></div>";
    }

    static void send(HttpExchange ex, String html) throws IOException {
        byte[] b = html.getBytes();
        ex.sendResponseHeaders(200, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }

    static void redirect(HttpExchange ex, String url) throws IOException {
        ex.getResponseHeaders().add("Location", url);
        ex.sendResponseHeaders(302, -1);
    }

    static String[] read(HttpExchange ex) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8));
        String data = br.readLine();
        String[] p = data.split("&");
        String[] v = new String[p.length];
        for (int i = 0; i < p.length; i++)
            v[i] = URLDecoder.decode(p[i].split("=")[1], "UTF-8");
        return v;
    }
}