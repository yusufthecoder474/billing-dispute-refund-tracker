import { defineConfig } from '@playwright/test';

// A separate config keeps this demonstration-only capture out of `npm test`.
export default defineConfig({
  testDir: './readme-captures',
  workers: 1,
  retries: 0,
  timeout: 90_000,
  reporter: [['list']],
  use: {
    baseURL: process.env.BASE_URL ?? 'http://127.0.0.1:18081',
    browserName: 'chromium',
    headless: true,
  },
});
