import { useEffect, useState } from "react";
import { useLanguage } from "../i18n/LanguageContext";

/**
 * Cuenta atras contra la hora que da el servidor, no la del dispositivo:
 * en el aula hay varias mesas y sus relojes no coinciden.
 */
export function RoundTimer({ deadlineAt, big }: { deadlineAt: string | null; big?: boolean }) {
  const { t } = useLanguage();
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const tick = setInterval(() => setNow(Date.now()), 500);
    return () => clearInterval(tick);
  }, []);

  if (deadlineAt === null) {
    return null;
  }
  const remaining = Math.max(0, Math.round((new Date(deadlineAt).getTime() - now) / 1000));
  const minutes = Math.floor(remaining / 60);
  const seconds = String(remaining % 60).padStart(2, "0");

  return (
    <div>
      <div className="marca-etiqueta">{remaining === 0 ? t("timer.expired") : t("timer.remaining")}</div>
      <div className={`${big ? "codigo" : "marca-valor"} ${remaining === 0 ? "agotado" : ""}`}>
        {minutes}:{seconds}
      </div>
    </div>
  );
}
