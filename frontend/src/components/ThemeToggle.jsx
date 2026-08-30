import { useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";

function apply(theme) {
  document.documentElement.classList.toggle("dark", theme === "dark");
  localStorage.setItem("mandal-theme", theme);
}

export function ThemeToggle({ className = "" }) {
  const [theme, setTheme] = useState("light");

  useEffect(() => {
    const stored = localStorage.getItem("mandal-theme");
    const initial =
      stored ??
      (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    setTheme(initial);
    apply(initial);
  }, []);

  return (
    <button
      type="button"
      aria-label="Toggle dark mode"
      onClick={() => {
        const next = theme === "dark" ? "light" : "dark";
        setTheme(next);
        apply(next);
      }}
      className={`inline-flex h-11 w-11 items-center justify-center rounded-full border border-border bg-card/70 text-foreground transition-colors hover:bg-accent ${className}`}
    >
      {theme === "dark" ? <Sun className="h-[18px] w-[18px]" /> : <Moon className="h-[18px] w-[18px]" />}
    </button>
  );
}
