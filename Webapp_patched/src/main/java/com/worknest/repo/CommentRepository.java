package com.worknest.repo;

import com.worknest.model.Comment;
import com.worknest.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Fetch all comments for a specific task, newest first
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);

    // Fetch all comments, newest first
    List<Comment> findAllByOrderByCreatedAtDesc();
}
