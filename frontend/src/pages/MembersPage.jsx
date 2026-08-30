import { useState, useEffect } from "react";
import { getUsers, changeUserRole, getMandal } from "../api";
import { useAuth } from "../context/AuthContext";
import { useLang } from "../context/LangContext";
import { AppShell } from "@/components/AppShell";
import { Shield, User, ShieldAlert, UserPlus, Copy, Check } from "lucide-react";

export default function MembersPage() {
  const { user } = useAuth();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const [inviteCode, setInviteCode] = useState("");

  const isAdmin = user?.role === "ADMIN";
  const { t } = useLang();

  const fetchMembers = async () => {
    try {
      const res = await getUsers();
      // API wraps response in { data: [...] }
      setMembers(res.data || res || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMembers();
    // Fetch invite code
    getMandal().then(res => {
      setInviteCode(res.data?.inviteCode || "");
    }).catch(() => {});
  }, []);

  const handleRoleChange = async (memberId, newRole) => {
    if (!isAdmin) return;
    try {
      await changeUserRole(memberId, newRole);
      fetchMembers();
    } catch (err) {
      alert("Failed to change role: " + err.message);
    }
  };

  const handleCopyInvite = () => {
    const joinLink = `${window.location.origin}/join/${inviteCode}`;
    navigator.clipboard.writeText(joinLink).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const getRoleIcon = (role) => {
    switch (role) {
      case "ADMIN": return <ShieldAlert className="h-3 w-3" />;
      case "KARYAKARTA": return <Shield className="h-3 w-3" />;
      default: return <User className="h-3 w-3" />;
    }
  };

  /* What each role can do */
  const rolePermissions = {
    ADMIN: [t("manageRoles"), t("postDeleteNotices"), t("addEditVarganiExpenses"), t("exportReports"), t("fullAccess")],
    KARYAKARTA: [t("postNotices"), t("addEditVarganiExpenses"), t("viewReports")],
    MEMBER: [t("viewNotices"), t("viewVarganiExpenses")],
  };

  return (
    <AppShell title={t("membersAndRoles")} subtitle={`${members.length} ${t("members")}`}>
      {/* Invite link card */}
      {isAdmin && (
        <section className="surface-lift mb-5 rounded-2xl p-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="accent-gradient flex h-10 w-10 items-center justify-center rounded-xl text-primary-foreground">
                <UserPlus className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-semibold text-foreground">{t("inviteMembers")}</p>
                <p className="text-[11px] text-muted-foreground">{t("shareLink")}</p>
              </div>
            </div>
            <button
              onClick={handleCopyInvite}
              className="flex items-center gap-1.5 rounded-xl bg-secondary px-3 py-2 text-xs font-semibold text-foreground transition-colors hover:bg-secondary/80"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <Copy className="h-3.5 w-3.5" />}
              {copied ? t("copied") : t("copyLink")}
            </button>
          </div>
        </section>
      )}

      {/* Role permissions checklists */}
      <section className="surface-lift mb-5 rounded-2xl p-5">
        <p className="text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground mb-3">
          {t("rolePermissions")}
        </p>
        <div className="space-y-4">
          {Object.entries(rolePermissions).map(([role, perms]) => (
            <div key={role}>
              <div className="flex items-center gap-2 mb-1.5">
                {getRoleIcon(role)}
                <span className="text-sm font-semibold text-foreground">{role}</span>
              </div>
              <ul className="space-y-1 ml-5">
                {perms.map(p => (
                  <li key={p} className="flex items-center gap-2 text-xs text-muted-foreground">
                    <span className="text-emerald-500">✓</span> {p}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </section>

      {/* Members list */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        </div>
      ) : error ? (
        <div className="surface-lift rounded-2xl px-6 py-14 text-center">
          <p className="text-sm text-destructive">{error}</p>
        </div>
      ) : members.length === 0 ? (
        <div className="surface-lift rounded-2xl px-6 py-14 text-center">
          <p className="font-display text-lg font-semibold text-foreground">{t("noMembersYet")}</p>
          <p className="mt-1 text-sm text-muted-foreground">{t("shareInvite")}</p>
        </div>
      ) : (
        <ul className="space-y-3">
          {members.map(member => (
            <li key={member.id} className="surface-lift rounded-2xl p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="accent-gradient flex h-10 w-10 items-center justify-center rounded-xl text-sm font-semibold text-primary-foreground">
                    {(member.name || "?").charAt(0).toUpperCase()}
                  </span>
                  <div>
                    <p className="text-sm font-semibold text-foreground">{member.name}</p>
                    <p className="text-[11px] text-muted-foreground">{member.email || member.phone || "No contact"}</p>
                  </div>
                </div>

                <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider ${
                  member.role === "ADMIN" ? "bg-primary/12 text-primary" :
                  member.role === "KARYAKARTA" ? "bg-emerald-500/12 text-emerald-500" :
                  "bg-secondary text-muted-foreground"
                }`}>
                  {getRoleIcon(member.role)}
                  {member.role}
                </span>
              </div>

              {isAdmin && member.id !== user.id && (
                <div className="mt-3 flex gap-2 border-t border-border pt-3">
                  {member.role === "MEMBER" && (
                    <button 
                      onClick={() => handleRoleChange(member.id, "KARYAKARTA")}
                      className="flex-1 min-h-[36px] rounded-xl bg-emerald-500/10 text-xs font-semibold text-emerald-600 transition-colors hover:bg-emerald-500/20"
                    >
                      {t("promoteToKaryakarta")}
                    </button>
                  )}
                  {member.role === "KARYAKARTA" && (
                    <button 
                      onClick={() => handleRoleChange(member.id, "MEMBER")}
                      className="flex-1 min-h-[36px] rounded-xl bg-secondary text-xs font-semibold text-muted-foreground transition-colors hover:bg-secondary/80"
                    >
                      {t("demoteToMember")}
                    </button>
                  )}
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </AppShell>
  );
}
