package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long userId, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime nowTime, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime nowTime, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId,
                                                                          LocalDateTime nowStart,
                                                                          LocalDateTime nowEnd, Pageable pageable);

    @Query(value = "select * from bookings b" +
            " where b.booker_id = ?1 and b.status = ?2  order by b.start_date desc", nativeQuery = true)
    List<Booking> findByBookerIdAndStatus(Long userId, String state, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id in " +
            "(select i.id from items i " +
            "where i.owner_id =?1) " +
            "order by b.start_date desc", nativeQuery = true)
    List<Booking> findByOwnerId(Long ownerId, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id in " +
            "(select i.id from items i " +
            "where i.owner_id =?1) " +
            "and b.start_date > current_timestamp order by b.start_date desc", nativeQuery = true)
    List<Booking> findByOwnerIdFuture(Long ownerId, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id in (select i.id from items i where i.owner_id =?1) " +
            "and b.end_date < current_timestamp " +
            "order by b.start_date desc", nativeQuery = true)
    List<Booking> findByOwnerIdPast(Long ownerId, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id in (select i.id from items i where i.owner_id =?1) " +
            "and b.start_date < current_timestamp and b.end_date > current_timestamp " +
            "order by b.start_date desc", nativeQuery = true)
    List<Booking> findByOwnerIdCurrent(Long ownerId, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id in (select i.id from items i where i.owner_id =?1) " +
            "and b.status = ?2 " +
            "order by b.start_date desc", nativeQuery = true)
    List<Booking> findByOwnerIdState(Long ownerId, String state, Pageable pageable);

    @Query(value = "select * from bookings b " +
            "where b.item_id=?1 and b.start_date < current_timestamp and b.status = 'APPROVED' " +
            "order by b.end_date desc limit 1", nativeQuery = true)
    Booking findByItemIdLast(Long itemId);

    @Query(value = "select * from bookings b " +
            "where b.item_id=?1  and b.start_date > current_timestamp and b.status = 'APPROVED' " +
            "order by b.start_date limit 1", nativeQuery = true)
    Booking findByItemIdNext(Long itemId);

    @Query(value = "select * from bookings b " +
            "where b.item_id = ?1 and b.booker_id = ?2 and b.end_date < current_timestamp " +
            "order by b.end_date asc limit 1", nativeQuery = true)
    Booking findBookingForComment(Long itemId, Long bookerId);
}