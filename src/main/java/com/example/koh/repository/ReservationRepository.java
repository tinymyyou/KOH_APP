package com.example.koh.repository;

import com.example.koh.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select coalesce(sum(r.partySize), 0) from Reservation r where r.reservationDate = :reservationDate and trim(function('replace', r.reservationTime, ':00', '')) = :reservationTime")
    Integer sumPartySizeByReservationDateAndReservationTime(@Param("reservationDate") LocalDate reservationDate,
                                                            @Param("reservationTime") String reservationTime);
}
