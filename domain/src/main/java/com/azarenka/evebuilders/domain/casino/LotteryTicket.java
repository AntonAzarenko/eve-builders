package com.azarenka.evebuilders.domain.casino;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "casino_lottery_ticket", schema = "builders")
public class LotteryTicket {
    @Id
    @Column(length = 64)
    private String uid;
    @Column(name = "character_id", unique = true, nullable = false)
    private String characterId;
    private String reward;
    @Column(name = "createdate")
    private LocalDate createDate;
}
