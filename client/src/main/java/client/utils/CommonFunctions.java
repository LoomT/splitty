package client.utils;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Objects;

public class CommonFunctions {
    /**
     * Adds a listener to the textField which will make the warningLabel visible
     * with a given message informing the user that the
     * length of the text reached maxLength
     *
     * @param textField event title text field
     * @param warningLabel error text node
     * @param maxLength max length of the field
     * @param warningMessage localized message with %d for showing max length
     */
    public static void lengthListener(TextField textField, Label warningLabel,
                                      int maxLength, String warningMessage) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            warningLabel.setVisible(false);
            if(newValue.length() > maxLength) {
                newValue = newValue.substring(0, maxLength);
            }
            if(newValue.length() == maxLength) {
                warningLabel.setText(
                        String.format(warningMessage, maxLength));
                warningLabel.setVisible(true);
            }
            textField.setText(newValue);
        });
    }

    /**
     * @param <T> type
     */
    public static class HideableItem<T> {
        private final ObjectProperty<T> object = new SimpleObjectProperty<>();
        private final BooleanProperty hidden = new SimpleBooleanProperty();

        /**
         * @param object object to put inside
         */
        public HideableItem(T object) {
            setObject(object);
        }

        private ObjectProperty<T> objectProperty(){return this.object;}
        private T getObject(){return this.objectProperty().get();}
        private void setObject(T object){this.objectProperty().set(object);}

        private BooleanProperty hiddenProperty(){return this.hidden;}
        private boolean isHidden(){return this.hiddenProperty().get();}
        private void setHidden(boolean hidden){this.hiddenProperty().set(hidden);}

        /**
         * @return string representation of object inside
         * or null if the object is null
         */
        @Override
        public String toString() {
            return getObject() == null ? null : getObject().toString();
        }

        /**
         * @param o object to compare against
         * @return true iff equal classes and contents
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HideableItem<?> that = (HideableItem<?>) o;
            return Objects.equals(object.get(), that.object.get());
        }

        /**
         * @return hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(object.get());
        }
    }

    /**
     * @param items to put
     * @param comboBox combo box to set up
     * @param <T> type of items
     */
    @SuppressWarnings("unchecked")
    public static <T> void comboBoxAutoCompletionSupport(
            List<T> items, ComboBox<HideableItem<T>> comboBox) {
        ObservableList<HideableItem<T>> hideableHideableItems =
                FXCollections.observableArrayList(hideableItem ->
                        new Observable[]{hideableItem.hiddenProperty()});
        items.forEach(item -> {
            HideableItem<T> hideableItem = new HideableItem<>(item);
            hideableHideableItems.add(hideableItem);
        });
        FilteredList<HideableItem<T>> filteredHideableItems =
                new FilteredList<>(hideableHideableItems, t -> !t.isHidden());
        comboBox.setItems(filteredHideableItems);
        HideableItem<T>[] selectedItem = (HideableItem<T>[]) new HideableItem[1];
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(!comboBox.isShowing()) return;

            comboBox.setEditable(true);
            comboBox.getEditor().clear();
        });
        comboBox.showingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                ListView<HideableItem<T>> lv = (ListView<HideableItem<T>>)
                        ((ComboBoxListViewSkin<?>) comboBox.getSkin()).getPopupContent();
                Platform.runLater(() -> {
                    if(selectedItem[0] == null) { // first use
                        double cellHeight = ((Control) lv.lookup(".list-cell")).getHeight();
                        lv.setFixedCellSize(cellHeight);
                    }
                });
                lv.scrollTo(comboBox.getValue());
            }
            else {
                HideableItem<T> value = comboBox.getValue();
                if(value != null) selectedItem[0] = value;
                comboBox.setEditable(false);
                Platform.runLater(() -> {
                    comboBox.getSelectionModel().select(selectedItem[0]);
                    comboBox.setValue(selectedItem[0]);
                });
            }
        });

        comboBox.setOnHidden(event -> hideableHideableItems.forEach(item -> item.setHidden(false)));

        filterComboBox(comboBox, hideableHideableItems);
    }

    /**
     * @param comboBox combo box to set up
     * @param hideableHideableItems hideable item observable list
     * @param <T> type of items
     */
    private static <T> void filterComboBox(ComboBox<HideableItem<T>> comboBox,
                                           ObservableList<HideableItem<T>> hideableHideableItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if(!comboBox.isShowing()) return;
            if(newValue.length() > 3) {
                comboBox.getEditor().setText(newValue.substring(0, 3));
                return;
            }
            final String filteredValue = newValue.replaceAll("[^a-zA-Z]", "");
            comboBox.getEditor().setText(filteredValue);

            Platform.runLater(() -> {
                if(comboBox.getSelectionModel().getSelectedItem() == null) {
                    hideableHideableItems.forEach(item ->
                            item.setHidden(!item.getObject().toString()
                                    .toLowerCase().contains(filteredValue.toLowerCase())));
                }
                else {
                    boolean validText = false;

                    for(HideableItem<T> hideableItem : hideableHideableItems) {
                        if(hideableItem.getObject().toString().equals(filteredValue)) {
                            validText = true;
                            break;
                        }
                    }

                    if(!validText) comboBox.getSelectionModel().select(null);
                }
            });
        });
    }

    /**
     * @return a blend for high contrast
     */
    public static Blend getHighContrastEffect() {
        ColorAdjust ca = new ColorAdjust();
        ca.setBrightness(-0.4);
        ca.setContrast(1);

        Blend b = new Blend();
        b.setMode(BlendMode.COLOR_BURN);
        b.setOpacity(.8);

        b.setTopInput(ca);
        return b;
    }
}