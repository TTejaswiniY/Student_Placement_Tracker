package com.example.placement.controller;

import com.example.placement.config.DBConnection;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

@Controller
@RequestMapping
public class PlacementController {

    // ====== DASHBOARD ("/") ======
    @GetMapping("/")
    public void dashboard(HttpServletResponse response) throws IOException {

        long totalStudents = 0;
        long placedStudents = 0;
        long totalCompanies = 0;

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs1 = st.executeQuery("select count(*) from student");
            if (rs1.next()) totalStudents = rs1.getLong(1);

            ResultSet rs2 = st.executeQuery("select count(*) from student where placed = true");
            if (rs2.next()) placedStudents = rs2.getLong(1);

            ResultSet rs3 = st.executeQuery("select count(*) from company");
            if (rs3.next()) totalCompanies = rs3.getLong(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html =
                pageStart("Dashboard") +
                        "<div class='grid-2'>" +
                        statsCard("Total Students", String.valueOf(totalStudents), "primary") +
                        statsCard("Placed Students", String.valueOf(placedStudents), "success") +
                        statsCard("Unplaced Students", String.valueOf(totalStudents - placedStudents), "warning") +
                        statsCard("Partner Companies", String.valueOf(totalCompanies), "info") +
                        "</div>" +

                        "<div class='grid-2'>" +
                        card("Add Student",
                                "<form class='form-grid' method='post' action='/addStudent'>" +
                                        "<div class='form-field'><label>Name</label><input name='name' placeholder='Student Name' required></div>" +
                                        "<div class='form-field'><label>Branch</label><input name='branch' placeholder='Branch (e.g. CSE, ECE)' required></div>" +
                                        "<button>Add Student</button></form>") +

                        card("Add Company",
                                "<form class='form-grid' method='post' action='/addCompany'>" +
                                        "<div class='form-field'><label>Company</label><input name='name' placeholder='Company Name' required></div>" +
                                        "<div class='form-field'><label>Role</label><input name='role' placeholder='Role (e.g. Developer)' required></div>" +
                                        "<div class='form-field'><label>Package (LPA)</label><input type='number' step='0.1' min='0' name='package' placeholder='e.g. 4.5' required></div>" +
                                        "<button>Add Company</button></form>") +
                        "</div>" +
                        pageEnd();

        send(response, html);
    }

    // ====== ADD STUDENT ======
    @PostMapping("/addStudent")
    public void addStudent(@RequestParam String name,
                           @RequestParam String branch,
                           HttpServletResponse response) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "insert into student(name,branch,placed,company_name) values(?,?,false,null)"
            );
            ps.setString(1, name);
            ps.setString(2, branch);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        redirect(response, "/viewStudents");
    }

    // ====== ADD COMPANY ======
    @PostMapping("/addCompany")
    public void addCompany(@RequestParam String name,
                           @RequestParam String role,
                           @RequestParam("package") double pkg,
                           HttpServletResponse response) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "insert into company(name,role,package) values(?,?,?)"
            );
            ps.setString(1, name);
            ps.setString(2, role);
            ps.setDouble(3, pkg);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        redirect(response, "/viewCompanies");
    }

    // ====== VIEW STUDENTS ======
    @GetMapping("/viewStudents")
    public void viewStudents(HttpServletResponse response) throws IOException {
        StringBuilder rows = new StringBuilder();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("select * from student order by name")) {

            while (rs.next()) {
                rows.append("<tr>")
                        .append("<td>").append(rs.getInt("id")).append("</td>")
                        .append("<td>").append(rs.getString("name")).append("</td>")
                        .append("<td>").append(rs.getString("branch")).append("</td>")
                        .append("<td>").append(rs.getString("company_name") == null ? "-" : rs.getString("company_name")).append("</td>")
                        .append("<td>")
                        .append(rs.getBoolean("placed")
                                ? "<span class='badge-green'>Placed</span>"
                                : "<span class='badge-red'>Unplaced</span>")
                        .append("</td>")
                        .append("</tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html =
                pageStart("Students") +
                        table("<tr><th>ID</th><th>Name</th><th>Branch</th><th>Company</th><th>Status</th></tr>", rows.toString()) +
                        pageEnd();

        send(response, html);
    }

    // ====== VIEW COMPANIES ======
    @GetMapping("/viewCompanies")
    public void viewCompanies(HttpServletResponse response) throws IOException {
        StringBuilder rows = new StringBuilder();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "select c.id, c.name, c.role, c.package, " +
                             "coalesce(count(s.id),0) as hired " +
                             "from company c " +
                             "left join student s on s.company_name = c.name and s.placed = true " +
                             "group by c.id, c.name, c.role, c.package " +
                             "order by c.name")) {

            while (rs.next()) {
                rows.append("<tr>")
                        .append("<td>").append(rs.getInt("id")).append("</td>")
                        .append("<td>").append(rs.getString("name")).append("</td>")
                        .append("<td>").append(rs.getString("role")).append("</td>")
                        .append("<td>").append(String.format("%.1f LPA", rs.getDouble("package"))).append("</td>")
                        .append("<td>").append(rs.getInt("hired")).append("</td>")
                        .append("</tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html =
                pageStart("Companies") +
                        table("<tr><th>ID</th><th>Company</th><th>Role</th><th>Package</th><th>Hired</th></tr>", rows.toString()) +
                        pageEnd();

        send(response, html);
    }

    // ====== PLACEMENT PAGE ======
    @GetMapping("/placement")
    public void placement(HttpServletResponse response) throws IOException {
        StringBuilder sOpt = new StringBuilder();
        StringBuilder cOpt = new StringBuilder();

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs1 = st.executeQuery("select id,name,branch from student order by name");
            while (rs1.next()) {
                sOpt.append("<option value='")
                        .append(rs1.getInt("id"))
                        .append("'>")
                        .append(rs1.getString("name"))
                        .append(" (")
                        .append(rs1.getString("branch"))
                        .append(")")
                        .append("</option>");
            }

            ResultSet rs2 = st.executeQuery("select name,role,package from company order by name");
            while (rs2.next()) {
                cOpt.append("<option value='")
                        .append(rs2.getString("name"))
                        .append("'>")
                        .append(rs2.getString("name"))
                        .append(" - ")
                        .append(rs2.getString("role"))
                        .append(" (")
                        .append(String.format("%.1f LPA", rs2.getDouble("package")))
                        .append(")")
                        .append("</option>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html =
                pageStart("Placement") +
                        card("Place Student",
                                "<form class='form-grid' method='post' action='/doPlacement'>" +
                                        "<div class='form-field'><label>Student</label><select name='student'>" + sOpt + "</select></div>" +
                                        "<div class='form-field'><label>Company</label><select name='company'>" + cOpt + "</select></div>" +
                                        "<button>Confirm Placement</button></form>") +
                        pageEnd();

        send(response, html);
    }

    // ====== DO PLACEMENT ======
    @PostMapping("/doPlacement")
    public void doPlacement(@RequestParam("student") int studentId,
                            @RequestParam("company") String company,
                            HttpServletResponse response) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "update student set placed=true, company_name=? where id=?"
            );
            ps.setString(1, company);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        redirect(response, "/placementStatus");
    }

    // ====== PLACEMENT STATUS DASHBOARD ======
    @GetMapping("/placementStatus")
    public void placementStatus(@RequestParam(value = "company", required = false) String companyFilter,
                                HttpServletResponse response) throws IOException {
        int placed = 0;
        int total = 0;

        StringBuilder companyRows = new StringBuilder();
        StringBuilder companyCards = new StringBuilder();
        StringBuilder studentRows = new StringBuilder();

        try (Connection con = DBConnection.getConnection()) {

            // overall counts
            try (Statement st = con.createStatement()) {
                ResultSet r1 = st.executeQuery("select count(*) from student");
                if (r1.next()) total = r1.getInt(1);

                ResultSet r2 = st.executeQuery("select count(*) from student where placed=true");
                if (r2.next()) placed = r2.getInt(1);
            }

            // per-company summary (table + cards)
            String companySql =
                    "select c.name, c.role, c.package, " +
                            "coalesce(count(s.id),0) as hired " +
                            "from company c " +
                            "left join student s on s.company_name = c.name and s.placed = true " +
                            "group by c.name, c.role, c.package " +
                            "order by c.name";

            try (Statement st2 = con.createStatement();
                 ResultSet rc = st2.executeQuery(companySql)) {

                while (rc.next()) {
                    String cname = rc.getString("name");
                    String role = rc.getString("role");
                    double pkg = rc.getDouble("package");
                    int hired = rc.getInt("hired");

                    companyRows.append("<tr><td>")
                            .append(cname)
                            .append("</td><td>")
                            .append(role)
                            .append("</td><td>")
                            .append(String.format("%.1f LPA", pkg))
                            .append("</td><td>")
                            .append(hired)
                            .append("</td></tr>");

                    String logoUrl = companyLogo(cname);
                    String encodedName = URLEncoder.encode(cname, StandardCharsets.UTF_8);

                    boolean isSelected = companyFilter != null && !companyFilter.isBlank() && companyFilter.equalsIgnoreCase(cname);
                    companyCards.append("<button class='company-card")
                            .append(isSelected ? " selected" : "")
                            .append("' onclick=\"location.href='/placementStatus?company=")
                            .append(encodedName)
                            .append("'\">")
                            .append("<div class='company-card-header'>")
                            .append("<img class='company-logo' src='")
                            .append(logoUrl)
                            .append("' alt='")
                            .append(cname)
                            .append(" logo' onerror=\"this.style.display='none'\">")
                            .append("<div>")
                            .append("<div class='company-name'>").append(cname).append("</div>")
                            .append("<div class='company-role'>").append(role).append("</div>")
                            .append("</div>")
                            .append("</div>")
                            .append("<div class='company-meta'>")
                            .append("<span>").append(String.format("%.1f LPA", pkg)).append("</span>")
                            .append("<span>").append(hired).append(" placed</span>")
                            .append("</div>")
                            .append("</button>");
                }
            }

            // students placed in a particular company (if filter applied)
            if (companyFilter != null && !companyFilter.isBlank()) {
                String studentSql =
                        "select id, name, branch from student where placed=true and company_name = ? order by name";
                try (PreparedStatement ps = con.prepareStatement(studentSql)) {
                    ps.setString(1, companyFilter);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        studentRows.append("<tr>")
                                .append("<td>").append(rs.getInt("id")).append("</td>")
                                .append("<td>").append(rs.getString("name")).append("</td>")
                                .append("<td>").append(rs.getString("branch")).append("</td>")
                                .append("</tr>");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int unplaced = total - placed;
        String selectedCompanyTitle =
                (companyFilter != null && !companyFilter.isBlank())
                        ? "Students placed in " + companyFilter
                        : "Select a company card to view students";

        String html =
                pageStart("Analytics") +
                        stats(placed, unplaced) +

                        "<div class='analytics-layout'>" +
                        "<div class='card chart-card'>" +
                        "<h3>Placement Overview</h3>" +
                        "<canvas id='placementPie'></canvas>" +
                        "</div>" +

                        "<div class='card'>" +
                        "<h3>Company-wise Placements</h3>" +
                        table("<tr><th>Company</th><th>Role</th><th>Package</th><th>Placed</th></tr>", companyRows.toString()) +
                        "</div>" +
                        "</div>" +

                        "<div class='grid-2'>" +
                        "<div class='card'>" +
                        "<h3>Companies</h3>" +
                        "<div class='company-grid'>" + companyCards + "</div>" +
                        "</div>" +

                        "<div class='card'>" +
                        "<h3>" + selectedCompanyTitle + "</h3>" +
                        (studentRows.length() == 0 && companyFilter != null && !companyFilter.isBlank()
                                ? "<p class='muted'>No students placed in this company yet.</p>"
                                : table("<tr><th>ID</th><th>Name</th><th>Branch</th></tr>", studentRows.toString())) +
                        "</div>" +
                        "</div>" +

                        // inline script for pie chart
                        "<script>" +
                        "if(window.Chart){const ctx=document.getElementById('placementPie').getContext('2d');" +
                        "new Chart(ctx,{type:'pie',data:{labels:['Placed','Unplaced']," +
                        "datasets:[{data:[" + placed + "," + unplaced + "]," +
                        "backgroundColor:['#22c55e','#f97316']}]},options:{plugins:{legend:{position:'bottom'}}}});}" +
                        "</script>" +

                        pageEnd();

        send(response, html);
    }

    // ===== UI HELPERS (same structure as original) =====

    static String pageStart(String title) {
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                "<title>Placement Tracker - " + title + "</title>" +
                "<link rel='stylesheet' href='/style.css?v=20260318a'>" +
                "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>" +
                "</head><body>" +
                "<div class='sidebar'>" +
                "<div class='brand'><span class='logo-dot'></span>Placement Tracker</div>" +
                "<nav>" +
                "<a href='/'>Dashboard</a>" +
                "<a href='/viewStudents'>Students</a>" +
                "<a href='/viewCompanies'>Companies</a>" +
                "<a href='/placement'>Placement</a>" +
                "<a href='/placementStatus'>Analytics</a>" +
                "</nav>" +
                "</div>" +
                "<div class='main'>" +
                "<header class='topbar'><h1>" + title + "</h1></header>" +
                "<main class='content'>";
    }

    static String pageEnd() {
        return "</main></div></body></html>";
    }

    static String card(String t, String b) {
        return "<div class='card'><h3>" + t + "</h3>" + b + "</div>";
    }

    static String table(String h, String r) {
        return "<table>" + h + r + "</table>";
    }

    static String stats(int p, int u) {
        return "<div class='card card-wide'><div class='stats-row'>" +
                "<div class='stat-pill success'>Placed : " + p + "</div>" +
                "<div class='stat-pill warning'>Unplaced : " + u + "</div>" +
                "</div></div>";
    }

    static String statsCard(String label, String value, String tone) {
        return "<div class='stat-card " + tone + "'>" +
                "<div class='stat-label'>" + label + "</div>" +
                "<div class='stat-value'>" + value + "</div>" +
                "</div>";
    }

    static String companyLogo(String name) {
        if (name == null) return "/logos/generic.svg";
        String n = name.toLowerCase();
        if (n.contains("infosys")) {
            return "/logos/infosys.png";
        } else if (n.contains("tcs")) {
            return "/logos/tcs.png";
        } else if (n.contains("wipro")) {
            return "/logos/wipro.png";
        } else if (n.contains("accenture")) {
            return "/logos/Accenture1.png";
        } else if (n.contains("virtusa")) {
            return "/logos/virtusa.png";
        }
        return "/logos/generic.svg?c=" + URLEncoder.encode(name.substring(0, 1).toUpperCase(), StandardCharsets.UTF_8);
    }

    static void send(HttpServletResponse response, String html) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(b.length);
        try (PrintWriter out = response.getWriter()) {
            out.write(html);
        }
    }

    static void redirect(HttpServletResponse response, String url) throws IOException {
        response.sendRedirect(url);
    }
}

