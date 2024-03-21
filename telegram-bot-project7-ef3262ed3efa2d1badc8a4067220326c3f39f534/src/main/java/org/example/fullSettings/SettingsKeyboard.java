package org.example.fullSettings;

import com.google.common.reflect.TypeToken;
import lombok.Data;
import org.example.bot.CurrencyTelegramBot;
import org.example.model.UserSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class SettingsKeyboard extends CurrencyTelegramBot {

    private Set<String> selectedSettings = new HashSet<>();
    private UserSettings.ChoiceBank selectedBank = UserSettings.ChoiceBank.NBU;
    private UserSettings.Currency selectedCurrency = UserSettings.Currency.USD;
    private Set<UserSettings.Currency> selectedCurrencies = new HashSet<>();


    private String fetchDataFromUrl(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }

    private String getPrivatBankExchangeRates(String data) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonPB>>() {
        }.getType();
        List<JsonPB> privatBankList = gson.fromJson(data, listType);

        StringBuilder sb = new StringBuilder();
        for (JsonPB pb : privatBankList) {
            sb.append("Currency: ").append(pb.getCcy()).append("\n");
            sb.append("Buy rate: ").append(pb.getBuy()).append("\n");
            sb.append("Sell rate: ").append(pb.getSale()).append("\n\n");
        }
        return sb.toString();
    }

    private String getNbuExchangeRates(String data) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String currencyCode = obj.getString("cc");
                if (currencyCode.equals("USD") || currencyCode.equals("EUR")) {
                    float rate = obj.getFloat("rate");
                    sb.append("Currency: ").append(currencyCode).append("\n");
                    sb.append("Buy rate: ").append(rate).append("\n");
                    sb.append("Sell rate: ").append(rate).append("\n\n");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public void sendSettingsMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Menu");
        message.setReplyMarkup(createSettingsMenuKeyboard(chatId));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendExchangeRates(long chatId) {
        try {
            String bankUrl = getBankUrl();
            String data = fetchDataFromUrl(bankUrl);
            String rates = getExchangeRates(data);
            rates = filterRatesBySelectedCurrency(rates);
            String limitedRates = limitExchangeRates(rates, 7);
            String bankName = getBankName();

            String messageText = "Exchange rates data:\n\nBank: " + bankName + "\n\n" + limitedRates;

            SendMessage message = new SendMessage();
            message.setText(messageText);
            message.setChatId(String.valueOf(chatId));

            execute(message);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getBankUrl() {
        return (selectedBank == UserSettings.ChoiceBank.PrivatBank) ?
                "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5" :
                "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    }

    private String getExchangeRates(String data) {
        return (selectedBank == UserSettings.ChoiceBank.PrivatBank) ?
                getPrivatBankExchangeRates(data) :
                getNbuExchangeRates(data);
    }

    private String filterRatesBySelectedCurrency(String rates) {
        if (selectedBank == UserSettings.ChoiceBank.NBU) {
            if (selectedCurrency == UserSettings.Currency.USD) {
                return filterExchangeRatesByCurrency(rates, UserSettings.Currency.USD);
            } else if (selectedCurrency == UserSettings.Currency.EUR) {
                return filterExchangeRatesByCurrency(rates, UserSettings.Currency.EUR);
            }
        } else if (selectedBank == UserSettings.ChoiceBank.PrivatBank) {
            if (selectedCurrency == UserSettings.Currency.USD) {
                return filterExchangeRatesByCurrency(rates, UserSettings.Currency.USD);
            } else if (selectedCurrency == UserSettings.Currency.EUR) {
                return filterExchangeRatesByCurrency(rates, UserSettings.Currency.EUR);
            }
        }
        return rates; // Return unchanged rates if conditions are not met
    }

    private String getBankName() {
        return (selectedBank == UserSettings.ChoiceBank.PrivatBank) ? "PrivatBank" : "NBU";
    }

    private String filterExchangeRatesByCurrency(String exchangeRates, UserSettings.Currency currency) {
        StringBuilder filteredRates = new StringBuilder();
        String[] lines = exchangeRates.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String currencyString = "Currency: " + currency.toString();
            if (line.startsWith(currencyString)) {
                filteredRates.append(line).append("\n");
                if (i + 1 < lines.length) {
                    filteredRates.append(lines[++i]).append("\n");
                }
                if (i + 1 < lines.length) {
                    filteredRates.append(lines[++i]).append("\n\n");
                }
            }
        }
        return filteredRates.toString();
    }

    private String limitExchangeRates(String exchangeRates, int limit) {
        String[] lines = exchangeRates.split("\n");
        StringBuilder limitedRates = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, limit); i++) {
            limitedRates.append(lines[i]).append("\n");
        }
        return limitedRates.toString();
    }

    private void addCurrency(UserSettings.Currency currency) {
        selectedCurrencies.add(currency);
    }

    private void removeCurrency(Currency currency) {
        selectedCurrencies.remove(currency);
    }

    private boolean isCurrencySelected(Currency currency) {
        return !selectedCurrencies.contains(currency);
    }

    private ReplyKeyboardMarkup createCurrencyKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("USD");
        row1.add("EUR");
        row1.add("back");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }


    private String getMonoBankExchangeRates(String monoBankData) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonMB>>() {
        }.getType();
        List<JsonMB> monoBankList = gson.fromJson(monoBankData, listType);

        StringBuilder sb = new StringBuilder();
        for (JsonMB mb : monoBankList) {
            sb.append("Buy rate: ").append(mb.getRateBuy()).append("\n");
            sb.append("Sell rate: ").append(mb.getRateSell()).append("\n\n");
        }
        return sb.toString();
    }


    private void sendNotificationTimeSettings(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select notification time");
        // Додайте необхідну клавіатуру для вибору часу нотифікації
        message.setReplyMarkup(createNotificationTimeKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSignAfterCommaSettings(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select the number of decimal places");
        message.setReplyMarkup(createSignAfterCommaKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /*
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
    */
    private void sendCurrencySettings(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select the currency");
        message.setReplyMarkup(createCurrencyKeyboard());
        message.setReplyMarkup(createSignAfterCommaKeyboard());
        message.setReplyMarkup(createBankKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // Метод для створення клавіатури з відміченими налаштуваннями
    private ReplyKeyboardMarkup createSettingsMenuKeyboard(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add("Number of decimal places");
        row2.add("Bank");
        row2.add("Currencies");
        row3.add("Notification time");
        row4.add("Back Setting Menu");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    /*
    public void handleSettingsUpdate(long chatId, String messageText) {
        switch (messageText) {
            case "Number of decimal places":
                sendSignAfterCommaSettings(chatId);
                break;
            case "Bank":

                sendBankSettings(chatId);
                break;
            case "Currencies":
                sendCurrencySettings(chatId);
                break;
            case "Notification time":
                sendNotificationTimeSettings(chatId);
                break;
            case "2":
            case "3":
            case "4":
                sendSettingsMenu(chatId); // Оновіть повідомлення про початок для відображення змін у налаштуваннях
                break;
            case "NBU":
                selectedBank = UserSettings.ChoiceBank.NBU;
                sendSettingsMenu(chatId);
                break;
            case "PrivatBank":
                selectedBank = UserSettings.ChoiceBank.PrivatBank;
                sendSettingsMenu(chatId);
                break;
            case "MonoBank":
                selectedBank = UserSettings.ChoiceBank.MonoBank;
                sendSettingsMenu(chatId);
                break;
            case "USD":
                selectedCurrency = UserSettings.Currency.USD;
                sendSettingsMenu(chatId);
                break;
            case "EUR":
                selectedCurrency = UserSettings.Currency.EUR;
                sendSettingsMenu(chatId);
                break;
            case "back":
                setupBeginButton();
                break;
            case "back Get Info":
                sendExchangeRates(chatId);
                sendSettingsMenu(chatId);

                break;
        }
    }*/
    private ReplyKeyboardMarkup createSignAfterCommaKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("2");
        row1.add("3");
        row1.add("4");
        row2.add("Back Setting Menu");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createBankKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add("NBU");
        row1.add("PrivatBank");
        row1.add("Bank.Monobank");
        row2.add("back");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createNotificationTimeKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Додавання кнопок для вибору часу нотифікації від 9 до 18 годин
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();

        row1.add("9");
        row1.add("10");
        row1.add("11");
        row2.add("12");
        row2.add("13");
        row2.add("14");
        row3.add("15");
        row3.add("16");
        row3.add("17");
        row4.add("18");
        row4.add("off");
        row4.add("back");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
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

