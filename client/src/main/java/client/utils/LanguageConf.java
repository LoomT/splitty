package client.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class LanguageConf {



    private static final List<Locale> availableLocales = List.of(Locale.of("en"), Locale.of("nl"));

    private static ConfigParser configParser;

    static {
        try {
            configParser = ConfigParser.createInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Locale currentLocale = Locale.of(configParser.getLocale());

    private static Runnable callback = null;
    private static ResourceBundle currentBundle = getCurrentResourceBundle();


    /**
     * Use this function when you need to display language dependent text
     * that is not in the fxml file. (Such as the title)
     * @param key The key of the property in the language properties file
     * @return the value of the key in the currently set language
     */
    public static String get(String key) {
        return currentBundle.getString(key);
    }

    /**
     * @return the current locale in string format
     */
    public static String getCurrentLocaleString() {
        return currentLocale.getLanguage();
    }

    /**
     * @param newLocaleString the locale string to change the new language to
     */
    public static void changeCurrentLocaleTo(String newLocaleString) {
        Locale newLocale = Locale.of(newLocaleString);
        if (!availableLocales.contains(newLocale)) {
            throw new RuntimeException("The provided locale " + newLocaleString +
                    " is not part of the available locales");
        }

        currentLocale = newLocale;
        currentBundle = getCurrentResourceBundle();
        callback.run();
        System.out.println("Language changed to " + newLocaleString);
        try {
            configParser.setLocale(newLocale.toString());
        } catch (IOException ignored){}
    }

    /**
     * @return the resourcebundle for the selected locale
     */
    private static ResourceBundle getCurrentResourceBundle() {
        return ResourceBundle.getBundle("languages", currentLocale);
    }

    /**
     * @return the current resource bundle
     */
    public static ResourceBundle getLanguageResources() {
        return currentBundle;
    }

    /**
     * @return the available locales list converted to a list of strings
     */
    public static List<String> getAvailableLocalesString() {
        List<String> localesString = new ArrayList<>();
        for (Locale l : availableLocales) {
            localesString.add(l.getLanguage());
        }
        return localesString;
    }

    /**
     * @param function sets the callback for the language change
     */
    public static void onLanguageChange(Runnable function) {
        callback = function;
    }
}
