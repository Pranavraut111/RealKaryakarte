package com.mandal.model;

public class Mandal {
    private Long id;
    private String mandalName;
    private String inviteCode;

    public Mandal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMandalName() { return mandalName; }
    public void setMandalName(String mandalName) { this.mandalName = mandalName; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
