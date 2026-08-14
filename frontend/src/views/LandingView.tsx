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
        <div className="landing-hero-head">
          <img src="/hackdecklogo.png" alt="HackDeck Logo" className="landing-logo-img" />
          <h1 className="landing-title">
            Hack<span className="acento-texto">Deck</span>
          </h1>
        </div>
        <p className="landing-lede">{t("landing.hero.lede")}</p>
        <div className="grupo-botones">
          <button className="boton-principal-destacado" onClick={onEnterGame}>
            <span>{t("landing.hero.cta.play")}</span>
            <span className="boton-icono" aria-hidden="true">→</span>
          </button>
          <button onClick={() => scrollToSection("mecanicas")}>{t("landing.hero.cta.learn")}</button>
          <a
            href="https://github.com/JJavierBS/HackDeck"
            target="_blank"
            rel="noreferrer"
            style={{ textDecoration: "none" }}
          >
            <button type="button">{t("landing.nav.github")}</button>
          </a>
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
        <div className="galeria-capturas">
          <div className="tarjeta-captura tarjeta-captura-destacada">
            <div className="captura-marco">
              <img
                src="/capturas/proyector.png"
                alt={t("landing.shots.proyector.title")}
                className="captura-img"
                loading="lazy"
              />
            </div>
            <div className="captura-info">
              <h4>{t("landing.shots.proyector.title")}</h4>
              <p>{t("landing.shots.proyector.desc")}</p>
            </div>
          </div>
          <div className="grid-tres-capturas">
            <div className="tarjeta-captura">
              <div className="captura-marco">
                <img
                  src="/capturas/atack.png"
                  alt={t("landing.shots.atack.title")}
                  className="captura-img"
                  loading="lazy"
                />
              </div>
              <div className="captura-info">
                <h4>{t("landing.shots.atack.title")}</h4>
                <p>{t("landing.shots.atack.desc")}</p>
              </div>
            </div>
            <div className="tarjeta-captura">
              <div className="captura-marco">
                <img
                  src="/capturas/deffend.png"
                  alt={t("landing.shots.deffend.title")}
                  className="captura-img"
                  loading="lazy"
                />
              </div>
              <div className="captura-info">
                <h4>{t("landing.shots.deffend.title")}</h4>
                <p>{t("landing.shots.deffend.desc")}</p>
              </div>
            </div>
            <div className="tarjeta-captura">
              <div className="captura-marco">
                <img
                  src="/capturas/instructor.png"
                  alt={t("landing.shots.instructor.title")}
                  className="captura-img"
                  loading="lazy"
                />
              </div>
              <div className="captura-info">
                <h4>{t("landing.shots.instructor.title")}</h4>
                <p>{t("landing.shots.instructor.desc")}</p>
              </div>
            </div>
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

      <section className="tarjeta-landing landing-cta-box">
        <h3>{t("landing.cta.title")}</h3>
        <p className="landing-lede" style={{ margin: "0 auto 1.5rem" }}>
          {t("landing.cta.lede")}
        </p>
        <button className="boton-principal-destacado" onClick={onEnterGame}>
          <span>{t("landing.hero.cta.play")}</span>
          <span className="boton-icono" aria-hidden="true">→</span>
        </button>
      </section>

      <footer className="landing-footer">
        <div className="landing-footer-brand">
          <img src="/hackdecklogo.png" alt="HackDeck" className="footer-logo-img" />
          <span>
            {t("landing.footer.by")}{" "}
            <a href="https://josejavierbravo.com" target="_blank" rel="noreferrer">
              {t("landing.footer.portfolio")}
            </a>{" "}
            ·{" "}
            <a href="https://github.com/JJavierBS/HackDeck" target="_blank" rel="noreferrer">
              {t("landing.footer.repo")}
            </a>
          </span>
        </div>
        <div className="footer-socials">
          <a
            href="https://github.com/JJavierBS"
            target="_blank"
            rel="noreferrer"
            className="footer-social-link"
            aria-label="GitHub de Jose Javier Bravo"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
            </svg>
          </a>
          <a
            href="https://linkedin.com/in/jjavierbs"
            target="_blank"
            rel="noreferrer"
            className="footer-social-link"
            aria-label="LinkedIn de Jose Javier Bravo"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.79-1.75-1.764s.784-1.764 1.75-1.764 1.75.79 1.75 1.764-.783 1.764-1.75 1.764zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z" />
            </svg>
          </a>
        </div>
      </footer>
    </main>
  );
}
