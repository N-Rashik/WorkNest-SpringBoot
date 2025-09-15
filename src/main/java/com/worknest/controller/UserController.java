package com.worknest.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.worknest.model.Comment;
import com.worknest.model.Task;
import com.worknest.model.User;
import com.worknest.repo.CommentRepository;
import com.worknest.repo.TaskRepository;
import com.worknest.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepo;
    private final TaskRepository taskRepo;
    private final CommentRepository commentRepo;

    public UserController(UserRepository userRepo, TaskRepository taskRepo,CommentRepository commentRepo) {
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
        this.commentRepo=commentRepo;
    }

    // Show User Registration Page
    @GetMapping("/register")
    public String showRegister() {
        return "user-register";
    }

    // Handle User Registration
    @PostMapping("/register")
    public String doRegister(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             Model model) {

        if (userRepo.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "user-register";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");

        userRepo.save(user);
        return "redirect:/user/login";
    }

    // Show User Login Page
    @GetMapping("/login")
    public String showLogin() {
        return "user-login";
    }

    // Handle User Login
    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid credentials");
            return "user-login";
        }

        session.setAttribute("user", user);
        return "redirect:/user/dashboard";
    }

    // Show Edit User Form (Admin only)
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("user", user);
        return "user-edit";
    }

    // Handle User Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Dashboard with user tasks
    @GetMapping("/dashboard")
    public String showUserDashboard(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("user");
        if (loggedUser == null) {
            return "redirect:/user/login";
        }

        List<Task> userTasks = taskRepo.findByAssignedUsers(loggedUser);

        // For each task, only include assigned users excluding current user for dropdown
        userTasks.forEach(task -> {
            List<User> otherAssignedUsers = task.getAssignedUsers()
                                                .stream()
                                                .filter(u -> !u.getUserId().equals(loggedUser.getUserId()))
                                                .toList();
           
        });

        model.addAttribute("user", loggedUser);
        model.addAttribute("tasks", userTasks);

        return "user-dashboard";
    }

    

    // Update Task Status
    @PostMapping("/task/updateStatus")
    public String updateTaskStatus(@RequestParam Long taskId,
                                   @RequestParam String status,
                                   HttpSession session) {

        User loggedUser = (User) session.getAttribute("user");
        if (loggedUser == null) {
            return "redirect:/user/login";
        }

        Task task = taskRepo.findById(taskId).orElse(null);
        if (task == null || task.getAssignedUsers().stream().noneMatch(u -> u.getUserId().equals(loggedUser.getUserId()))) {
            return "redirect:/user/dashboard";
        }

        task.setStatus(status);
        taskRepo.save(task);

        return "redirect:/user/dashboard";
    }

    // Reassign Task without removing current user
    @PostMapping("/task/reassign")
    public String reassignTask(@RequestParam Long fromTaskId,
                               @RequestParam Long toUserId,
                               HttpSession session) {

        User loggedUser = (User) session.getAttribute("user");
        if (loggedUser == null) {
            return "redirect:/user/login";
        }

        Task task = taskRepo.findById(fromTaskId).orElse(null);
        User newUser = userRepo.findById(toUserId).orElse(null);

        if (task == null || newUser == null) {
            return "redirect:/user/dashboard";
        }

        // Add new user if not already assigned
        if (!task.getAssignedUsers().contains(newUser)) {
            task.getAssignedUsers().add(newUser);
        }

        // Track who reassigned this task for the new user
        if (task.getReassignedBy() == null) {
            task.setReassignedBy(new java.util.HashMap<>());
        }
        task.getReassignedBy().put(newUser.getUserId(), loggedUser.getName());

        // Update task status to show reassignment
        task.setStatus("Reassigned to " + newUser.getName() + " by " + loggedUser.getName());

        taskRepo.save(task);

        return "redirect:/user/dashboard";
    }



    // Update User (Admin only)
    @PostMapping("/update")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password) {
        User user = userRepo.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/dashboard";
        }

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        userRepo.save(user);
        return "redirect:/admin/dashboard";
    }

    // Optional: Delete User (Admin only)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userRepo.deleteById(id);
        return "redirect:/admin/dashboard";
    }
    @PostMapping("/task/comment")
    public String postComment(@RequestParam Long taskId,
                              @RequestParam String content,
                              HttpSession session) {

        User loggedUser = (User) session.getAttribute("user");
        if (loggedUser == null) {
            return "redirect:/user/login";
        }

        Task task = taskRepo.findById(taskId).orElse(null);
        if (task == null) {
            return "redirect:/user/dashboard";
        }

        // Create new comment
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(loggedUser); // ✅ set the actual logged-in user
        comment.setContent(content);
        comment.setCreatedAt(new java.util.Date());
        comment.setSeenByAdmin(false);

        commentRepo.save(comment); // Make sure you autowire CommentRepository

        return "redirect:/user/dashboard"; // or redirect to task details page
    }

}
