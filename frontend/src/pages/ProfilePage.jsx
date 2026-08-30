import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronRight, LogOut, Shield, Check } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";
import { downloadReport, updateProfile, getMandal, renameMandal } from "@/api";

export default function ProfilePage() {
  const { user, login, logout, setMandalName } = useAuth();
  const { lang, switchLang, t } = useLang();
  const navigate = useNavigate();

  const userName = user?.name || "Member";
  const userPhone = user?.phone || "";
  const userEmail = user?.email || "";
  const userRole = user?.role || "MEMBER";
  
  const [mandal, setMandal] = useState(null);
  const [newMandalName, setNewMandalName] = useState("");
  const [isRenaming, setIsRenaming] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (userRole === "ADMIN") {
      getMandal().then(data => {
        if (data.data) {
          setMandal(data.data);
          setNewMandalName(data.data.mandalName || "");
        }
      }).catch(err => console.error("Failed to fetch mandal", err));
    }
  }, [userRole]);
  const maskedPhone = userPhone.length >= 10
    ? "+91 " + userPhone.slice(0, 2) + "•••• ••" + userPhone.slice(-2)
    : userPhone;

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const handleLangChange = async (newLang) => {
    switchLang(newLang);
    try {
      await updateProfile(user.id, { languagePref: newLang });
      login(localStorage.getItem("mandal-token"), { ...user, languagePref: newLang });
    } catch (err) {
      console.error("Failed to save language pref", err);
    }
  };

  const handleExport = async () => {
    try {
      const from = "2026-01-01";
      const to = "2026-12-31";
      await downloadReport(from, to);
    } catch (err) {
      alert("Export failed: " + err.message);
    }
  };

  const handleRenameMandal = async () => {
    if (!newMandalName.trim()) return;
    try {
      const res = await renameMandal(newMandalName);
      setMandal(res.data);
      if (setMandalName) setMandalName(res.data.mandalName); // Update global context
      setIsRenaming(false);
    } catch (err) {
      alert("Failed to rename mandal: " + err.message);
    }
  };

  return (
    <AppShell title={t("profile")} subtitle={`${userName} · ${userRole}`} mandalName={mandal?.mandalName}>
      <section className="ink-panel relative overflow-hidden rounded-3xl p-6">
        <div className="pointer-events-none absolute -left-12 -bottom-16 h-48 w-48 rounded-full bg-[var(--color-primary)] opacity-25 blur-3xl" />
        <div className="relative flex items-center gap-4">
          <span className="accent-gradient flex h-16 w-16 items-center justify-center rounded-2xl text-2xl font-semibold text-primary-foreground">
            {userName.charAt(0).toUpperCase()}
          </span>
          <div>
            <p className="font-display text-xl font-semibold text-ink-foreground">
              {userName}
            </p>
            <p className="text-sm text-ink-foreground/60">{userEmail || maskedPhone}</p>
            <span className="mt-2 inline-flex items-center gap-1 rounded-full bg-ink-foreground/10 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider text-ink-foreground/80">
              <Shield className="h-3 w-3" /> {userRole}
            </span>
          </div>
        </div>
      </section>

      <section className="surface-lift mt-4 rounded-2xl p-4">
        <p className="text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground">
          {t("language")}
        </p>
        <div className="mt-3 grid grid-cols-2 gap-2">
          {[
            { id: "en", label: t("english") },
            { id: "mr", label: t("marathi") },
          ].map((l) => (
            <button
              key={l.id}
              onClick={() => handleLangChange(l.id)}
              className={`min-h-[46px] rounded-xl text-sm font-medium transition-colors ${
                lang === l.id
                  ? "accent-gradient text-primary-foreground"
                  : "border border-border bg-card text-foreground"
              }`}
            >
              {l.label}
            </button>
          ))}
        </div>
      </section>

      <section className="surface-lift mt-4 divide-y divide-border overflow-hidden rounded-2xl">
        <button
          onClick={() => navigate("/members")}
          className="flex min-h-[52px] w-full items-center justify-between px-4 py-3.5 text-left text-sm font-medium text-foreground transition-colors hover:bg-accent/60"
        >
          {t("membersAndRoles")}
          <ChevronRight className="h-4 w-4 text-muted-foreground" />
        </button>

        <button
          onClick={handleExport}
          className="flex min-h-[52px] w-full items-center justify-between px-4 py-3.5 text-left text-sm font-medium text-foreground transition-colors hover:bg-accent/60"
        >
          {t("reportsAndExport")}
          <ChevronRight className="h-4 w-4 text-muted-foreground" />
        </button>
      </section>

      {userRole === "ADMIN" && mandal && (
        <section className="surface-lift mt-4 rounded-2xl p-4 space-y-4">
          <p className="text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground">
            Mandal Settings
          </p>
          <div className="space-y-3">
            <div>
              <label className="text-sm font-medium text-foreground">Mandal Name</label>
              {isRenaming ? (
                <div className="flex gap-2 mt-1">
                  <input
                    type="text"
                    value={newMandalName}
                    onChange={(e) => setNewMandalName(e.target.value)}
                    className="w-full rounded-xl border border-input bg-background/50 px-3 py-2 text-sm text-foreground outline-none focus:border-primary"
                  />
                  <button
                    onClick={handleRenameMandal}
                    className="rounded-xl bg-primary px-3 py-2 text-sm font-medium text-primary-foreground"
                  >
                    Save
                  </button>
                  <button
                    onClick={() => { setIsRenaming(false); setNewMandalName(mandal.mandalName); }}
                    className="rounded-xl border border-input px-3 py-2 text-sm font-medium text-foreground"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <div className="flex justify-between items-center mt-1">
                  <span className="text-sm text-foreground">{mandal.mandalName}</span>
                  <button
                    onClick={() => setIsRenaming(true)}
                    className="text-xs font-medium text-primary"
                  >
                    Rename
                  </button>
                </div>
              )}
            </div>
            
            {mandal.inviteCode && (
              <div>
                <label className="text-sm font-medium text-foreground">Invite Code</label>
                <div className="mt-1 flex items-center justify-between rounded-xl border border-input bg-background/50 px-3 py-2">
                  <span className="font-mono text-sm tracking-widest text-primary">{mandal.inviteCode}</span>
                  <button
                    onClick={() => {
                      const code = mandal.inviteCode;
                      if (navigator.clipboard && navigator.clipboard.writeText) {
                        navigator.clipboard.writeText(code).then(() => { setCopied(true); setTimeout(() => setCopied(false), 2000); });
                      } else {
                        const ta = document.createElement('textarea');
                        ta.value = code;
                        ta.style.position = 'fixed';
                        ta.style.opacity = '0';
                        document.body.appendChild(ta);
                        ta.select();
                        document.execCommand('copy');
                        document.body.removeChild(ta);
                        setCopied(true);
                        setTimeout(() => setCopied(false), 2000);
                      }
                    }}
                    className="text-xs font-medium text-muted-foreground hover:text-foreground flex items-center gap-1"
                  >
                    {copied ? <><Check className="h-3 w-3 text-green-500" /> Copied!</> : "Copy"}
                  </button>
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  Share this code with members so they can join this Mandal during registration.
                </p>
              </div>
            )}
          </div>
        </section>
      )}

      <button
        onClick={handleLogout}
        className="mt-4 flex min-h-[48px] w-full items-center justify-center gap-2 rounded-2xl border border-border bg-card text-sm font-semibold text-destructive"
      >
        <LogOut className="h-4 w-4" /> {t("logOut")}
      </button>
    </AppShell>
  );
}
