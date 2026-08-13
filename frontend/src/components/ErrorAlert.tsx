import { ApiRequestError } from "../api/restClient";
import { useLanguage } from "../i18n/LanguageContext";
import type { TranslationKey } from "../i18n/dictionary";

interface ErrorAlertProps {
  error: unknown;
}

export function ErrorAlert({ error }: ErrorAlertProps) {
  const { t } = useLanguage();

  if (error === null || error === undefined) {
    return null;
  }

  let title = t("error.title.generico");
  let message = t("entry.error.network");

  if (error instanceof ApiRequestError) {
    const titleKey = `error.title.${error.code}` as TranslationKey;
    const msgKey = `error.${error.code}` as TranslationKey;

    const translatedTitle = t(titleKey);
    const translatedMsg = t(msgKey);

    title = translatedTitle !== titleKey ? translatedTitle : error.code;
    message = translatedMsg !== msgKey ? translatedMsg : error.message;
  } else if (typeof error === "string") {
    message = error;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return (
    <div className="alerta-error" role="alert">
      <span className="alerta-error-icono">!</span>
      <div className="alerta-error-contenido">
        <div className="alerta-error-titulo">{title}</div>
        <p className="alerta-error-mensaje">{message}</p>
      </div>
    </div>
  );
}
