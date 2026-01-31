package com.solarlab.pozdravlyator.controller;

import com.solarlab.pozdravlyator.model.Birthday;
import com.solarlab.pozdravlyator.service.BirthdayService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Controller
public class BirthdayController {

    private final BirthdayService birthdayService;
    private final Path uploadDir;

    @Autowired
    public BirthdayController(BirthdayService birthdayService,
                              @Value("${app.upload.dir}") String uploadDir) {
        this.birthdayService = birthdayService;
        this.uploadDir = Paths.get(uploadDir);
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "sort",
                                required = false,
                                defaultValue = "days") String sort,
                        Model model) {
        List<Birthday> todayAndUpcoming = birthdayService.getTodayAndUpcomingBirthdays();
        if ("name".equals(sort)) {
            todayAndUpcoming = todayAndUpcoming.stream()
                    .sorted((b1, b2) -> b1.getName()
                            .compareToIgnoreCase(b2.getName()))
                    .toList();
        } else if ("date".equals(sort)) {
            todayAndUpcoming = todayAndUpcoming.stream()
                    .sorted(Comparator.comparing(Birthday::getBirthDate))
                    .toList();
        }
        model.addAttribute("birthdays", todayAndUpcoming);
        model.addAttribute("isTodayView", true);
        model.addAttribute("birthdayService", birthdayService);
        model.addAttribute("currentSort", sort);
        return "index";
    }

    @GetMapping("/all")
    public String allBirthdays(@RequestParam(value = "sort",
                                       required = false,
                                       defaultValue = "date") String sort,
                               Model model) {
        List<Birthday> allBirthdays = birthdayService.getAllBirthdays(sort);
        model.addAttribute("birthdays", allBirthdays);
        model.addAttribute("isTodayView", false);
        model.addAttribute("birthdayService", birthdayService);
        model.addAttribute("currentSort", sort);
        return "all-birthdays";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("birthday", new Birthday());
        return "add-birthday";
    }

    @PostMapping("/add")
    public String addBirthday(@Valid @ModelAttribute Birthday birthday,
                              BindingResult result,
                              @RequestParam(value = "photo", required = false)
                              MultipartFile photoFile,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-birthday";
        }

        try {
            birthdayService.saveBirthday(birthday, photoFile);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Birthday successfully added!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error uploading photo:" + e.getMessage());
            return "add-birthday";
        }

        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Birthday birthday = birthdayService.getBirthDateById(id)
                .orElseThrow(() -> new RuntimeException("Birthday not found with id:" + id));
        model.addAttribute("birthday", birthday);
        return "edit-birthday";
    }

    @PostMapping("/edit/{id}")
    public String updateBirthday(@PathVariable Long id,
                                 @Valid @ModelAttribute Birthday birthday,
                                 BindingResult result,
                                 @RequestParam(value = "photo", required = false)
                                 MultipartFile photoFile,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "edit-birthday";
        }

        try {
            birthdayService.updateBirthday(id, birthday, photoFile);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Birthday successfully updated!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error uploading photo: " + e.getMessage());
            return "edit-birthday";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/all";
        }

        return "redirect:/all";
    }

    @PostMapping("/delete/{id}")
    public String deleteBirthday(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            birthdayService.deleteBirthday(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Birthday deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/all";
    }

    @GetMapping("/photos/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> servePhoto(@PathVariable String filename) {
        try {
            Path file = uploadDir.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg";
                String lowerFilename = filename.toLowerCase();
                if (lowerFilename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowerFilename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowerFilename.endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\""
                                        + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
