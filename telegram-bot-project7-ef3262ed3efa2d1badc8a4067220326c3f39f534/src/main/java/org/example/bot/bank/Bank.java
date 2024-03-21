package org.example.bot.bank;

import lombok.Data;
import org.example.bot.CurrencyTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

public class Bank extends CurrencyTelegramBot {

    public void sendBankSettings(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select the bank");
        message.setReplyMarkup(createBankKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup createBankKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("NBU");
        row1.add("PrivatBank");
        row2.add("Monobank");
        row2.add("Back Setting Menu");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
}

    @Data
    class JsonMB {
        private int currencyCodeA;
        private int currencyCodeB;
        private int date;
        private float rateSell;
        private float rateBuy;
        private float rateCross;
    }

    @Data
    class JsonNBU {
        private int r030;
        private String txt;
        private float rate;
        private String cc;
        private String exchangeDate;
    }

    @Data
    class JsonPB {
        private String ccy;
        private String base_ccy;
        private float buy;
        private float sale;
    }



