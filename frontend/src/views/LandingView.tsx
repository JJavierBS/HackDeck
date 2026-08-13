import { useLanguage } from "../i18n/LanguageContext";

interface LandingViewProps {
  onEnterGame: () => void;
}

export function LandingView({ onEnterGame }: LandingViewProps) {
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
          {t("landing.hero.title1")} <span className="acento-texto">{t("landing.hero.title2")}</span>
        </h1>
        <p className="landing-lede">{t("landing.hero.lede")}</p>
        <div className="grupo-botones">
          <button onClick={onEnterGame}>{t("landing.hero.cta.play")}</button>
          <button onClick={() => scrollToSection("mecanicas")}>{t("landing.hero.cta.learn")}</button>
        </div>
        <p className="tenue">{t("landing.hero.note")}</p>
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
        <h2>{t("landing.shots.kicker")}</h2>
        <div className="grid-tres">
          <div className="placeholder-captura">
            <span className="placeholder-captura-icono">🖥️</span>
            <p>{t("landing.shots.p1")}</p>
          </div>
          <div className="placeholder-captura">
            <span className="placeholder-captura-icono">🎛️</span>
            <p>{t("landing.shots.p2")}</p>
          </div>
          <div className="placeholder-captura">
            <span className="placeholder-captura-icono">📊</span>
            <p>{t("landing.shots.p3")}</p>
          </div>
        </div>
      </section>

      <section className="seccion-landing">
        <h2>{t("landing.about.kicker")}</h2>
        <article className="tarjeta-landing">
          <h3>{t("landing.about.title")}</h3>
          <p>{t("landing.about.desc")}</p>
          <div className="grupo-botones" style={{ marginTop: "1rem" }}>
            <a
              href="https://josejavierbravo.com"
              target="_blank"
              rel="noreferrer"
              style={{ color: "var(--acento)", fontFamily: "var(--mono)", fontSize: "0.85rem" }}
            >
              {t("landing.about.link")}
            </a>
          </div>
        </article>
      </section>

      <section className="seccion-landing" style={{ textAlign: "center", padding: "2rem 0" }}>
        <h2>{t("landing.cta.title")}</h2>
        <p className="landing-lede" style={{ margin: "0 auto 1.5rem" }}>
          {t("landing.cta.lede")}
        </p>
        <button onClick={onEnterGame}>{t("landing.hero.cta.play")}</button>
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
