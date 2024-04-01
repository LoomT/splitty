package client.scenes;

import client.components.ErrorPopupCtrl;
import javafx.scene.Parent;
import javafx.util.Pair;

public record PairCollector(Pair<StartScreenCtrl, Parent> startScreen,
                            Pair<EventPageCtrl, Parent> eventPage,
                            Pair<AdminLoginCtrl, Parent> adminLogin,
                            Pair<EditParticipantsCtrl, Parent> editParticipantsPage,
                            Pair<AdminOverviewCtrl, Parent> adminOverview,
                            Pair<AddExpenseCtrl, Parent> addExpensePage,
                            Pair<ErrorPopupCtrl, Parent> errorPopup,
                            Pair<EditTitleCtrl, Parent> editTitlePage) {
}
