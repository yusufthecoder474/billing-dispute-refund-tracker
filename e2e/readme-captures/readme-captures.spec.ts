import { mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { expect, test } from '@playwright/test';
import { loginUi } from '../tests/helpers';

const screenshots = resolve(process.cwd(), '..', 'docs', 'screenshots');

// This is an opt-in documentation task. It creates a new synthetic dispute,
// but does not delete or edit earlier demo disputes.
test('capture the 13 README screenshots from the live demo', async ({ page }) => {
  await mkdir(screenshots, { recursive: true });
  async function capture(file: string) {
    await page.screenshot({ path: resolve(screenshots, file), fullPage: true });
  }
  async function signOut() {
    await page.getByRole('button', { name: 'Sign out' }).click();
    await expect(page.getByRole('button', { name: 'Sign in' })).toBeVisible();
  }
  async function openDispute(id: string) {
    await page.getByRole('button', { name: `#${id}`, exact: true }).click();
    await expect(page.getByRole('heading', { name: `Dispute #${id}` })).toBeVisible();
  }

  await page.goto('/');
  await expect(page.getByRole('button', { name: 'Sign in' })).toBeVisible();
  await capture('01-login.png');

  await loginUi(page, 'customer');
  await expect(page.getByRole('heading', { name: 'My disputes' })).toBeVisible();
  await capture('02-customer-disputes.png');
  await page.getByRole('button', { name: 'New dispute' }).click();
  await page.getByLabel('Disputed amount (INR)').fill('100.00');
  await page.getByLabel('Description').fill('Synthetic duplicate subscription charge for documentation capture');
  await page.getByRole('button', { name: 'Submit dispute', exact: true }).click();
  const h = page.getByRole('heading', { name: /^Dispute #\d+$/ });
  await expect(h).toBeVisible();
  const id = (await h.textContent())?.match(/#(\d+)/)?.[1];
  if (!id) throw new Error('Could not determine the newly created dispute ID');
  await capture('03-customer-dispute-detail.png');

  await signOut();
  await loginUi(page, 'manager');
  await expect(page.getByRole('heading', { name: 'All disputes' })).toBeVisible();
  await capture('04-manager-all-disputes.png');
  await openDispute(id);
  await expect(page.getByLabel('Support agent')).toBeVisible();
  await capture('05-manager-assign-support.png');
  await page.getByLabel('Support agent').selectOption({ label: 'Demo support' });
  await page.getByRole('button', { name: 'Assign', exact: true }).click();
  await expect(page.getByText('ASSIGNED', { exact: true })).toBeVisible();
  await capture('06-assignment-confirmed.png');

  await signOut();
  await loginUi(page, 'support');
  await expect(page.getByRole('heading', { name: 'My support queue' })).toBeVisible();
  await openDispute(id);
  await expect(page.getByRole('button', { name: 'Start review' })).toBeVisible();
  await capture('07-support-open.png');
  await page.getByRole('button', { name: 'Start review' }).click();
  await expect(page.getByRole('button', { name: 'Submit for decision' })).toBeVisible();
  await capture('08-support-in-review.png');
  await page.getByRole('button', { name: 'Submit for decision' }).click();
  await expect(page.getByText('SUBMITTED FOR DECISION', { exact: true })).toBeVisible();
  await capture('09-pending-decision.png');

  await signOut();
  await loginUi(page, 'manager');
  await openDispute(id);
  await expect(page.getByRole('heading', { name: 'Record final decision' })).toBeVisible();
  await capture('10-manager-decision-form.png');
  await page.getByLabel('Approved refund (INR)').fill('75.50');
  await page.getByLabel('Explanation').fill('Synthetic duplicate charge confirmed; simulated partial refund approved.');
  await page.getByRole('button', { name: 'Save final decision' }).click();
  await expect(page.getByRole('heading', { name: 'Final decision: APPROVED' })).toBeVisible();
  await capture('11-resolved-and-audit.png');
  await page.getByRole('button', { name: 'SLA & reports' }).click();
  await expect(page.getByRole('heading', { name: 'Manager dashboard' })).toBeVisible();
  await expect(page.getByText('Resolved within SLA', { exact: true })).toBeVisible();
  await capture('12-sla-dashboard.png');
  await page.getByRole('button', { name: 'Generate report' }).click();
  await expect(page.getByRole('link', { name: 'Download CSV' })).toBeVisible();
  await capture('13-weekly-report.png');
});
