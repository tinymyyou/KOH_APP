package com.example.koh.service;

import com.example.koh.entity.Reservation;
import com.example.koh.entity.ReservationSlot;
import com.example.koh.form.ReservationForm;
import com.example.koh.repository.ReservationRepository;
import com.example.koh.repository.ReservationSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationSlotRepository reservationSlotRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationSlotRepository = reservationSlotRepository;
    }

    @Transactional
    public Reservation createReservation(ReservationForm form) {
        Reservation reservation = new Reservation();
        reservation.setReservationDate(form.getReservationDate());
        reservation.setReservationTime(form.getReservationTime());
        reservation.setPartySize(form.getPartySize());
        reservation.setName(form.getName());
        reservation.setEmail(form.getEmail());
        reservation.setPhone(form.getPhone());
        reservation.setNote(form.getNote());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public boolean reserveIfCapacityAvailable(ReservationForm form) {
        String reservationTime = form.getReservationTime().trim();
        ReservationSlot slot = lockSlotForUpdate(form.getReservationDate(), reservationTime);
        int currentReservedPartySize = sumPartySizeBySlot(form.getReservationDate(), reservationTime);
        if (!slot.getReservedPartySize().equals(currentReservedPartySize)) {
            slot.setReservedPartySize(currentReservedPartySize);
        }

        int reservedPartySize = slot.getReservedPartySize();
        int requestedPartySize = form.getPartySize();
        int capacityLimit = 10;
        if (reservedPartySize + requestedPartySize > capacityLimit) {
            logger.warn(
                    "Reservation rejected due to capacity exceeded. reservationDate={}, reservationTime={}, reservedPartySize={}, requestedPartySize={}, capacityLimit={}",
                    form.getReservationDate(),
                    reservationTime,
                    reservedPartySize,
                    requestedPartySize,
                    capacityLimit);
            return false;
        }
        createReservation(form);
        slot.setReservedPartySize(reservedPartySize + requestedPartySize);
        reservationSlotRepository.save(slot);
        logger.info(
                "Reservation completed. reservationDate={}, reservationTime={}, partySize={}",
                form.getReservationDate(),
                reservationTime,
                requestedPartySize);
        return true;
    }

    @Transactional(readOnly = true)
    public int sumPartySizeBySlot(LocalDate reservationDate, String reservationTime) {
        Integer total = reservationRepository.sumPartySizeByReservationDateAndReservationTime(reservationDate, reservationTime);
        return total != null ? total : 0;
    }

    private ReservationSlot lockSlotForUpdate(LocalDate reservationDate, String reservationTime) {
        return reservationSlotRepository.findByReservationDateAndReservationTimeForUpdate(reservationDate, reservationTime)
                .orElseGet(() -> createAndLockSlot(reservationDate, reservationTime));
    }

    private ReservationSlot createAndLockSlot(LocalDate reservationDate, String reservationTime) {
        reservationSlotRepository.upsertSlot(
                reservationDate,
                reservationTime,
                sumPartySizeBySlot(reservationDate, reservationTime));
        return reservationSlotRepository.findByReservationDateAndReservationTimeForUpdate(reservationDate, reservationTime)
                .orElseThrow(() -> new IllegalStateException("Failed to lock reservation slot"));
    }
}
