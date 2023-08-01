package ru.practicum.main.users.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.users.model.User;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    List<User> findUsersById(int userId, Pageable page);
}
