import { fileURLToPath } from 'node:url';
import { defineConfig } from 'playwright/test';

const origin = 'http://127.0.0.1:6217';

export default defineConfig({
  testDir: './grid-layout',
  outputDir: '../node_modules/.cache/playwright',
  workers: 1,
  use: { baseURL: origin, channel: process.env.PLAYWRIGHT_CHANNEL },
  webServer: {
    command: 'pnpm run dev --host 127.0.0.1 --port 6217 --strictPort',
    cwd: fileURLToPath(new URL('../', import.meta.url)),
    port: 6217,
  },
});
