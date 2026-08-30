import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { ArrowUpRight, Plus, Receipt, Wallet, TrendingUp, PartyPopper, X, PieChart as PieChartIcon, Building2 } from "lucide-react";
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { AppShell, FlameIcon } from "@/components/AppShell";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";
import * as api from "@/api/index";
import { createMandal } from "@/api";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

function useCountUp(target) {
  const [value, setValue] = useState(target);
  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    let frame = 0;
    const total = 42;
    setValue(0);
    const id = setInterval(() => {
      frame += 1;
      const t = 1 - Math.pow(1 - frame / total, 3);
      setValue(Math.round(target * t));
      if (frame >= total) clearInterval(id);
    }, 16);
    return () => clearInterval(id);
  }, [target]);
  return value;
}

export default function DashboardPage() {
  const { user, login } = useAuth();
  const { t, lang } = useLang();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const [creatingMandal, setCreatingMandal] = useState(false);
  const [joiningMandal, setJoiningMandal] = useState(false);
  const [newMandalName, setNewMandalName] = useState("");
  const [inviteCode, setInviteCode] = useState("");

  const location = useLocation();
  const [showWelcome, setShowWelcome] = useState(false);

  useEffect(() => {
    if (location.state?.justRegistered && user?.mandalId) {
      setShowWelcome(true);
      window.history.replaceState({}, document.title);
    }
  }, [location, user?.mandalId]);

  useEffect(() => {
    if (!user?.mandalId) {
      setLoading(false);
      return;
    }
    api.getDashboard()
      .then((res) => setData(res.data || res))
      .catch((err) => console.error("Dashboard fetch failed:", err))
      .finally(() => setLoading(false));
  }, [user]);

  const handleCreateMandal = async () => {
    if (!newMandalName.trim()) return;
    try {
      const res = await api.createMandal(newMandalName);
      const { token, user: updatedUser } = res.data;
      login(token, updatedUser);
      setShowWelcome(true);
    } catch (err) {
      alert("Failed to create mandal: " + err.message);
    }
  };

  const handleJoinMandal = async () => {
    if (!inviteCode.trim()) return;
    try {
      const res = await api.joinMandal(inviteCode);
      const { token, user: updatedUser } = res.data;
      login(token, updatedUser);
      setShowWelcome(true);
    } catch (err) {
      alert("Failed to join mandal: " + err.message);
    }
  };

  const totalCollected = data?.totalCollected ?? 0;
  const totalSpent = data?.totalSpent ?? 0;
  const balance = totalCollected - totalSpent;
  const target = data?.target ?? 250000;
  const recentActivity = data?.recentActivity ?? [];

  const shown = useCountUp(balance);
  const progress = Math.min(100, Math.round((totalCollected / target) * 100));

  if (loading) {
    return (
      <AppShell title={t("dashboard")} subtitle="Loading...">
        <div className="flex items-center justify-center py-20">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        </div>
      </AppShell>
    );
  }

  if (!user?.mandalId) {
    return (
      <AppShell title="Welcome" subtitle="Get Started">
        <section className="ink-panel relative overflow-hidden rounded-3xl p-8 text-center mt-8">
          <h2 className="font-display text-2xl font-semibold mb-4">You are not part of any Mandal</h2>
          <p className="text-muted-foreground mb-8 text-sm">
            Create a new Mandal to become an Admin, or ask an existing Admin for an Invite Code and join an existing one.
          </p>
          
          {creatingMandal ? (
            <div className="space-y-4 max-w-sm mx-auto">
              <input
                type="text"
                placeholder="Enter Mandal Name"
                value={newMandalName}
                onChange={(e) => setNewMandalName(e.target.value)}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
              />
              <div className="flex gap-2">
                <button
                  onClick={handleCreateMandal}
                  className="flex-1 rounded-2xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground"
                >
                  Create
                </button>
                <button
                  onClick={() => setCreatingMandal(false)}
                  className="flex-1 rounded-2xl border border-input px-4 py-3 text-sm font-semibold"
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : joiningMandal ? (
            <div className="space-y-4 max-w-sm mx-auto">
              <input
                type="text"
                placeholder="Enter Invite Code (e.g. MANDAL-ABC123)"
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value)}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
              />
              <div className="flex gap-2">
                <button
                  onClick={handleJoinMandal}
                  className="flex-1 rounded-2xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground"
                >
                  Join
                </button>
                <button
                  onClick={() => setJoiningMandal(false)}
                  className="flex-1 rounded-2xl border border-input px-4 py-3 text-sm font-semibold"
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-3 max-w-xs mx-auto">
              <button
                onClick={() => setCreatingMandal(true)}
                className="accent-gradient inline-flex min-h-[48px] items-center justify-center rounded-2xl px-6 text-sm font-semibold text-primary-foreground transition-transform active:scale-95"
              >
                Create New Mandal
              </button>
              <button
                onClick={() => setJoiningMandal(true)}
                className="surface-lift inline-flex min-h-[48px] items-center justify-center rounded-2xl px-6 text-sm font-semibold text-foreground transition-transform active:scale-95"
              >
                Join Existing Mandal
              </button>
            </div>
          )}
        </section>
      </AppShell>
    );
  }

  return (
    <AppShell title={t("dashboard")} subtitle={t("dashboardSubtitle")}>
      {/* Signature balance card */}
      <section className="ink-panel glow-accent relative overflow-hidden rounded-3xl p-6">
        <div className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-[var(--color-primary)] opacity-30 blur-3xl" />
        <div className="relative">
          <div className="flex items-center gap-2 text-[11px] font-medium uppercase tracking-[0.24em] text-ink-foreground/60">
            <FlameIcon className="h-3.5 w-3.5" />
            {t("balanceInHand")}
          </div>
          <p className="font-display tabular mt-3 text-[3.1rem] leading-none font-semibold text-ink-foreground">
            {inr(shown)}
          </p>

          <div className="mt-6 grid grid-cols-2 gap-3">
            <MiniStat label={t("collectedLabel")} value={inr(totalCollected)} />
            <MiniStat label={t("spentLabel")} value={inr(totalSpent)} />
          </div>


        </div>
      </section>

      {/* Quick actions - hidden for MEMBER */}
      {user?.role !== "MEMBER" && (
        <section className="mt-4 grid grid-cols-2 gap-3">
          <QuickAction to="/vargani" state={{ openForm: true }} icon={Receipt} label={t("addVargani")} accent />
          <QuickAction to="/expenses" state={{ openForm: true }} icon={Wallet} label={t("addExpense")} />
        </section>
      )}

      {/* Vargani Tracker Card - ADMIN/KARYAKARTA only */}
      {user?.role !== "MEMBER" && (
        <Link
          to="/tracker"
          className="mt-3 flex items-center gap-4 rounded-2xl surface-lift px-5 py-4 transition-transform active:scale-[0.98]"
        >
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary/10">
            <Building2 className="h-5 w-5 text-primary" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-foreground">{t("trackCollection")}</p>
            <p className="text-xs text-muted-foreground truncate">{t("trackCollectionDesc")}</p>
          </div>
          <ArrowUpRight className="h-4 w-4 text-muted-foreground shrink-0" />
        </Link>
      )}

      {/* Fund Utilization Analytics */}
      <section className="mt-7">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-display text-lg font-semibold">
            {lang === "mr" ? "निधीचा वापर (Fund Utilization)" : "Fund Utilization"}
          </h2>
        </div>

        {totalCollected > 0 ? (
          <div className="surface-lift rounded-2xl p-6 flex flex-col items-center mb-6">
            <div className="h-60 w-full relative">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={[
                      { name: t("spentLabel"), value: totalSpent, fill: "#f97316" },
                      { name: t("balance"), value: balance > 0 ? balance : 0, fill: "#10b981" }
                    ]}
                    cx="50%"
                    cy="50%"
                    innerRadius={70}
                    outerRadius={90}
                    paddingAngle={5}
                    dataKey="value"
                    stroke="none"
                    cornerRadius={5}
                  >
                    <Cell fill="#f97316" />
                    <Cell fill="#10b981" opacity={0.8} />
                  </Pie>
                  <Tooltip 
                    formatter={(value) => inr(value)}
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: 'var(--shadow-float)' }}
                  />
                  <Legend 
                    verticalAlign="bottom" 
                    height={36}
                    iconType="circle"
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none pb-8">
                <div className="text-center">
                  <span className="block text-xs font-medium text-muted-foreground uppercase tracking-wider">{t("totalCollection")}</span>
                  <span className="block font-display text-2xl font-bold text-foreground">{inr(totalCollected)}</span>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="surface-lift rounded-2xl px-6 py-14 text-center flex flex-col items-center mb-6">
            <div className="h-12 w-12 rounded-full bg-secondary text-muted-foreground flex items-center justify-center mb-3">
              <PieChartIcon className="h-6 w-6" />
            </div>
            <p className="font-display text-lg font-semibold">{t("noActivityYet")}</p>
            <p className="mt-1 text-sm text-muted-foreground">
              Start by adding a vargani to see your fund utilization.
            </p>
          </div>
        )}

        {/* Recent Activity */}
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-display text-lg font-semibold">
            {t("recentActivity")}
          </h2>
          <Link
            to="/vargani"
            className="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary-deep"
          >
            {t("viewAll")} <ArrowUpRight className="h-3.5 w-3.5" />
          </Link>
        </div>
        
        {recentActivity?.length > 0 ? (
          <ul className="surface-lift divide-y divide-border overflow-hidden rounded-2xl">
            {recentActivity.map((a, i) => (
              <li key={i} className="flex items-center gap-3 px-4 py-3.5">
                <span
                  className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${
                    a.type === "CONTRIBUTION" ? "bg-primary/12 text-primary" : "bg-secondary text-muted-foreground"
                  }`}
                >
                  {a.type === "CONTRIBUTION" ? (
                    <TrendingUp className="h-4 w-4" />
                  ) : (
                    <Wallet className="h-4 w-4" />
                  )}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-medium">{a.description}</span>
                </span>
                <span
                  className={`tabular text-sm font-semibold ${
                    a.type === "CONTRIBUTION" ? "text-primary" : "text-foreground"
                  }`}
                >
                  {a.type === "CONTRIBUTION" ? "+" : "−"}
                  {inr(a.amount)}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <div className="surface-lift rounded-2xl px-6 py-14 text-center">
            <p className="font-display text-lg font-semibold">{t("noActivityYet")}</p>
            <p className="mt-1 text-sm text-muted-foreground">
              {user?.role === "MEMBER" ? t("memberNoActivity") : t("adminNoActivity")}
            </p>
          </div>
        )}
      </section>

      {/* Welcome Modal */}
      {showWelcome && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <button
            onClick={() => setShowWelcome(false)}
            className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in duration-300"
          />
          <div className="relative w-full max-w-sm rounded-3xl bg-background p-8 text-center shadow-2xl animate-in zoom-in-95 duration-300">
            <button
              onClick={() => setShowWelcome(false)}
              className="absolute right-4 top-4 rounded-full bg-secondary p-1 text-muted-foreground hover:bg-secondary/80 hover:text-foreground"
            >
              <X className="h-4 w-4" />
            </button>
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
              <PartyPopper className="h-8 w-8 text-primary" />
            </div>
            <h2 className="font-display text-2xl font-bold tracking-tight text-foreground">
              Welcome to the Mandal!
            </h2>
            <p className="mt-2 text-sm text-muted-foreground">
              You've successfully joined. You can now view all the latest contributions, expenses, and announcements from the committee.
            </p>
            <button
              onClick={() => setShowWelcome(false)}
              className="accent-gradient mt-6 w-full rounded-2xl py-3.5 text-sm font-semibold text-primary-foreground shadow-lg active:scale-95 transition-transform"
            >
              Get Started
            </button>
          </div>
        </div>
      )}
    </AppShell>
  );
}

function MiniStat({ label, value }) {
  return (
    <div className="rounded-2xl border border-ink-foreground/10 bg-ink-foreground/6 px-4 py-3">
      <p className="text-[11px] uppercase tracking-[0.16em] text-ink-foreground/55">{label}</p>
      <p className="tabular mt-1 text-lg font-semibold text-ink-foreground">{value}</p>
    </div>
  );
}

function QuickAction({ to, state, icon: Icon, label, accent }) {
  return (
    <Link
      to={to}
      state={state}
      className={`flex min-h-[56px] items-center gap-2.5 whitespace-nowrap rounded-2xl px-4 py-3 text-sm font-medium transition-transform active:scale-[0.98] ${
        accent
          ? "accent-gradient text-primary-foreground shadow-[var(--shadow-float)]"
          : "surface-lift text-foreground"
      }`}
    >
      <Icon className="h-4 w-4" />
      {label}
      <Plus className="ml-auto h-4 w-4 opacity-70" />
    </Link>
  );
}
