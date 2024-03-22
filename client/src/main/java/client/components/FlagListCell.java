package client.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Locale;
import java.util.ResourceBundle;

public class FlagListCell extends javafx.scene.control.ListCell<String> {
    private final ImageView imageView = new ImageView();

    /**
     *
     * @param language The language located at the cell
     * @param empty whether this cell represents data from the list. If it
     *        is empty, then it does not represent any data, but is a cell
     *        being used to render an "empty" row.
     */
    @Override
    protected void updateItem(String language, boolean empty) {
        super.updateItem(language, empty);
        if (empty || language == null) {
            setGraphic(null);
        } else {
            Image flagImage = new Image(ResourceBundle.getBundle
                            ("languages", Locale.of("flags"))
                            .getString("flag_" + language));
            imageView.setImage(flagImage);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            setGraphic(imageView);
        }
    }
}
