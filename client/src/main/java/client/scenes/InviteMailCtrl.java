package client.scenes;

import client.MockClass.MainCtrlInterface;
import client.utils.EmailService;
import client.utils.LanguageConf;
import client.utils.ServerUtils;
import client.utils.UserConfig;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.ConnectException;

public class InviteMailCtrl {

    private Event event;
    private Stage stage;
    private final MainCtrlInterface mainCtrl;
    private final LanguageConf languageConf;
    private final UserConfig userConfig;
    private final EmailService emailService;
    private final ServerUtils server;
    private FadeTransition ft;

    @FXML
    private TextField emailField;
    @FXML
    private ProgressIndicator loadIndicator;
    @FXML
    private Label confirmationLabel;

    /**
     * @param mainCtrl     main control
     * @param languageConf language configuration
     * @param userConfig   user configuration
     * @param emailService email service
     * @param server       server
     */
    @Inject
    public InviteMailCtrl(MainCtrl mainCtrl, LanguageConf languageConf,
                          UserConfig userConfig, EmailService emailService, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.languageConf = languageConf;
        this.userConfig = userConfig;
        this.emailService = emailService;
        this.server = server;
    }

    /**
     * Initializes the InviteMail popup
     */
    public void initialize() {
        ft = new FadeTransition(Duration.millis(2000), confirmationLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(1000));
        ft.setOnFinished(e -> confirmationLabel.setVisible(false));
        loadIndicator.setVisible(false);
        confirmationLabel.setVisible(false);
    }

    /**
     * Initializes the InviteMail scene to be shown
     *
     * @param event event to be invited to
     * @param stage stage to set the popup
     */
    public void showInviteMail(Event event, Stage stage) {
        this.event = event;
        this.stage = stage;
    }

    /**
     * sends invite to the email that was inputted
     */
    public void sendInvite() {
        loadIndicator.setVisible(true);
        confirmationLabel.setVisible(false);

        if (emailField.getLength() == 0) {
            confirmationLabel.setText(languageConf.get("Options.emptyInput"));
            confirmationLabel.setVisible(true);
            ft.stop();
            ft.play();
            loadIndicator.setVisible(false);
            return;
        }

        String subject = languageConf.get("EmailService.inviteHeader");
        String body = languageConf.get("EmailService.inviteBody");
        body = String.format(body, event.getTitle(), event.getId(), userConfig.getUrl());
        boolean result = emailService.sendEmail(emailField.getText(), subject, body);

        if (result) {
            confirmationLabel.setText(languageConf.get("InviteMail.emailSuccessful"));
            for (Participant p : event.getParticipants()) {
                if (p.getName().equals(emailField.getText())
                        || emailField.getText().equals(p.getEmailAddress())) return;
            }
            try {
                server.createParticipant(event.getId(), new Participant(emailField.getText(),
                        emailField.getText()));
            } catch (ConnectException e) {
                throw new RuntimeException(e); //TODO
            }
        } else {
            confirmationLabel.setText(languageConf.get("InviteMail.emailFailed"));
        }

        ft.stop();
        loadIndicator.setVisible(false);
        confirmationLabel.setVisible(true);
        confirmationLabel.setOpacity(1.0);
        ft.play();
    }
}
