// @ts-check
const path = require('path');
const { defineConfig, devices } = require('@playwright/test');

// So the auto-started backend sees DB_* / JWT_* (same as running `npm start` in backend/)
require('dotenv').config({ path: path.join(__dirname, '..', 'backend', '.env') });

const baseURL = process.env.BASE_URL || 'http://localhost:3000';

/** WebKit on Windows is often unstable; skip unless RUN_WEBKIT=1 */
const isWin = process.platform === 'win32';
const runWebKit = process.env.RUN_WEBKIT === '1';

const projects = [
  { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  {
    name: 'Mobile Chrome',
    use: { ...devices['Pixel 7'] },
  },
  { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
];

module.exports = defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 4 : undefined,
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    ['allure-playwright', { outputFolder: 'allure-results', suiteTitle: 'FindIt Playwright' }],
  ],
  timeout: 60_000,
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    actionTimeout: 25_000,
    navigationTimeout: 45_000,
  },
  projects,
  webServer: process.env.SKIP_WEBSERVER
    ? undefined
    : {
        command: 'node scripts/seed.js && node src/server.js',
        cwd: path.join(__dirname, '..', 'backend'),
        url: `${baseURL.replace(/\/$/, '')}/health`,
        reuseExistingServer: true,
        timeout: 120_000,
        env: { ...process.env, NODE_ENV: process.env.NODE_ENV || 'development' },
      },
});
