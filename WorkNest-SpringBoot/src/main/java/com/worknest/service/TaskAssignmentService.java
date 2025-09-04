package com.worknest.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worknest.model.Task;
import com.worknest.model.TaskAssignment;
import com.worknest.model.User;
import com.worknest.repo.TaskAssignmentRepository;

@Service
public class TaskAssignmentService {

    private final TaskAssignmentRepository repository;

    public TaskAssignmentService(TaskAssignmentRepository repository) {
        this.repository = repository;
    }

    // Assign task to multiple users
    @Transactional
    public void assignTaskToUsers(Task task, List<User> users) {
        for (User user : users) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTask(task);
            assignment.setAssignedUser(user);
            assignment.setStatus("PENDING");
            repository.save(assignment);
        }
    }

    // Reassign a task from one user to another
    @Transactional
    public void reassignTask(TaskAssignment assignment, User toUser) {
        // Mark current assignment as REASSIGNED
        assignment.setStatus("REASSIGNED");
        repository.save(assignment);

        // Check if toUser already has assignment for this task
        boolean exists = repository.findByTask(assignment.getTask()).stream()
            .anyMatch(a -> a.getAssignedUser() != null && a.getAssignedUser().getUserId().equals(toUser.getUserId()));

        if (!exists) {
            TaskAssignment newAssignment = new TaskAssignment();
            newAssignment.setTask(assignment.getTask());
            newAssignment.setAssignedUser(toUser);
            newAssignment.setStatus("PENDING");
            newAssignment.setLastUpdatedBy(toUser != null ? toUser.getName() : "SYSTEM");
            newAssignment.setUpdatedAt(new java.util.Date());
            repository.save(newAssignment);
        }
    }
}
