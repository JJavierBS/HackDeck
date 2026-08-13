import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { DICTIONARY, type Language, type TranslationKey } from "./dictionary";

interface LanguageContextValue {
  language: Language;
  toggle: () => void;
  t: (key: TranslationKey) => string;
  /** Elige el idioma de un texto que ya viene traducido del servidor. */
  fromServer: (texts: Record<string, string>) => string;
}

const STORAGE_KEY = "cyberdeck.language";

const LanguageContext = createContext<LanguageContextValue | null>(null);

function initialLanguage(): Language {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === "es" || stored === "en") {
    return stored;
  }
  return navigator.language.startsWith("en") ? "en" : "es";
}

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<Language>(initialLanguage);

  const value = useMemo<LanguageContextValue>(
    () => ({
      language,
      toggle: () => {
        const next: Language = language === "es" ? "en" : "es";
        localStorage.setItem(STORAGE_KEY, next);
        setLanguage(next);
      },
      t: (key) => DICTIONARY[language][key],
      fromServer: (texts) => texts[language] ?? texts.es ?? Object.values(texts)[0] ?? "",
    }),
    [language],
  );

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage(): LanguageContextValue {
  const context = useContext(LanguageContext);
  if (context === null) {
    throw new Error("useLanguage necesita estar dentro de LanguageProvider");
  }
  return context;
}
