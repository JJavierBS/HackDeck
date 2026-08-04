import type { GameStateDto } from "../api/restClient";

const OUTCOME_TEXT: Record<string, string> = {
  TAKEDOWN: "derribo de un pilar",
  TAKEDOWN_FASTER: "derribo mas rapido",
  POINTS: "mejor defensa de la triada",
  DRAW: "empate",
};

export function MatchScoreboard({ state }: { state: GameStateDto }) {
  const result = state.result;
  if (result === null) {
    return null;
  }
  const winnerName = result.winner === null ? null : state.teams[result.winner];

  return (
    <section>
      <h2>Fin del match</h2>
      <p>
        {winnerName === null ? "Empate" : `Gana ${winnerName}`} por {OUTCOME_TEXT[result.outcome]}
      </p>
      <ul>
        {Object.entries(result.defendedCia).map(([team, defended]) => (
          <li key={team}>
            {state.teams[team]} defendio {defended} puntos de triada
            {result.takedownRound[team] !== undefined
              ? `, y derribo un pilar en la ronda ${result.takedownRound[team]}`
              : ""}
          </li>
        ))}
      </ul>
    </section>
  );
}
