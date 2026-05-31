package com.p2p.presentation.cli;

import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.application.observer.BorrowerNotificationObserver;
import com.p2p.application.observer.LenderNotificationObserver;
import com.p2p.application.service.FundingService;
import com.p2p.application.service.LoanService;
import com.p2p.application.service.NotificationService;
import com.p2p.application.service.UserService;
import com.p2p.application.service.WithdrawalService;
import com.p2p.domain.valueobject.Money;
import com.p2p.infrastructure.memory.RepositoryFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AppContext {

    public static final String ADMIN_EMAIL    = "admin@p2p.com";
    public static final String ADMIN_PASSWORD = "admin123";

    private static AppContext instance;

    private final UserService userService;
    private final LoanService loanService;
    private final FundingService fundingService;
    private final WithdrawalService withdrawalService;
    private final NotificationService notificationService;
    private final RepositoryFactory repos;

    // Saldo platform (admin fee 1% dari setiap pencairan)
    private Money adminSaldo = new Money(BigDecimal.ZERO, "IDR");

    // Session
    private String currentUserId;
    private String currentRole;

    // Pemetaan UserId -> BorrowerId / LenderId
    private final Map<String, String> userToBorrowerId = new HashMap<>();
    private final Map<String, String> userToLenderId   = new HashMap<>();

    private AppContext() {
    repos = RepositoryFactory.getInstance();

    BorrowerNotificationObserver notifObserver = new BorrowerNotificationObserver();
    LoanEventPublisher publisher = new LoanEventPublisher();

    notificationService  = new NotificationService(repos.getLoanRepository(), notifObserver);

    LenderNotificationObserver lenderNotifObserver = new LenderNotificationObserver();
    notifObserver.setNotificationService(notificationService);
    lenderNotifObserver.setNotificationService(notificationService);
    publisher.registerObserver(notifObserver);
    publisher.registerObserver(lenderNotifObserver);


    userService          = new UserService(repos.getUserRepository(), repos.getBorrowerRepository(), repos.getLenderRepository());
    loanService          = new LoanService(repos.getLoanRepository(), repos.getBorrowerRepository(),
                                           repos.getLenderRepository(), publisher, notificationService);
    fundingService = new FundingService(repos.getLoanRepository(), repos.getLenderRepository(), publisher);
    withdrawalService    = new WithdrawalService(repos.getLenderRepository());

    // Daftarkan callback admin fee agar setiap pencairan otomatis menambah adminSaldo
    loanService.setAdminFeeCallback(this::tambahAdminSaldo);
}

    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    // Getters service
    public UserService getUserService() {
        return userService;
    }

    public LoanService getLoanService() {
        return loanService;
    }

    public FundingService getFundingService() {
        return fundingService;
    }

    public WithdrawalService getWithdrawalService() {
        return withdrawalService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public RepositoryFactory getRepos() {
        return repos;
    }

    // --- Admin Saldo ---
    public Money getAdminSaldo() {
        return adminSaldo;
    }

    public void tambahAdminSaldo(Money fee) {
        adminSaldo = new Money(adminSaldo.getAmount().add(fee.getAmount()), "IDR");
    }

    // --- Session ---
    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String id) {
        this.currentUserId = id;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String role) {
        this.currentRole = role;
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public void logout() {
        currentUserId = null;
        currentRole = null;
    }

    public void linkBorrower(String userId, String borrowerId) {
        userToBorrowerId.put(userId, borrowerId);
    }

    public void linkLender(String userId, String lenderId) {
        userToLenderId.put(userId, lenderId);
    }

    public String getBorrowerId(String userId) {
        return userToBorrowerId.get(userId);
    }

    public String getLenderId(String userId) {
        return userToLenderId.get(userId);
    }
}
