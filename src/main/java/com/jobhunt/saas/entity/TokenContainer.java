package com.jobhunt.saas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Table(name = "token_container")
public class TokenContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    private LocalDateTime exptime;

    private String token;

    private boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

}
