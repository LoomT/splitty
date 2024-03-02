package client;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyConverter {

    private static CurrencyConverter currencyParser;
    private static Map<String, Double> currencyMap;
    private URI apiURI;
    private String base;
    private double conversionRate;


    /**
     * @param apiURI custom uri for dependency injection
     *                             TODO add: double conversionRate
     */
    private CurrencyConverter(URI apiURI, String base, double conversionRate) {
        this.apiURI = apiURI;
        this.base = base;
        this.conversionRate = conversionRate;
    }

    public static CurrencyConverter createInstance() {
        try {
            return createInstanceInjection(new URI("https://openexchangerates.org/api/" +
                    "latest.json?app_id=4368d26633d149e0b992c5bcdce76270"), "EUR");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public static CurrencyConverter createInstanceInjection(URI uri, String base) {
        if (currencyParser == null) {
            currencyMap = readConfig();
            double conversionRate = currencyMap.get(base);
            currencyParser = new CurrencyConverter(uri, base, conversionRate);
        }
        return currencyParser;
    }

    public static Map<String, Double> readConfig() {
        File file = new File(Objects.requireNonNull(ConfigParser.class.getClassLoader()
                .getResource("client/migration.properties")).getPath());

        try (FileReader fileReader = new FileReader(file)) {
            return getCurrencyMap(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Double> getCurrencyMap(FileReader fileReader) {
        BufferedReader reader = new BufferedReader(fileReader);
        List<String> temp = reader.lines().toList();
        Map<String, Double> result = new HashMap<>();
        for (int i = 2; i < temp.size() - 1; i++) {
            String[] tempArr = temp.get(i).split("=");
            result.put(tempArr[0], Double.parseDouble(tempArr[1]));
        }
        return result;
    }

    /**
     * constructor with a default uri value.
     */
    public CurrencyConverter() {
        try {
            this.apiURI = new URI("https://openexchangerates.org/api/latest.json?" +
                    "app_id=4368d26633d149e0b992c5bcdce76270");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    public String getExchange() throws URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiURI)
                .GET()
                .build();

        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body().toString();
    }

    public void updateExchange() throws URISyntaxException, IOException, InterruptedException {
        String path = Objects.requireNonNull(CurrencyConverter.class.getClassLoader()
                .getResource("client/migration.properties")).getPath();
        String next = "";
        BufferedReader reader = new BufferedReader(new StringReader(getExchange()));
        while (!next.contains("base")) {
            next = reader.readLine();
        }
        reader.readLine();
        next = reader.readLine();

        Properties prop = new Properties();
        OutputStream outputstream = new FileOutputStream(path);

        while (next.contains(":")) {
            String[] tempArr = next.split(": ");
            prop.setProperty(tempArr[0].replaceAll("[, " + (char) 34 + "]", ""),
                    tempArr[1].replaceAll("[, ]", ""));
            next = reader.readLine();
        }
        prop.setProperty("base", "EUR");

        prop.store(outputstream, "Test");
        outputstream.close();
    }

    public boolean setBase(String base) {
        if (base == null || !currencyMap.containsKey(base)) {
            return false;
        }
        this.base = base;
        this.conversionRate = currencyMap.get(base) / currencyMap.get("EUR");
        return true;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        new CurrencyConverter().updateExchange();
    }
}
