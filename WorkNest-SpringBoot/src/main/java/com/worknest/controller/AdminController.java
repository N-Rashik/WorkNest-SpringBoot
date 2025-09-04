package com.worknest.controller;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.worknest.model.Admin;
import com.worknest.model.Comment;
import com.worknest.model.Task;
import com.worknest.model.User;
import com.worknest.repo.AdminRepository;
import com.worknest.repo.CommentRepository;
import com.worknest.repo.TaskRepository;
import com.worknest.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    public AdminController(AdminRepository adminRepository, UserRepository userRepository,
                           TaskRepository taskRepository, CommentRepository commentRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
    }

    // --- Admin Login ---
    @GetMapping("/login")
    public String showLogin() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        Admin admin = adminRepository.findByEmail(email)
                .filter(a -> a.getPassword().equals(password))
                .orElse(null);

        if (admin == null) {
            model.addAttribute("error", "Invalid credentials");
            return "admin-login";
        }

        session.setAttribute("admin", admin);
        return "redirect:/admin/dashboard";
    }

    // --- Admin Registration ---
    @GetMapping("/register")
    public String showRegister() {
        return "admin-register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             Model model) {

        if (adminRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "admin-register";
        }

        Admin a = new Admin();
        a.setName(name);
        a.setEmail(email);
        a.setPassword(password);
        adminRepository.save(a);

        return "redirect:/admin/login";
    }

    // --- Logout ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // --- Admin Dashboard ---
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        List<User> users = userRepository.findAll();
        List<Task> tasks = taskRepository.findAll();

        model.addAttribute("users", users);
        model.addAttribute("tasks", tasks);

        // Tasks by status
        model.addAttribute("pendingTasks", tasks.stream().filter(t -> "PENDING".equals(t.getStatus())).collect(Collectors.toList()));
        model.addAttribute("inProgressTasks", tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).collect(Collectors.toList()));
        model.addAttribute("completedTasks", tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).collect(Collectors.toList()));
        model.addAttribute("delayedTasks", tasks.stream().filter(t -> "DELAYED".equals(t.getStatus())).collect(Collectors.toList()));

        return "admin-dashboard";
    }

    // --- User CRUD Operations ---

    // Show form to create new user
    @GetMapping("/user/new")
    public String newUserForm(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        return "user-form"; // create user-form.html for this
    }

    // Create a new user
    @PostMapping("/user/create")
    public String createUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             HttpSession session,
                             Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "user-form";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        userRepository.save(user);

        return "redirect:/admin/dashboard";
    }

    // Show form to edit existing user
    @GetMapping("/user/edit/{userId}")
    public String editUserForm(@PathVariable Long userId, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/admin/dashboard";

        model.addAttribute("user", user);
        return "user-form"; // reuse user-form.html
    }

    // Update existing user
    @PostMapping("/user/update")
    public String updateUser(@RequestParam Long userId,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             HttpSession session,
                             Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/admin/dashboard";

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        userRepository.save(user);

        return "redirect:/admin/dashboard";
    }

    // Delete user
    @PostMapping("/user/delete/{userId}")
    public String deleteUser(@PathVariable Long userId, 
                             HttpSession session, 
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/admin/dashboard";

        // Check if user has comments
        if (user.getComments() != null && !user.getComments().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "❌ Cannot delete user because they are related to tasks assigned.");
            return "redirect:/admin/dashboard";
        }

        // Check if user is assigned to tasks
        if (user.getTasks() != null && !user.getTasks().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "❌ Cannot delete user because they are assigned to tasks.");
            return "redirect:/admin/dashboard";
        }

        // Safe to delete
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success", "✅ User deleted successfully.");

        return "redirect:/admin/dashboard";
    }


    // --- Task CRUD Operations ---

    @GetMapping("/task/new")
    public String newTaskForm(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        model.addAttribute("users", userRepository.findAll());
        return "task-form";
    }

    @PostMapping("/task/create")
    public String createTask(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam List<Long> assignedUserIds,
                             @RequestParam String startDate,
                             @RequestParam String dueDate,
                             HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);

        List<User> assignedUsers = userRepository.findAllById(assignedUserIds);
        task.setAssignedUsers(assignedUsers);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            task.setStartDate(sdf.parse(startDate));
            task.setDueDate(sdf.parse(dueDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        task.setStatus("PENDING");
        taskRepository.save(task);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/task/edit/{taskId}")
    public String editTaskForm(@PathVariable Long taskId, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/admin/dashboard";

        model.addAttribute("task", task);
        model.addAttribute("users", userRepository.findAll());
        return "task-form";
    }

    @PostMapping("/task/update")
    public String updateTask(@RequestParam Long taskId,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam List<Long> assignedUserIds,
                             @RequestParam String startDate,
                             @RequestParam String dueDate,
                             @RequestParam String status,   // ✅ added
                             HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/admin/dashboard";

        task.setTitle(title);
        task.setDescription(description);

        List<User> assignedUsers = userRepository.findAllById(assignedUserIds);
        task.setAssignedUsers(assignedUsers);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            task.setStartDate(sdf.parse(startDate));
            task.setDueDate(sdf.parse(dueDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        task.setStatus(status); // ✅ save selected status

        taskRepository.save(task);

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/task/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        taskRepository.deleteById(taskId);
        return "redirect:/admin/dashboard";
    }
    

    // --- Task Comments ---
    @GetMapping("/task/comments/{taskId}")
    public String viewTaskComments(@PathVariable Long taskId, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/admin/dashboard";

        List<Comment> comments = commentRepository.findByTaskOrderByCreatedAtDesc(task);

        // Mark comments as seen by admin
        comments.forEach(c -> {
            if (!c.isSeenByAdmin()) {
                c.setSeenByAdmin(true);
                commentRepository.save(c);
            }
        });

        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        model.addAttribute("isAdmin", true);

        return "admin-task-comments";
    }
}
