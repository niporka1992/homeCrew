package ru.homecrew.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.homecrew.entity.UserExternalIds;

public interface UserExternalIdsRepository extends JpaRepository<UserExternalIds, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<UserExternalIds> findByTelegramChatId(Long chatId);

    Optional<UserExternalIds> findByUser_Id(Long id);
}
