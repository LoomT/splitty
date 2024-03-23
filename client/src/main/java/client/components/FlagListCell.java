package client.components;

import client.utils.LanguageConf;
import jakarta.inject.Inject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FlagListCell extends javafx.scene.control.ListCell<String> {
    private final ImageView imageView = new ImageView();

    @Inject
    private LanguageConf languageConf;

    /**
     * @param languageConf to add the current language configuration to the class
     */
    public FlagListCell(LanguageConf languageConf){
        this.languageConf = languageConf;
    }

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
            Image flagImage = new Image(languageConf.get("flag", language));
            imageView.setImage(flagImage);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            setGraphic(imageView);
        }
    }
}
