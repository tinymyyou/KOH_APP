package com.example.koh;

import com.example.koh.entity.Reservation;
import com.example.koh.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void postReserveSavesDbAndReservationsUrlRedirectsToThanks() throws Exception {
        long beforeCount = reservationRepository.count();

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-04-05")
                        .param("reservationTime", "11")
                        .param("partySize", "2")
                        .param("name", "Flow Test User")
                .param("email", "flow-test@example.com")
                .param("phone", "09011112222")
                .param("note", "web flow test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        long afterCount = reservationRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount + 1);

        List<Reservation> savedReservations = reservationRepository.findAll();
        assertThat(savedReservations)
                .extracting(Reservation::getEmail)
                .contains("flow-test@example.com");

        mockMvc.perform(get("/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));
    }

    @Test
    void reservationsUrlDirectAccessRedirectsToThanks() throws Exception {
        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-04-05")
                        .param("reservationTime", "11")
                        .param("partySize", "1")
                        .param("name", "Older User")
                        .param("email", "older@example.com")
                        .param("phone", "09000000001"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-04-06")
                        .param("reservationTime", "13")
                        .param("partySize", "2")
                        .param("name", "Newer User")
                        .param("email", "newer@example.com")
                .param("phone", "09000000002"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));
    }

    @Test
    void postReserveWithInvalidValuesReturnsReserveWithErrors() throws Exception {
        long beforeCount = reservationRepository.count();

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "")
                        .param("reservationTime", "99")
                        .param("partySize", "0")
                        .param("name", "   ")
                        .param("email", "invalid-email")
                        .param("phone", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reserve"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("入力内容をご確認ください。")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("メールアドレスの形式が正しくありません。")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("人数は1名以上で入力してください。")));

        long afterCount = reservationRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount);
    }

    @Test
    void postReserveWhenExistingEightAndNewTwoAllowsSave() throws Exception {
        long beforeCount = reservationRepository.count();

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-05-02")
                        .param("reservationTime", "11")
                        .param("partySize", "8")
                        .param("name", "Eight User")
                        .param("email", "eight@example.com")
                        .param("phone", "09033334444"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-05-02")
                        .param("reservationTime", "11")
                        .param("partySize", "2")
                        .param("name", "Two User")
                        .param("email", "two@example.com")
                        .param("phone", "09055556666"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        long afterCount = reservationRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount + 2);
    }

    @Test
    void postReserveWhenExistingEightAndNewThreeRejectsWithoutSaving() throws Exception {
        long beforeCount = reservationRepository.count();

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-05-03")
                        .param("reservationTime", "13")
                        .param("partySize", "8")
                        .param("name", "Existing Eight User")
                        .param("email", "existing-eight@example.com")
                        .param("phone", "09077778888"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/thanks"));

        long afterFirstSaveCount = reservationRepository.count();
        assertThat(afterFirstSaveCount).isEqualTo(beforeCount + 1);

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-05-03")
                        .param("reservationTime", "13")
                        .param("partySize", "3")
                        .param("name", "Overflow User")
                        .param("email", "overflow@example.com")
                        .param("phone", "09099990000"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserve"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("定員10名")));

        long afterSecondPostCount = reservationRepository.count();
        assertThat(afterSecondPostCount).isEqualTo(afterFirstSaveCount);
    }

    @Test
    void postReserveAlsoCountsLegacyTimeFormatWithColon() throws Exception {
        long beforeCount = reservationRepository.count();

        Reservation legacyReservation = new Reservation();
        legacyReservation.setReservationDate(java.time.LocalDate.of(2026, 5, 4));
        legacyReservation.setReservationTime("11:00");
        legacyReservation.setPartySize(8);
        legacyReservation.setName("Legacy Time User");
        legacyReservation.setEmail("legacy-time@example.com");
        legacyReservation.setPhone("09012121212");
        reservationRepository.save(legacyReservation);

        long afterLegacySaveCount = reservationRepository.count();
        assertThat(afterLegacySaveCount).isEqualTo(beforeCount + 1);

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-05-04")
                        .param("reservationTime", "11")
                        .param("partySize", "3")
                        .param("name", "New User")
                        .param("email", "new-user@example.com")
                        .param("phone", "09034343434"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserve"));

        long afterSecondPostCount = reservationRepository.count();
        assertThat(afterSecondPostCount).isEqualTo(afterLegacySaveCount);
    }

    @Test
    void postReserveAlsoCountsPaddedTimeValue() throws Exception {
        long beforeCount = reservationRepository.count();

        Reservation paddedReservation = new Reservation();
        paddedReservation.setReservationDate(java.time.LocalDate.of(2026, 4, 5));
        paddedReservation.setReservationTime("11   ");
        paddedReservation.setPartySize(45);
        paddedReservation.setName("Padded Time User");
        paddedReservation.setEmail("padded-time@example.com");
        paddedReservation.setPhone("09056565656");
        reservationRepository.save(paddedReservation);

        long afterPaddedSaveCount = reservationRepository.count();
        assertThat(afterPaddedSaveCount).isEqualTo(beforeCount + 1);

        mockMvc.perform(post("/reserve")
                        .param("reservationDate", "2026-04-05")
                        .param("reservationTime", "11")
                        .param("partySize", "1")
                        .param("name", "Another User")
                        .param("email", "another@example.com")
                        .param("phone", "09078787878"))
                .andExpect(status().isOk())
                .andExpect(view().name("reserve"));

        long afterSecondPostCount = reservationRepository.count();
        assertThat(afterSecondPostCount).isEqualTo(afterPaddedSaveCount);
    }
}
