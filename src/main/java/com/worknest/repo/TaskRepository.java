package com.worknest.repo;

import com.worknest.model.Task;
import com.worknest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Fetch tasks by User entity
    List<Task> findByAssignedUsers(User user);

    // Fetch tasks by userId
    List<Task> findByAssignedUsers_UserId(Long userId);
}
