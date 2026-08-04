import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const backend = "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  server: {
    // Con el proxy, el front habla siempre con su propio origen: ni CORS en
    // desarrollo ni URLs distintas entre desarrollo y produccion.
    proxy: {
      "/api": backend,
      "/ws": { target: backend, ws: true },
    },
  },
  build: {
    outDir: "dist",
  },
});
