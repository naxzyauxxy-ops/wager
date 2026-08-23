# HavocCasino

Casino games for **Purpur / Paper 1.21.x** — an animated slot machine, a mines
board, and a progressive jackpot, playable with **money** (via Vault) or built-in
**rubies**.

## Features

- `/slots <bet> [money|rubies]` — animated 3-reel slot GUI with weighted symbols and payouts.
- `/mines <bet> [mines] [money|rubies]` — reveal tiles on a 5x5 board; the multiplier climbs with each safe tile, cash out any time before hitting a mine.
- `/jackpot [bet]` — progressive pool; each entry feeds the pot and rolls for the whole thing.
- `/havoccasino` (`/hc`) — admin: reload config, manage the pool, grant/take rubies.
- Vault is a **soft dependency**: without it, rubies still work.
- MiniMessage-styled output, configurable bets, weights, odds and prefix.
- **Customizable messages** in `messages.yml` with placeholders (internal `{tokens}` + PlaceholderAPI).
- **Per-player message toggle** — each player turns HavocCasino messages on/off for themselves via a green (ON) / red (OFF) button (`/hc messages`).

## Requirements

- Java 21
- Purpur or Paper 1.21.x
- (Optional) Vault + an economy plugin for `money`
- (Optional) PlaceholderAPI for `%...%` placeholders in messages

## Building

The build is wired to compile **and obfuscate** in one step (ProGuard runs in the
`package` phase and rewrites the jar in place).

```bash
mvn clean package
```

The finished plugin lands at:

```
target/HavocCasino-1.0.0.jar
```

Drop that jar into your server's `plugins/` folder.

### CI build

`.github/workflows/build.yml` builds the obfuscated jar on every push and uploads
it as a workflow artifact (`HavocCasino-plugin`). Push this repo to GitHub and grab
the jar from the **Actions** tab — no local Maven needed.

## Obfuscation notes

`proguard.conf` renames and repackages classes, flattens the package hierarchy,
strips source-file names and runs several optimization passes, so decompiled output
is messy and hard to follow. What's deliberately kept readable (because the server
needs it): the main class named in `plugin.yml`, `@EventHandler` methods, enum
`values()/valueOf()`, and the GUI holder type.

Honest caveat: no tool makes JVM bytecode *impossible* to decompile. ProGuard raises
the effort a lot, but a determined person can still recover logic. If you need to run
after obfuscation and something misbehaves, loosen the aggressive options
(`-overloadaggressively` is intentionally omitted for that reason) and re-test.

## Messages & placeholders

All player-facing game text lives in `messages.yml` and is fully editable. Templates
support MiniMessage formatting and two kinds of placeholders:

- **Internal tokens** filled by the plugin: `{amount}`, `{multiplier}`, `{player}`,
  `{pool}`, `{chance}`.
- **PlaceholderAPI** `%...%` placeholders (when PlaceholderAPI is installed), including
  this plugin's own expansion:
  - `%havoccasino_rubies%` — the player's ruby balance
  - `%havoccasino_jackpot%` — formatted jackpot pool (`%havoccasino_jackpot_raw%` for the number)
  - `%havoccasino_messages%` — `ON` / `OFF` for that player

### Turning messages on/off (client-side, per player)

Each player controls whether they receive HavocCasino messages:

- `/hc messages` — opens a settings screen with a **green ON** / **red OFF** toggle button; click to flip.
- `/hc messages on` / `/hc messages off` — quick toggle without the GUI.

The preference is saved per player in `settings.yml` and defaults to ON. Jackpot
broadcasts and game results respect each player's choice individually.

## Config quick reference

- `currency.default` — `money` or `rubies` when a player omits the currency.
- `betting.min-bet` / `max-bet` — slot bet bounds.
- `slots.two-match-multiplier` — payout when two reels match.
- `mines.default-mines` / `min-mines` / `max-mines` — mine count bounds on the 5x5 board.
- `mines.house-edge` — fraction shaved off the fair cash-out multiplier.
- `jackpot.currency` — currency the pool is tracked in.
- `jackpot.seed` — pool value after a win.
- `jackpot.contribution-percent` — fraction of each entry added to the pool.
- `jackpot.win-chance` — 0.0–1.0 chance an entry wins the pool.
- `jackpot.min-entry` — minimum entry amount.

## Permissions

| Permission           | Default | Grants                    |
|----------------------|---------|---------------------------|
| `havoccasino.slots`  | true    | `/slots`                  |
| `havoccasino.mines`  | true    | `/mines`                  |
| `havoccasino.jackpot`| true    | `/jackpot`                |
| `havoccasino.messages`| true   | `/hc messages` (self)     |
| `havoccasino.admin`  | op      | `/havoccasino` admin cmds |
