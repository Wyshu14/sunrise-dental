# Pushing this project to GitHub (Task D)

This repository was created and committed locally (9 logical commits — see
`git log`). It has **not** been pushed anywhere yet, since this was built
in a sandbox with no access to your GitHub account. Follow these steps on
your own machine, in IntelliJ, to finish Task D.

## 1. Create the GitHub repository

1. Go to https://github.com/new
2. Repository name: `sunrise-dental-appointment-system` (or your choice)
3. Visibility: **Public** (the brief requires this)
4. Do **NOT** tick "Add a README" / "Add .gitignore" / "Add license" —
   this project already has all of those; ticking them creates conflicting
   files on GitHub's side before you've pushed anything.
5. Click **Create repository**. Copy the URL it gives you, e.g.
   `https://github.com/<your-username>/sunrise-dental-appointment-system.git`

## 2. Push the existing local repository

Open this project's folder in a terminal (or IntelliJ's built-in terminal)
and run:

```bash
git remote add origin https://github.com/<your-username>/sunrise-dental-appointment-system.git
git push -u origin main
```

If prompted for credentials, GitHub no longer accepts your account
password for `git push` — use a Personal Access Token
(GitHub → Settings → Developer settings → Personal access tokens) as the
password, or set up the GitHub CLI (`gh auth login`) / SSH keys instead.

## 3. Confirm the commit history looks right

Refresh the GitHub repository page — you should see 9 commits under
"Insights → Commits", each with a descriptive message, not one giant
"Initial commit".

## 4. Keep committing over the following days (important for marks)

The marking criteria explicitly reward "several versions ... updated and
deployed with changes" over time, not a single upload. The most honest and
highest-scoring way to satisfy this is to actually keep developing:

- Run the project against a real MySQL database (see README) and commit
  any fixes that surfaces (there will likely be at least one - see
  `docs/dev-notes.md`, "Known limitation").
- Add the `@Tag("integration")` tests against live MySQL mentioned in
  `docs/test-plan.md`.
- Try the app end-to-end yourself, note anything you'd improve, and commit
  each improvement separately with its own message.
- Use a short-lived feature branch + pull request for at least one change,
  and merge it — this alone demonstrates a real Git *workflow*, not just
  commits on `main`, which is explicitly called out in the top marking
  band ("Workflow (CI/CD) demonstrated, along with the deployment of
  changes").

Each of these, done on a different day and pushed, is exactly the kind of
incremental, dated history the brief is asking you to demonstrate — and
unlike a backdated commit history, it is both genuine and easy to defend
if you're asked about it.

## 5. GitHub Actions (CI)

`.github/workflows/ci.yml` runs automatically once you push — check the
**Actions** tab on GitHub after your first push to confirm it goes green
(`mvn test`). If it fails, that is useful, real signal — fix it and commit
the fix; a red-then-green Actions run in your history is itself good
evidence of CI being used properly, not just present.

## 6. Add the report link

Once you have a submission-ready report (PDF), add its link (or the
document itself) to this repository, and reference the repository URL in
the report, per the brief's "Share the report link within the
documentation" instruction.
