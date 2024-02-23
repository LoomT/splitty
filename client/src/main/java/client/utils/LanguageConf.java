package client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageConf {



    private static final List<Locale> availableLocales = List.of(Locale.of("en"), Locale.of("nl"));
    private static Locale currentLocale = Locale.of("en");
    private static ResourceBundle currentBundle = getCurrentResourceBundle();

    /**
     * @return the current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
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
        System.out.println("Language changed to " + newLocaleString);
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
}
