package com.worknest.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.worknest.model.Comment;
import com.worknest.model.Task;
import com.worknest.model.User;
import com.worknest.repo.CommentRepository;
import com.worknest.repo.TaskAssignmentRepository;
import com.worknest.repo.TaskRepository;
import com.worknest.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository,
                          CommentRepository commentRepository,
                          TaskAssignmentRepository taskAssignmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    // --- View Task Details ---
 // --- View Task Details ---
    @GetMapping("/view/{taskId}")
    public String viewTask(@PathVariable Long taskId, Model model, HttpSession session) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/tasks/my";

        checkAndUpdateDelay(task);

        List<Comment> comments = commentRepository.findByTaskOrderByCreatedAtDesc(task);

        // If admin is viewing, mark unseen comments as seen
        if (session.getAttribute("admin") != null) {
            comments.stream()
                    .filter(c -> !c.isSeenByAdmin())
                    .forEach(c -> {
                        c.setSeenByAdmin(true);
                        commentRepository.save(c);
                    });
        }

        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        model.addAttribute("isAdmin", session.getAttribute("admin") != null);
        
        java.util.List<com.worknest.model.TaskAssignment> assignments = taskAssignmentRepository.findByTask(task);
        model.addAttribute("assignments", assignments);
        model.addAttribute("isUser", session.getAttribute("user") != null);
        return "task-view";
    }

    // --- Add Comment (User or Admin) ---
    @PostMapping("/comment/add")
    public String addComment(@RequestParam Long taskId, @RequestParam String content,
                             HttpSession session, Model model) {

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/tasks/my";

        User user;
        boolean isAdmin = false;

        if (session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        } else if (session.getAttribute("admin") != null) {
            // Use a dedicated persisted admin user
            user = userRepository.findById(1L) // assuming Admin user has ID = 1
                    .orElseThrow(() -> new RuntimeException("Admin user not found in DB"));
            isAdmin = true;
        } else {
            return "redirect:/user/login";
        }

        try {
            Comment comment = new Comment();
            comment.setTask(task);
            comment.setUser(user);
            comment.setContent(content);
            comment.setCreatedAt(new Date());

            // Mark as unseen if posted by regular user
            comment.setSeenByAdmin(false);

            commentRepository.save(comment);

            return "redirect:/tasks/view/" + taskId;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add comment: " + e.getMessage());
            List<Comment> comments = commentRepository.findByTaskOrderByCreatedAtDesc(task);
            model.addAttribute("task", task);
            model.addAttribute("comments", comments);
            model.addAttribute("isAdmin", session.getAttribute("admin") != null);
            return "task-view";
        }
    }

    // --- Utility: Update delayed tasks ---
    private void checkAndUpdateDelay(Task t) {
        if (t.getDueDate() != null && !"COMPLETED".equals(t.getStatus())) {
            Date today = new Date();
            if (t.getDueDate().before(today) && !"DELAYED".equals(t.getStatus())) {
                t.setStatus("DELAYED");
                taskRepository.save(t);
            }
        }
    }
}
