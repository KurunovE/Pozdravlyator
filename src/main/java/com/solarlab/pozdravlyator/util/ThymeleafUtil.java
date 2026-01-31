package com.solarlab.pozdravlyator.util;

import com.solarlab.pozdravlyator.model.Birthday;
import com.solarlab.pozdravlyator.service.BirthdayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThymeleafUtil {
    private static BirthdayService birthdayService;

    @Autowired
    public ThymeleafUtil(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    public static boolean isToday(Birthday birthday) {
        return birthdayService.isToday(birthday);
    }

    public static boolean isOverdue(Birthday birthday) {
        return birthdayService.isOverdue(birthday);
    }

    public static long getDaysUntil(Birthday birthday) {
        return birthdayService.getDaysUntil(birthday);
    }
}
