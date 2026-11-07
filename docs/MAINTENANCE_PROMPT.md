# Optional prompt for future documentation updates

This project already includes a complete screenshot gallery and updated README. You do **not** need an AI coding assistant to use the documentation package.

If you later modify the UI and want an assistant to recheck the documentation, use this short prompt:

> Work in my existing `billing-dispute-refund-tracker` repository. Keep the Java backend, SQL schema, auth and business rules unchanged. Verify the README and linked diagrams against current source files. Use the supplied Playwright screenshot-capture script to refresh only the 13 UI images against a disposable local demonstration instance. Do not invent test results, metrics, screenshots or a successful CI run. Preserve old terminal evidence, explain the selected ports and role workflow, and run `npm run build`, `npm test` and Playwright only when the required environment is available. Produce a concise changed-files list and an honest PASSED/FAILED/NOT EXECUTED table. Do not alter the .env, user's existing data, lockfiles or working dependency versions without explicit approval.

To regenerate application screenshots yourself, see [screenshot gallery](SCREENSHOT_GALLERY.md#regenerate-the-app-screenshots). The screenshots from 7 November 2026 are already included in this package.
