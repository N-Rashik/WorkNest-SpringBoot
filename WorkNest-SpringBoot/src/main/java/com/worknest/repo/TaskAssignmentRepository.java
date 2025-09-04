package com.worknest.repo;

import com.worknest.model.TaskAssignment;
import com.worknest.model.Task;
import com.worknest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByTask(Task task);
    List<TaskAssignment> findByAssignedUser(User user);
}
