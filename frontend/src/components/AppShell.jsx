import { NavLink, useLocation } from "react-router-dom";
import { Home, Receipt, Wallet, Megaphone, User, Flame } from "lucide-react";
import { ThemeToggle } from "./ThemeToggle";
import { useLang } from "../context/LangContext";
import { useAuth } from "../context/AuthContext";

export function AppShell({ title, subtitle, mandalName: propMandalName, children }) {
  const { pathname } = useLocation();
  const { t } = useLang();
  const { mandalName: contextMandalName } = useAuth();
  const displayMandalName = propMandalName || contextMandalName || t("mandalName");

  const tabs = [
    { to: "/dashboard", label: t("home"), icon: Home },
    { to: "/vargani", label: t("vargani"), icon: Receipt },
    { to: "/expenses", label: t("expenses"), icon: Wallet },
    { to: "/notices", label: t("notices"), icon: Megaphone },
    { to: "/profile", label: t("profile"), icon: User },
  ];

  return (
    <div className="min-h-screen bg-background">
      <div className="pointer-events-none fixed inset-x-0 top-0 h-64 bg-[radial-gradient(60%_100%_at_50%_0%,var(--color-primary-soft),transparent_70%)] opacity-25 dark:opacity-15" />

      <div className="relative mx-auto w-full max-w-xl px-5 pb-32 pt-6 sm:max-w-2xl">
        <header className="mb-7 flex items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <img src="/logo.png" alt="Logo" className="h-14 w-14 rounded-2xl object-contain shrink-0" />
            <div>
              <p className="text-[11px] font-medium uppercase tracking-[0.22em] text-muted-foreground">
                {displayMandalName}
              </p>
              <h1 className="font-display mt-0.5 text-3xl leading-tight font-semibold tracking-tight text-foreground">
                {title}
              </h1>
              {subtitle ? (
                <p className="mt-0.5 text-sm text-muted-foreground">{subtitle}</p>
              ) : null}
            </div>
          </div>
          <ThemeToggle className="shrink-0" />
        </header>

        {children}
      </div>

      <nav className="fixed inset-x-0 bottom-0 z-50 px-4 pb-4">
        <div className="bg-background/95 dark:bg-background/95 backdrop-blur-2xl border border-border/50 shadow-lg mx-auto flex max-w-xl items-stretch justify-between rounded-3xl p-1.5">
          {tabs.map(({ to, label, icon: Icon }) => {
            const active = pathname === to;
            return (
              <NavLink
                key={to}
                to={to}
                className={`relative flex min-h-[52px] flex-1 flex-col items-center justify-center gap-1 rounded-2xl px-1 py-2 text-[11px] font-medium transition-colors ${
                  active
                    ? "bg-secondary text-primary"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <Icon className="h-[18px] w-[18px]" strokeWidth={active ? 2.4 : 1.8} />
                <span>{label}</span>
                {active ? (
                  <span className="absolute bottom-1 h-1 w-1 rounded-full bg-primary" />
                ) : null}
              </NavLink>
            );
          })}
        </div>
      </nav>
    </div>
  );
}

export function FlameIcon({ className = "" }) {
  return <Flame className={`flame text-primary ${className}`} strokeWidth={2} />;
}
