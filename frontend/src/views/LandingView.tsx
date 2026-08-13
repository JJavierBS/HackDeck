import type { RestClient } from "../api/restClient";
import type { GameSession, TournamentSession } from "../api/session";
import { useLanguage } from "../i18n/LanguageContext";
import { EntryView } from "./EntryView";

interface LandingViewProps {
  client: RestClient;
  onSession: (session: GameSession) => void;
  onTournament: (session: TournamentSession) => void;
}

export function LandingView({ client, onSession, onTournament }: LandingViewProps) {
  const { t } = useLanguage();

  const scrollToSection = (id: string) => {
    document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <main>
      <header className="landing-hero">
        <div className="eyebrow-badge">
          <span className="dot" />
          {t("landing.hero.eyebrow")}
        </div>
        <h1 className="landing-title">
          {t("landing.hero.title1")} <span className="acento-texto">{t("landing.hero.title2")}</span>{" "}
          {t("landing.hero.title3")}
        </h1>
        <p className="landing-lede">{t("landing.hero.lede")}</p>
        <div className="grupo-botones">
          <button onClick={() => scrollToSection("jugar")}>{t("landing.hero.cta.play")}</button>
          <button onClick={() => scrollToSection("mecanicas")}>{t("landing.hero.cta.learn")}</button>
        </div>
        <p className="tenue">{t("landing.hero.note")}</p>

        <div className="landing-stats">
          <div className="landing-stat">
            <span className="landing-stat-val">6</span>
            <span className="landing-stat-lbl">{t("landing.stats.rounds")}</span>
          </div>
          <div className="landing-stat">
            <span className="landing-stat-val">44</span>
            <span className="landing-stat-lbl">{t("landing.stats.cards")}</span>
          </div>
          <div className="landing-stat">
            <span className="landing-stat-val">100%</span>
            <span className="landing-stat-lbl">{t("landing.stats.browser")}</span>
          </div>
        </div>
      </header>

      <section id="mecanicas" className="seccion-landing">
        <h2>{t("landing.loop.kicker")}</h2>
        <div className="grid-tres">
          <article className="tarjeta-landing">
            <h3>{t("landing.loop.f1.title")}</h3>
            <p>{t("landing.loop.f1.desc")}</p>
          </article>
          <article className="tarjeta-landing">
            <h3>{t("landing.loop.f2.title")}</h3>
            <p>{t("landing.loop.f2.desc")}</p>
          </article>
          <article className="tarjeta-landing">
            <h3>{t("landing.loop.f3.title")}</h3>
            <p>{t("landing.loop.f3.desc")}</p>
          </article>
        </div>
      </section>

      <section className="seccion-landing">
        <h2>{t("landing.teams.kicker")}</h2>
        <div className="grid-dos">
          <article className="tarjeta-landing bando-red">
            <h3>{t("landing.teams.red.title")}</h3>
            <p>{t("landing.teams.red.desc")}</p>
          </article>
          <article className="tarjeta-landing bando-blue">
            <h3>{t("landing.teams.blue.title")}</h3>
            <p>{t("landing.teams.blue.desc")}</p>
          </article>
        </div>
      </section>

      <section className="seccion-landing">
        <h2>{t("landing.classroom.kicker")}</h2>
        <article className="tarjeta-landing">
          <h3>{t("landing.classroom.title")}</h3>
          <p>{t("landing.classroom.desc")}</p>
        </article>
      </section>

      <section id="jugar" className="seccion-landing">
        <h2>{t("landing.cta.title")}</h2>
        <EntryView client={client} onSession={onSession} onTournament={onTournament} />
      </section>

      <footer className="landing-footer">
        <div>
          {t("landing.footer.by")}{" "}
          <a href="https://josejavierbravo.com" target="_blank" rel="noreferrer">
            {t("landing.footer.portfolio")}
          </a>
        </div>
        <div>{t("landing.footer.rights")}</div>
      </footer>
    </main>
  );
}
