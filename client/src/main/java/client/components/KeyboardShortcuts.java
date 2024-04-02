package client.components;

import client.utils.Backable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.lang.reflect.InvocationTargetException;

public class KeyboardShortcuts {

    public static void checkEscape(Scene scene, Backable backable) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
                System.out.println("Key Pressed: " + ke.getCode());
                try {
                    backable.getClass().getMethod("backButtonClicked").invoke(backable);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                ke.consume(); // <-- stops passing the event to next node
            }
        });
    }
}
