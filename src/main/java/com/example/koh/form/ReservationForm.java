package com.example.koh.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReservationForm {

    @NotNull(message = "予約希望日を選択してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reservationDate;

    @NotBlank(message = "時間枠を選択してください。")
    @Pattern(regexp = "11|13|15", message = "時間枠の値が不正です。")
    private String reservationTime;

    @NotNull(message = "人数を選択してください。")
    @Min(value = 1, message = "人数は1名以上で入力してください。")
    private Integer partySize;

    @NotBlank(message = "名前を入力してください。")
    @Size(max = 50, message = "名前は50文字以内で入力してください。")
    private String name;

    @NotBlank(message = "メールアドレスを入力してください。")
    @Size(max = 200, message = "メールアドレスは200文字以内で入力してください。")
    @Email(message = "メールアドレスの形式が正しくありません。")
    private String email;

    @NotBlank(message = "電話番号を入力してください。")
    @Size(min = 10, max = 13, message = "電話番号は10桁から13文字で入力してください。")
    @Pattern(regexp = "^(?:\\d{10,11}|0\\d{1,4}-\\d{1,4}-\\d{4})$", message = "電話番号は半角数字とハイフンで正しい形式で入力してください。")
    private String phone;

    @Size(max = 300, message = "備考は300文字以内で入力してください。")
    private String note;

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

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
