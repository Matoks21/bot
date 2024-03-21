package org.example.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class UserSettings {
    private int decimalPlaces;
    private Currency currencies;
    private ChoiceBank banks;
    private int notificationTime;

    public void UserSetting() {
        // Ініціалізація полів за замовчуванням, якщо юзер ше не вибрав
       decimalPlaces = 2;
       banks = ChoiceBank.NBU;
       currencies = Currency.USD;

        notificationTime = 9;
    }
    public enum Currency {
        USD,
        EUR
    }

    public enum ChoiceBank {
        NBU,
        PrivatBank,
        MonoBank
    }

}