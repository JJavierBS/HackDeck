import { useLanguage } from "../i18n/LanguageContext";

/**
 * La espera de verdad es la del arranque de partida, que puede durar lo que
 * tarde el instructor: conviene que se vea que la pantalla sigue viva.
 */
export function Loading({ big = false }: { big?: boolean }) {
  const { t } = useLanguage();
  return (
    <div className={big ? "carga-pantalla carga-pantalla-grande" : "carga-pantalla"} role="status">
      <span className="spinner-mono" />
      <span>{t("app.loading")}</span>
    </div>
  );
}
