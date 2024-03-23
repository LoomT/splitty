package client.utils;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class LanguageConf {
    private final List<Locale> availableLocales = List.of(Locale.of("en"), Locale.of("nl"));

    private final UserConfig userConfig;

    private Locale currentLocale;
    private Runnable callback = null;
    private ResourceBundle currentBundle;

    /**
     * Language config constructor where user config
     * is injected and currently configured language is set
     *
     * @param userConfig user config
     */
    @Inject
    public LanguageConf(UserConfig userConfig) {
        this.userConfig = userConfig;
        currentLocale = Locale.of(userConfig.getLocale());
        currentBundle = getCurrentResourceBundle();
    }

    /**
     * Use this function when you need to display language dependent text
     * that is not in the fxml file. (Such as the title)
     * @param key The key of the property in the language properties file
     * @return the value of the key in the currently set language
     */
    public String get(String key) {
        return currentBundle.getString(key);
    }

    /**
     * @param key the key of the property in the language properties file
     * @param lang language code to find the key value for
     * @return String value of requested property
     */
    public String get(String key, String lang){
        return ResourceBundle.getBundle("languages", Locale.of(lang)).getString(key);
    }

    /**
     * @return the current locale in string format
     */
    public String getCurrentLocaleString() {
        return currentLocale.getLanguage();
    }

    /**
     * @param newLocaleString the locale string to change the new language to
     */
    public void changeCurrentLocaleTo(String newLocaleString) {
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
            userConfig.setLocale(newLocale.toString());
        } catch (IOException e){
            // show a pop-up here maybe or just ignore
        }
    }

    /**
     * @return the resourcebundle for the selected locale
     */
    private ResourceBundle getCurrentResourceBundle() {
        return ResourceBundle.getBundle("languages", currentLocale);
    }

    /**
     * @return the current resource bundle
     */
    public ResourceBundle getLanguageResources() {
        return currentBundle;
    }

    /**
     * @return the available locales list converted to a list of strings
     */
    public List<String> getAvailableLocalesString() {
        List<String> localesString = new ArrayList<>();
        for (Locale l : availableLocales) {
            localesString.add(l.getLanguage());
        }
        return localesString;
    }

    /**
     * @param function sets the callback for the language change
     */
    public void onLanguageChange(Runnable function) {
        callback = function;
    }
}
