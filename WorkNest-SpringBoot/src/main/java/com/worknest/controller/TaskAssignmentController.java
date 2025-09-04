package com.worknest.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.worknest.model.Task;
import com.worknest.model.TaskAssignment;
import com.worknest.model.User;
import com.worknest.repo.TaskAssignmentRepository;
import com.worknest.repo.TaskRepository;
import com.worknest.repo.UserRepository;
import com.worknest.service.TaskAssignmentService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/assignments")
public class TaskAssignmentController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final TaskAssignmentService assignmentService;

    public TaskAssignmentController(TaskRepository taskRepository, UserRepository userRepository,
                                    TaskAssignmentRepository assignmentRepository, TaskAssignmentService assignmentService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
    }

    // Show form to assign task to multiple users (admin)
    @GetMapping("/admin/assign/{taskId}")
    public String showAssignForm(@PathVariable Long taskId, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/admin/dashboard";
        model.addAttribute("task", task);
        model.addAttribute("users", userRepository.findAll());
        return "admin-assign-multiple";
    }

    // Assign task to multiple users (admin)
    @PostMapping("/admin/assign")
    public String assignToMultiple(@RequestParam Long taskId,
                                   @RequestParam(value = "userIds", required = false) List<Long> userIds,
                                   HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/admin/login";
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return "redirect:/admin/dashboard";

        if (userIds != null && !userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            assignmentService.assignTaskToUsers(task, users);
        }
        return "redirect:/admin/dashboard";
    }

    // Show reassign form for user
    @GetMapping("/user/reassign/{assignmentId}")
    public String showUserReassign(@PathVariable Long assignmentId, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/user/login";
        TaskAssignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) return "redirect:/tasks/my";

        Task task = assignment.getTask();
        List<TaskAssignment> assignments = assignmentRepository.findByTask(task);
        List<User> otherUsers = assignments.stream()
                .map(TaskAssignment::getAssignedUser)
                .filter(u -> u != null && !u.getUserId().equals(assignment.getAssignedUser().getUserId()))
                .collect(Collectors.toList());

        model.addAttribute("assignment", assignment);
        model.addAttribute("otherUsers", otherUsers);
        model.addAttribute("task", task);
        return "user-reassign";
    }

    // Perform reassign action
    @PostMapping("/user/reassign")
    public String doUserReassign(@RequestParam Long assignmentId,
                                 @RequestParam Long toUserId,
                                 HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/user/login";

        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assignment ID: " + assignmentId));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + toUserId));

        assignmentService.reassignTask(assignment, toUser);
        return "redirect:/tasks/my";
    }
        
        @PostMapping("/admin/delete/{taskId}")
        public String deleteTask(@PathVariable Long taskId, HttpSession session) {
            if (session.getAttribute("admin") == null) return "redirect:/admin/login";

            // Find the task
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) return "redirect:/admin/dashboard";

            // Delete all assignments for this task first
            List<TaskAssignment> assignments = assignmentRepository.findByTask(task);
            assignmentRepository.deleteAll(assignments);

            // Delete the task
            taskRepository.delete(task);

            return "redirect:/admin/dashboard";
        

    }
}
