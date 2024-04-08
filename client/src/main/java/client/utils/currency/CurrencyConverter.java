package client.utils.currency;

import client.utils.ServerUtils;
import client.utils.UserConfig;
import jakarta.inject.Inject;

import java.util.List;

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

    /**
     * @param server ServerUtils for testing
     * @param fileManager file manager for managing historical rates
     * @param userConfig user config for preferred currency
     */
    @Inject
    public CurrencyConverter(ServerUtils server, FileManager fileManager, UserConfig userConfig) {
        this.fileManager = fileManager;
        this.currencies = fileManager.getAvailableCurrencies();
        this.server = server;
        this.userConfig = userConfig;
    }

    /**
     * @return get a set of
     */
    public List<String> getCurrencies() {
        return currencies;
    }
}
