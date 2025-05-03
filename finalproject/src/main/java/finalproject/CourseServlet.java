package finalproject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Course> courses = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finalproject", "root", "AWang@SQL01!");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CourseID, CourseName FROM Courses")) {

            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("CourseID"));
                c.setCourseName(rs.getString("CourseName"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().print(gson.toJson(courses));
    }

    class Course {
        private int courseId;
        private String courseName;
        public void setCourseId(int id) { this.courseId = id; }
        public void setCourseName(String name) { this.courseName = name; }
    }
}
