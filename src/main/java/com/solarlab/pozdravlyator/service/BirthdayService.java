package com.solarlab.pozdravlyator.service;

import com.solarlab.pozdravlyator.model.Birthday;
import com.solarlab.pozdravlyator.repository.BirthdayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BirthdayService {

    private final BirthdayRepository birthdayRepository;
    private final Path uploadDir;

    @Autowired
    public BirthdayService(BirthdayRepository birthdayRepository,
                           @Value("${app.upload.dir}") String uploadDir) {
        this.birthdayRepository = birthdayRepository;
        this.uploadDir = Paths.get(uploadDir);
        try {
            if (!Files.exists(this.uploadDir)) {
                Files.createDirectories(this.uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create upload directory", e);
        }
    }

    public List<Birthday> getAllBirthdays() {
        return birthdayRepository.findAll();
    }

    public List<Birthday> getAllBirthdays(String sortBy) {
        if (sortBy == null || sortBy.isEmpty() || "date".equals(sortBy)) {
            return birthdayRepository.findAllOrderedByDate();
        } else if ("name".equals(sortBy)) {
            return birthdayRepository.findAllOrderedByName();
        } else if ("birthDate".equals(sortBy)) {
            return birthdayRepository.findAllOrderedByBirthDate();
        }
        return birthdayRepository.findAllOrderedByDate();
    }

    public List<Birthday> getTodayAndUpcomingBirthdays() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(30);

        List<Birthday> allBirthdays = birthdayRepository.findAllOrderedByDate();

        return allBirthdays.stream()
                .filter(birthday -> {
                    LocalDate thisYearBirthday = birthday.getBirthDate()
                            .withYear(today.getYear());

                    if (isToday(birthday)) {
                        return true;
                    }

                    if (thisYearBirthday.isAfter(today)
                            && !thisYearBirthday.isAfter(endDate)) {
                        return true;
                    }

                    if (thisYearBirthday.isBefore(today)
                            || thisYearBirthday.isEqual(today)) {
                        LocalDate nextYearBirthday = thisYearBirthday.plusYears(1);
                        return nextYearBirthday.isAfter(today)
                                && !nextYearBirthday.isAfter(endDate);
                    }

                    return false;
                })
                .toList();
    }

    public List<Birthday> getTodayBirthdays() {
        return birthdayRepository.findByBirthday(LocalDate.now());
    }

    public Optional<Birthday> getBirthDateById(Long id) {
        return birthdayRepository.findById(id);
    }

    public Birthday saveBirthday(Birthday birthday,
                                 MultipartFile photoFile) throws IOException {
        if (photoFile != null && !photoFile.isEmpty()) {
            String photoPath = savePhoto(photoFile);
            birthday.setPhotoPath(photoPath);
        }
        return birthdayRepository.save(birthday);
    }

    public Birthday updateBirthday(Long id, Birthday birthdayDetails,
                                   MultipartFile photoFile) throws IOException {
        Birthday birthday = birthdayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("День рождения не найден с id: " + id));

        birthday.setName(birthdayDetails.getName());
        birthday.setBirthDate(birthdayDetails.getBirthDate());

        if (photoFile != null && !photoFile.isEmpty()) {
            if (birthday.getPhotoPath() != null) {
                deletePhoto(birthday.getPhotoPath());
            }
            String photoPath = savePhoto(photoFile);
            birthday.setPhotoPath(photoPath);
        }

        return birthdayRepository.save(birthday);
    }

    public void deleteBirthday(Long id) {
        Birthday birthday = birthdayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("День рождения не найден с id: " + id));

        if (birthday.getPhotoPath() != null) {
            deletePhoto(birthday.getPhotoPath());
        }

        birthdayRepository.delete(birthday);
    }

    private String savePhoto(MultipartFile photoFile) throws IOException {
        String originalFilename = photoFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID() + extension;
        Path filePath = uploadDir.resolve(filename);
        Files.copy(photoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private void deletePhoto(String photoPath) {
        try {
            Path filePath = uploadDir.resolve(photoPath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при удалении файла: " + photoPath);
        }
    }

    public boolean isToday(Birthday birthday) {
        LocalDate today = LocalDate.now();
        return birthday.getBirthDate().getMonth() == today.getMonth() &&
                birthday.getBirthDate().getDayOfMonth() == today.getDayOfMonth();
    }

    public boolean isOverdue(Birthday birthday) {
        LocalDate today = LocalDate.now();
        LocalDate thisYearBirthday = birthday.getBirthDate().withYear(today.getYear());

        return thisYearBirthday.isBefore(today) && !isToday(birthday);
    }

    public long getDaysUntil(Birthday birthday) {
        LocalDate today = LocalDate.now();
        LocalDate thisYearBirthday = birthday.getBirthDate().withYear(today.getYear());

        if (thisYearBirthday.isBefore(today) || thisYearBirthday.isEqual(today)) {
            thisYearBirthday = thisYearBirthday.plusYears(1);
        }

        return ChronoUnit.DAYS.between(today, thisYearBirthday);
    }
}
