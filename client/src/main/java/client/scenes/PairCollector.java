package client.scenes;

import javafx.scene.Parent;
import javafx.util.Pair;

public record PairCollector(Pair<StartScreenCtrl, Parent> startScreen,
                            Pair<EventPageCtrl, Parent> eventPage,
                            Pair<AdminLoginCtrl, Parent> adminLogin,
                            Pair<EditParticipantsCtrl, Parent> editParticipantsPage,
                            Pair<AdminOverviewCtrl, Parent> adminOverview,
                            Pair<AddExpenseCtrl, Parent> addExpensePage,
                            Pair<EditTitleCtrl, Parent> editTitlePage,
                            Pair<AddTagCtrl, Parent> addTagPage,
                            Pair<StatisticsCtrl, Parent> statisticsPage ,
                            Pair<OptionsCtrl, Parent> options,
                            Pair<AddCustomTransactionCtrl, Parent> addCustomTransaction,
                            Pair<OpenDebtsPageCtrl, Parent> openDebtsPage,
                            Pair<InviteMailCtrl, Parent> inviteMailPage) {
}
