package com.smartcampus.repository;

import com.smartcampus.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Two bookings overlap when each starts before the other one ends.
    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status IN ('CONFIRMED', 'ACTIVE') " +
           "AND ((r.startTime < :endTime) AND (r.endTime > :startTime))")
    List<Reservation> findConflictingReservations(
        @Param("roomId") Long roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}