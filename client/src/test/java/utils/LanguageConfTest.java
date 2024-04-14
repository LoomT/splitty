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
                    highContrast=false""")));
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
        assertEquals(languageConf.get("flag"), "flags/flag_uk.png");
        languageConf.changeCurrentLocaleTo("nl");
        assertEquals(languageConf.get("flag"), "flags/flag_nl.png");
        languageConf.changeCurrentLocaleTo("de");
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
}
