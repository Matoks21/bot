package org.example.bot;


import org.example.bot.bank.Bank;
import org.example.fullSettings.SettingsKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static org.example.utils.ConstantData.*;

public class CurrencyTelegramBot  extends TelegramLongPollingBot  {
    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = new SendMessage();
        String chatId = update.getMessage().getChatId().toString();
        message.setChatId(chatId);
        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(BOT_COMMAND_START)) {
            message.setText(BOT_COMMAND_GREETING);
            message.setReplyMarkup(setupBeginButton());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        fullSettings(update, message, chatId);

    }

    private void fullSettings(Update update, SendMessage message, String chatId) {
        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(GET_INFO)) {
            message.setText("Menu");
            new SettingsKeyboard().sendExchangeRates(Long.parseLong(chatId)); // Відправляємо повідомлення про курси
        }
        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(SETTNGS)) {
            message.setText("Menu");
            new SettingsKeyboard().sendSettingsMenu(Long.parseLong(chatId)); // Відправляємо повідомлення про налаштування
        }

        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(DECIMAL_PLACES)) {
            message.setText("Choise opthion ->");
            new SettingsKeyboard().sendSignAfterCommaSettings(Long.parseLong(chatId));}

        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(DECIMAL_PLACES_2)) {
            message.setText("Choise opthion ->");
            new SettingsKeyboard().sendSignAfterCommaSettings(Long.parseLong(chatId));
            new SettingsKeyboard().sendExchangeRates(Long.parseLong(chatId)); // Оновіть повідомлення про початок для відображення змін у налаштуваннях
                }

        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(DECIMAL_PLACES_3)) {
            message.setText("Choise opthion ->");
            new SettingsKeyboard().sendSignAfterCommaSettings(Long.parseLong(chatId));
            new SettingsKeyboard().sendExchangeRates(Long.parseLong(chatId));// Оновіть повідомлення про початок для відображення змін у налаштуваннях
        }

        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(DECIMAL_PLACES_4)) {
            message.setText("Choise opthion ->");
            new SettingsKeyboard().sendSignAfterCommaSettings(Long.parseLong(chatId));
            new SettingsKeyboard().sendExchangeRates(Long.parseLong(chatId)); // Оновіть повідомлення про початок для відображення змін у налаштуваннях
        }
        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(BACK_SETTING_MENU)) {
            message.setText("Choise opthion ->");
            new SettingsKeyboard().sendSettingsMenu(Long.parseLong(chatId));
        // Оновіть повідомлення про початок для відображення змін у налаштуваннях
        }
        if (isMessagePresent(update) && update.getMessage().getText().equalsIgnoreCase(BANK)) {
            message.setText("Choise opthion ->");
            new Bank().sendBankSettings(Long.parseLong(chatId));
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    private static boolean isMessagePresent(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    public ReplyKeyboard setupBeginButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(GET_INFO);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(SETTNGS);

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}