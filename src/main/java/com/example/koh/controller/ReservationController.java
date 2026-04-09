package com.example.koh.controller;

import com.example.koh.form.ReservationForm;
import com.example.koh.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @ModelAttribute("reservationForm")
    public ReservationForm reservationForm() {
        return new ReservationForm();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/reserve")
    public String reserve() {
        return "reserve";
    }

    @PostMapping("/reserve")
    public String submitReserve(@Valid @ModelAttribute("reservationForm") ReservationForm reservationForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "reserve";
        }
        if (!reservationService.reserveIfCapacityAvailable(reservationForm)) {
            bindingResult.rejectValue("partySize", "capacity.exceeded",
                    "この時間枠は定員10名を超えるため予約できません。人数を調整してください。");
            return "reserve";
        }
        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String thanks() {
        return "thanks";
    }

    @GetMapping("/reservations")
    public String reservations() {
        return "redirect:/thanks";
    }

}
