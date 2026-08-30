import { createContext, useContext, useState } from "react";
import { t as translate } from "../i18n/translations";

const LangContext = createContext(null);

export function LangProvider({ children }) {
  const [lang, setLang] = useState(() => {
    const stored = localStorage.getItem("mandal-lang");
    return stored || "en";
  });

  const switchLang = (newLang) => {
    setLang(newLang);
    localStorage.setItem("mandal-lang", newLang);
  };

  const t = (key) => translate(key, lang);

  return (
    <LangContext.Provider value={{ lang, switchLang, t }}>
      {children}
    </LangContext.Provider>
  );
}

export function useLang() {
  const ctx = useContext(LangContext);
  if (!ctx) throw new Error("useLang must be used within LangProvider");
  return ctx;
}
