package com.worknest.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worknest.model.Task;
import com.worknest.model.User;
import com.worknest.repo.TaskRepository;
import com.worknest.repo.UserRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private UserRepository userRepo;

    // Get task by ID
    public Task getTaskById(Long taskId) {
        return taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    // Get team members excluding current user
    public List<User> getTeamMembersExcludingCurrent(Long currentUserId) {
        User currentUser = userRepo.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userRepo.findByTeam(currentUser.getTeam())
                             .stream()
                             .filter(u -> !u.getUserId().equals(currentUserId))
                             .collect(Collectors.toList());
    }

    // Reassign task directly
    public void reassignTask(Long taskId, Long fromUserId, Long toUserId) {
        Task task = getTaskById(taskId);

        User fromUser = userRepo.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User toUser = userRepo.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove current user and assign new user
        task.getAssignedUsers().remove(fromUser);
        task.getAssignedUsers().add(toUser);

        // Update status
        task.setStatus("ASSIGNED");

        taskRepo.save(task);
    }

    // Update task status
    public void updateTaskStatus(Long taskId, String status) {
        Task task = getTaskById(taskId);
        task.setStatus(status);
        taskRepo.save(task);
    }
}
