package com.example.koh;

import com.example.koh.entity.Reservation;
import com.example.koh.form.ReservationForm;
import com.example.koh.repository.ReservationRepository;
import com.example.koh.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
    }

    @Test
    void sameSlotConcurrentSixAndSix_withExistingReservation_onlyOneSucceedsAndTotalNeverExceedsTen() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 1);
        String time = "11";
        seedReservation(date, time, 1, "seed-a@example.com", "09000000000");

        ReservationForm sixA = createForm(date, time, 6, "user-a@example.com", "09000000001");
        ReservationForm sixB = createForm(date, time, 6, "user-b@example.com", "09000000002");

        List<Boolean> results = runConcurrently(
                () -> reservationService.reserveIfCapacityAvailable(sixA),
                () -> reservationService.reserveIfCapacityAvailable(sixB));

        assertThat(results).containsExactlyInAnyOrder(true, false);
        assertThat(reservationService.sumPartySizeBySlot(date, time)).isEqualTo(7);
        assertThat(reservationService.sumPartySizeBySlot(date, time)).isLessThanOrEqualTo(10);
    }

    @Test
    void sameSlotWithExistingEight_concurrentTwoAndThree_onlyOneSucceeds() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 2);
        String time = "11";
        seedReservation(date, time, 8, "seed@example.com", "09000000003");

        ReservationForm plusTwo = createForm(date, time, 2, "two@example.com", "09000000004");
        ReservationForm plusThree = createForm(date, time, 3, "three@example.com", "09000000005");

        List<Boolean> results = runConcurrently(
                () -> reservationService.reserveIfCapacityAvailable(plusTwo),
                () -> reservationService.reserveIfCapacityAvailable(plusThree));

        assertThat(results).containsExactlyInAnyOrder(true, false);
        int finalTotal = reservationService.sumPartySizeBySlot(date, time);
        assertThat(finalTotal).isEqualTo(10);
        assertThat(finalTotal).isLessThanOrEqualTo(10);
    }

    @Test
    void differentSlotsConcurrent_requestsDoNotInterfere() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 3);

        ReservationForm time11 = createForm(date, "11", 6, "slot11@example.com", "09000000006");
        ReservationForm time13 = createForm(date, "13", 6, "slot13@example.com", "09000000007");

        List<Boolean> results = runConcurrently(
                () -> reservationService.reserveIfCapacityAvailable(time11),
                () -> reservationService.reserveIfCapacityAvailable(time13));

        assertThat(results).containsExactlyInAnyOrder(true, true);
        assertThat(reservationService.sumPartySizeBySlot(date, "11")).isEqualTo(6);
        assertThat(reservationService.sumPartySizeBySlot(date, "13")).isEqualTo(6);
    }

    private List<Boolean> runConcurrently(Callable<Boolean> task1, Callable<Boolean> task2) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> future1 = executor.submit(wrapWithLatch(task1, ready, start));
            Future<Boolean> future2 = executor.submit(wrapWithLatch(task2, ready, start));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<Boolean> results = new ArrayList<>();
            results.add(future1.get(10, TimeUnit.SECONDS));
            results.add(future2.get(10, TimeUnit.SECONDS));
            return results;
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Callable<Boolean> wrapWithLatch(Callable<Boolean> delegate, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
            return delegate.call();
        };
    }

    private ReservationForm createForm(LocalDate date, String time, int partySize, String email, String phone) {
        ReservationForm form = new ReservationForm();
        form.setReservationDate(date);
        form.setReservationTime(time);
        form.setPartySize(partySize);
        form.setName("Concurrency Test");
        form.setEmail(email);
        form.setPhone(phone);
        form.setNote("concurrency");
        return form;
    }

    private void seedReservation(LocalDate date, String time, int partySize, String email, String phone) {
        Reservation seed = new Reservation();
        seed.setReservationDate(date);
        seed.setReservationTime(time);
        seed.setPartySize(partySize);
        seed.setName("Seed User");
        seed.setEmail(email);
        seed.setPhone(phone);
        seed.setNote("seed");
        reservationRepository.save(seed);
    }
}
