package com.example.koh.repository;

import com.example.koh.entity.ReservationSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
    @Modifying
    @Query(value = "merge into reservation_slots (reservation_date, reservation_time, reserved_party_size) key (reservation_date, reservation_time) values (:reservationDate, :reservationTime, :reservedPartySize)", nativeQuery = true)
    void upsertSlot(@Param("reservationDate") LocalDate reservationDate,
                    @Param("reservationTime") String reservationTime,
                    @Param("reservedPartySize") int reservedPartySize);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ReservationSlot s where s.reservationDate = :reservationDate and s.reservationTime = :reservationTime")
    Optional<ReservationSlot> findByReservationDateAndReservationTimeForUpdate(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("reservationTime") String reservationTime);
}
