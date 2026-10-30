package com.smartcampus.dto;
import com.smartcampus.model.Reservation;
import java.time.LocalDateTime;


/**
    * Carries the CSP outcome together with the persisted Reservation on success.
*/
public class BookingResponseDto {
        public boolean success;
        public String message;
        public Long reservationId;
        public RoomDetailsDto roomDetails;
        public String startTime;
        public String endTime;
        public String status;

        public BookingResponseDto() {}

        public BookingResponseDto(boolean success, String message, Reservation reservation, RoomDetailsDto roomDetails) {
            this.success = success;
            this.message = message;
            this.roomDetails = roomDetails;
            if (reservation != null) {
                this.reservationId = reservation.getId();
                this.startTime = reservation.getStartTime() != null ? reservation.getStartTime().toString() : null;
                this.endTime = reservation.getEndTime() != null ? reservation.getEndTime().toString() : null;
                this.status = reservation.getStatus();
            }
        }
    }