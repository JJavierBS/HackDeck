/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Solo para apuntar a un backend remoto; en local basta con el proxy de Vite. */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
