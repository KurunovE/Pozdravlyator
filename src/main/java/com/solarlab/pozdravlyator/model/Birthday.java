package com.solarlab.pozdravlyator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "birthdays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Birthday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is not be null")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Birthday is not be null")
    @Column(nullable = false)
    private LocalDate birthday;

    @Column(length = 500)
    private String photoPath;

}
