package client.utils.currency;

import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import jakarta.inject.Inject;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Currency converter which can scan the currencies.txt file to
 * create a map of currencies and respective exchange rates and fetch
 * exchange rates from openExchangeRates API.
 * Uses singleton pattern.
 */
public class CurrencyConverter {

    private final FileManager fileManager;
    private final List<String> currencies;
    private final ServerUtils server;
    private final UserConfig userConfig;
    private final LanguageConf languageConf;

    /**
     * @param server ServerUtils for testing
     * @param fileManager file manager for managing historical rates
     * @param userConfig user config for preferred currency
     * @param languageConf language config for errors
     */
    @Inject
    public CurrencyConverter(ServerUtils server, FileManager fileManager,
                             UserConfig userConfig, LanguageConf languageConf) {
        this.fileManager = fileManager;
        this.currencies = fileManager.getAvailableCurrencies();
        this.server = server;
        this.userConfig = userConfig;
        this.languageConf = languageConf;
    }

    /**
     * @param currency currency code to convert from
     * @param amount amount in that currency
     * @param time date at which to convert
     * @return converted amount
     */
    public double convertToPreferred(String currency, double amount, Instant time)
            throws IOException {
        String date = DateTimeFormatter.ISO_DATE.format(time.atZone(ZoneOffset.UTC).toLocalDate());
        try {
            Map<String, Double> rates = fileManager.get(date);
            if(rates == null) {
                rates = server.getExchangeRates(date); // fetch rates from server
                fileManager.add(rates, date); // cache the rates
            }
            return amount / rates.get(currency) * rates.get(userConfig.getCurrency());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    languageConf.get("Currency.IOError"));
            alert.setHeaderText(languageConf.get("unexpectedError"));
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw e;
        }
    }

    /**
     * @return get a set of
     */
    public List<String> getCurrencies() {
        return currencies;
    }
}
