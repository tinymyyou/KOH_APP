package com.example.koh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "reservation_slots",
        uniqueConstraints = @UniqueConstraint(name = "uk_reservation_slot_date_time", columnNames = {"reservation_date", "reservation_time"}))
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false, length = 5)
    private String reservationTime;

    @Column(name = "reserved_party_size", nullable = false)
    private Integer reservedPartySize;

    public Long getId() {
        return id;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public Integer getReservedPartySize() {
        return reservedPartySize;
    }

    public void setReservedPartySize(Integer reservedPartySize) {
        this.reservedPartySize = reservedPartySize;
    }
}
