import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

const PHASES = ["RECON", "ACCESS", "ESCALATION", "IMPACT"] as const;

/**
 * Sin la fase previa se puede atacar igual, pero con mucha menos
 * probabilidad: se muestra para que el equipo entienda por que falla.
 */
export function KillChainPanel({ unlocked }: { unlocked: string[] }) {
  const { t } = useLanguage();

  return (
    <section>
      <h2>{t("killchain.title")}</h2>
      <div className="fase">
        {PHASES.map((phase) => {
          const open = unlocked.includes(phase);
          return (
            <div key={phase} className={`fase-paso ${open ? "abierta" : ""}`}>
              {open ? "●" : "○"} {t(`killchain.${phase}` as TranslationKey)}
              {!open && <div className="tenue">{t("killchain.locked")}</div>}
            </div>
          );
        })}
      </div>
    </section>
  );
}
