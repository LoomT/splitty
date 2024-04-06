package client.scenes;

import javafx.scene.Parent;
import javafx.util.Pair;

public record PairCollector(Pair<StartScreenCtrl, Parent> startScreen,
                            Pair<EventPageCtrl, Parent> eventPage,
                            Pair<AdminLoginCtrl, Parent> adminLogin,
                            Pair<EditParticipantsCtrl, Parent> editParticipantsPage,
                            Pair<AdminOverviewCtrl, Parent> adminOverview,
                            Pair<AddExpenseCtrl, Parent> addExpensePage,
                            Pair<EditTitleCtrl, Parent> editTitlePage) {
}
