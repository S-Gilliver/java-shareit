package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId, Pageable pageable);

    @Query(value = "select * from items i " +
            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper (concat('%', ?1, '%')) " +
            "and i.is_available = true", nativeQuery = true)
    List<Item> search(String query, Pageable page);
    List<Item> findByRequestId(long requestId);
}
