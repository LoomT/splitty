package client.utils.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.scene.control.Alert;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FileManagerImpl implements FileManager{
    private final File rateDir;
    private final List<String> currencies;

    /**
     * Constructs a file manager for exchange rates
     */
    public FileManagerImpl() {
        ObjectReader reader = new ObjectMapper().reader().forType(List.class);
        InputStream currencyStream = FileManager.class.getClassLoader()
                .getResourceAsStream("rates/currencies.txt");
        if(currencyStream == null) {
            throw new RuntimeException("Resource not found: " +
                    "rates/currencies.txt");
        }
        try {
            currencies = reader.readValue(currencyStream);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Failed to read currencies.txt.\n" +
                            "If the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Failed to read currencies.txt");
        }

        try {
            AppDirs appDirs = AppDirsFactory.getInstance();
            String appData = appDirs.getUserDataDir("Splitty", null, null)
                    + File.separator + "rates";
            rateDir = new File(appData);
            rateDir.mkdirs();
            if(!rateDir.exists()) {
                System.out.println("Cannot write to user data directory");
                throw new RuntimeException("Cannot write to user data directory");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    e.getMessage() + ".\nIf the error persists, try reinstalling the app");
            alert.setHeaderText("Unexpected error");
            java.awt.Toolkit.getDefaultToolkit().beep();
            alert.showAndWait();
            throw new RuntimeException("Currency folders not found");
        }

    }

    /**
     * Saves the rates
     *
     * @param rates exchange rates
     * @param date date of them, yyyy-mm-dd format
     */
    @Override
    public void add(Map<String, Double> rates, String date) throws IOException {
        File file = new File(rateDir, date + ".txt");
        if (!file.createNewFile()) return;

        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        writer.writeValue(file, rates);
    }

    /**
     * @param date date of rates, yyyy-mm-dd format
     * @return rates, null if not found
     */
    @Override
    public Map<String, Double> get(String date) throws IOException {
        File file = new File(rateDir, date + ".txt");
        if(!file.exists() || !file.isFile()) return null;

        ObjectReader reader = new ObjectMapper().reader().forType(Map.class);
        return reader.readValue(file);
    }

    /**
     * @return list of available currencies from currencies.txt
     */
    @Override
    public List<String> getAvailableCurrencies() {
        return currencies;
    }
}
