package com.solarlab.pozdravlyator.repository;

import com.solarlab.pozdravlyator.model.Birthday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BirthdayRepository extends JpaRepository<Birthday, Long> {

    @Query("SELECT b FROM Birthday b WHERE DAY(b.birthDate) = DAY(:date) AND MONTH(b.birthDate) = MONTH(:date)")
    List<Birthday> findByBirthday(LocalDate birthDate);

    @Query("SELECT b FROM Birthday b ORDER BY MONTH(b.birthDate), DAY(b.birthDate)")
    List<Birthday> findAllOrderedByDate();

    @Query("SELECT b FROM Birthday b ORDER BY b.name ASC")
    List<Birthday> findAllOrderedByName();

    @Query("SELECT b FROM Birthday b ORDER BY b.birthDate ASC")
    List<Birthday> findAllOrderedByBirthDate();
}
