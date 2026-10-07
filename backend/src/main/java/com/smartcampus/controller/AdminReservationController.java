package com.smartcampus.controller;

import com.smartcampus.model.Reservation;
import com.smartcampus.repository.ReservationRepository;
import com.smartcampus.repository.RoomRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public AdminReservationController(ReservationRepository reservationRepository,
                                      UserRepository userRepository,
                                      RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        ResponseEntity<?> validation = validateReservation(reservation);
        if (validation != null) return validation;
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationRepository.save(reservation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody Reservation updatedData) {
        return reservationRepository.findById(Objects.requireNonNull(id)).map(existingReservation -> {
            if (updatedData.getUserId() != null) existingReservation.setUserId(updatedData.getUserId());
            if (updatedData.getRoomId() != null) existingReservation.setRoomId(updatedData.getRoomId());
            if (updatedData.getStartTime() != null) existingReservation.setStartTime(updatedData.getStartTime());
            if (updatedData.getEndTime() != null) existingReservation.setEndTime(updatedData.getEndTime());
            if (updatedData.getStatus() != null) existingReservation.setStatus(updatedData.getStatus());
            ResponseEntity<?> validation = validateReservation(existingReservation);
            if (validation != null) return validation;
            return ResponseEntity.ok((Object) reservationRepository.save(existingReservation));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (!reservationRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validateReservation(Reservation reservation) {
        if (reservation.getUserId() == null || !userRepository.existsById(reservation.getUserId())) {
            return ResponseEntity.badRequest().body("User does not exist.");
        }
        if (reservation.getRoomId() == null || !roomRepository.existsById(reservation.getRoomId())) {
            return ResponseEntity.badRequest().body("Room does not exist.");
        }
        if (reservation.getStartTime() == null || reservation.getEndTime() == null
                || !reservation.getEndTime().isAfter(reservation.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time.");
        }
        return null;
    }
}
