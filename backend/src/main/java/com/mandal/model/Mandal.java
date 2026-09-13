package com.mandal.model;

import java.math.BigDecimal;

public class Mandal {
    private Long id;
    private String mandalName;
    private String inviteCode;
    private BigDecimal previousBalance;

    public Mandal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMandalName() { return mandalName; }
    public void setMandalName(String mandalName) { this.mandalName = mandalName; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public BigDecimal getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(BigDecimal previousBalance) { this.previousBalance = previousBalance; }
}
