package controller;

import dal.UserDAO;
import models.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterServlet extends HttpServlet {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@fpt\\.edu\\.vn$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/view/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String error = validateInput(fullName, email, password, confirmPassword);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/register.jsp").forward(request, response);
            return;
        }
        UserDAO ud = new UserDAO();
        if (ud.getUserByEmail(email.trim()) != null) {
            request.setAttribute("error", "Email này đã được sử dụng!");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/register.jsp").forward(request, response);
            return;
        }

        Users newUser = new Users();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPassword(password.trim());
        newUser.setPermissionID(1); // Default: Student
        newUser.setStatus(true);    // Active

        boolean success = ud.register(newUser);
        if (success) {
            // Tự động đăng nhập
            HttpSession session = request.getSession();
            session.setAttribute("user", newUser);
            session.setAttribute("permissionID", newUser.getPermissionID());
            session.setMaxInactiveInterval(30 * 60); // 30 phút

            int permissionID = newUser.getPermissionID();
            if (permissionID == 1) {
                // Chuyển hướng đến trang chủ - HomepageServlet
                response.sendRedirect(request.getContextPath() + "/");
            }
            if(permissionID == 2){
                response.sendRedirect(request.getContextPath() + "/admin");
            }
            if(permissionID == 3){
                response.sendRedirect(request.getContextPath() + "/ic");
            }
        } else {
            request.setAttribute("error", "Đăng ký thất bại! Vui lòng thử lại.");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/view/register.jsp").forward(request, response);
        }
    }

    private String validateInput(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100) {
            return "Họ tên phải từ 2 đến 100 ký tự!";
        }

        if (email == null || email.trim().isEmpty()) {
            return "Vui lòng nhập email!";
        }
        if (email.trim().length() > 255 || !pattern.matcher(email.trim()).matches()) {
            return "Vui lòng sử dụng email@fpt.edu.vn";
        }

        if (password == null || password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự!";
        }

        if (!password.equals(confirmPassword)) {
            return "Xác nhận mật khẩu không khớp!";
        }

        return null;
    }
}
