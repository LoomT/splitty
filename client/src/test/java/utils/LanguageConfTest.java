package utils;

import client.utils.LanguageConf;
import client.utils.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageConfTest {
    private LanguageConf languageConf;

    @BeforeEach
    public void setUp() {
        Runnable function = () -> {//do nothing
        };
        try {
            languageConf = new LanguageConf(new UserConfig(new TestIO("""
                    serverURL=localhost:8080
                    lang=en
                    recentEventCodes=
                    initialExportDirectory=
                    currency=EUR
                    highContrast=false
                    locales=["en", "nl", "de"]""")));
            languageConf.onLanguageChange(function);
        } catch (Exception e){
            fail();
        }
    }

    @Test
    void simpleLanguageTest(){
        //test for all standard languages
        assert languageConf != null;
        assertTrue(languageConf.getAvailableLocalesString().containsAll(List.of("en", "nl", "de")));

        languageConf.changeCurrentLocaleTo("en");
        assertEquals(languageConf.getCurrentLocaleString(), "en");
        assertEquals(languageConf.get("flag"), "flags/flag_uk.png");

        languageConf.changeCurrentLocaleTo("nl");
        assertEquals(languageConf.getCurrentLocaleString(), "nl");
        assertEquals(languageConf.get("flag"), "flags/flag_nl.png");

        languageConf.changeCurrentLocaleTo("de");
        assertEquals(languageConf.getCurrentLocaleString(), "de");
        assertEquals(languageConf.get("flag"), "flags/flag_de.png");
    }

    @Test
    void runtimeErrorTest(){
        assert languageConf != null;
        assertFalse(languageConf.getAvailableLocalesString().contains("nonExistingLang"));
        try{
            languageConf.changeCurrentLocaleTo("nonExistingLang");
        } catch (Exception e){
            return;
        }
        fail();
    }

    @Test
    void getKeyDiffLangTest(){
        assert languageConf != null;
        //test for usage language switcher
        languageConf.changeCurrentLocaleTo("en");
        assertEquals(languageConf.get("flag", "nl"), "flags/flag_nl.png");
        assertEquals(languageConf.get("flag", "de"), "flags/flag_de.png");
        assertEquals(languageConf.get("flag", "en"), "flags/flag_uk.png");
    }
}
