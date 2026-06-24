# Option C Adaptation — CHANGES

Adapts the Celvo KMP client to the backend per-assignment data model (Option C) for `/api/v1/esims/home`, `/api/v1/esims/{iccid}/bundles`, and `/api/v1/esims/{iccid}/packages`. `/api/v1/esims/my-esims` is untouched.

---

## Phase 1 — File inventory

New:

- `core/domain/src/commonMain/kotlin/com/mtislab/core/domain/model/AssignmentId.kt`

Modified (data layer):

- `feature/store/src/commonMain/kotlin/com/mtislab/celvo/feature/store/data/dto/EsimHomeItemDto.kt`
- `feature/store/src/commonMain/kotlin/com/mtislab/celvo/feature/store/data/mapper/EsimHomeMapper.kt`
- `feature/myesim/src/commonMain/kotlin/com/mtislab/celvo/feature/myesim/data/dto/EsimBundlesDto.kt`
- `feature/myesim/src/commonMain/kotlin/com/mtislab/celvo/feature/myesim/data/mapper/BundleMapper.kt`

Modified (domain layer):

- `core/domain/src/commonMain/kotlin/com/mtislab/core/domain/model/ActiveEsimHome.kt`
- `feature/myesim/src/commonMain/kotlin/com/mtislab/celvo/feature/myesim/domain/model/EsimBundleInfo.kt`

Modified (presentation layer):

- `feature/store/src/commonMain/kotlin/com/mtislab/celvo/feature/store/presentation/store/StoreScreen.kt`
- `feature/myesim/src/commonMain/kotlin/com/mtislab/celvo/feature/myesim/presentation/details/EsimDetails.kt`

Modified (resources):

- `feature/store/src/commonMain/composeResources/values/strings.xml`
- `feature/myesim/src/commonMain/composeResources/values/strings.xml`

Total: 11 files (1 new, 10 modified). Zero files under `feature/myesim/.../MyEsimList*` or the `my-esims` remote service were edited.

---

## Phase 3 — List-key migrations

Two call sites — the two places where same-SKU rows can coexist:

1. `feature/store/src/commonMain/kotlin/com/mtislab/celvo/feature/store/presentation/store/StoreScreen.kt:530`
   `HorizontalPager( key = { page -> packages[page].assignmentId.raw }, … )`
   Home carousel. Previously keyed by `bundleName` → two active `ULTRA-1GB-30D` rows collapsed to one page.

2. `feature/myesim/src/commonMain/kotlin/com/mtislab/celvo/feature/myesim/presentation/details/EsimDetails.kt:187`
   `items(allBundles, key = { it.assignmentId.raw })`
   Bundle Details LazyColumn (active + queued + history). Previously `it.assignmentId ?: it.bundleName` — still collapsed on legacy responses.

No other list renders over `EsimHomePackage` or `EsimBundle`. Verified by grep: no residual `key = { … bundleName … }` or `key = { … it.id … }` over these domain types.

---

## Phase 4 — Stacking UX

### Chosen pattern: "Next up" chip (Apple Wallet style)

- **Home carousel** — the pager page keeps rendering the top-of-queue active bundle unchanged. Queued same-SKU siblings surface as a small rounded chip overlaid at `Alignment.TopCenter` of each page, reading *"Next up"* (position 1) or *"#N in queue"* (position ≥ 2). The chip is suppressed when `queuePosition` is null or 0 (active).
- **Bundle Details** — every bundle row always renders. Queued rows get a one-line subtitle directly under the country name (*"Next up"* / *"#N in queue"*). No extra headers, no new sections.

The active bundle's gauge, CTAs, and gestures are unchanged. Stacking is expressed as secondary affordance on existing cells.

### Rejected alternatives

- **Stripe-style timeline / roadmap row above the gauge.** Would add a new component class, demand its own spacing/typography tokens, and visually compete with the gauge. Rejected — Home's top surface is already dense.
- **Card-stack offset (peek the sibling underneath the active page).** Compelling visually, but HorizontalPager with `pageSize = PageSize.Fill` does not natively express "stacked siblings of the same SKU" without custom layout math. Introducing `beyondBoundsPageCount` + offset transforms changes motion behaviour — motion is a frozen design-system axis.
- **Grayscale-fill the gauge when queued.** Misleading: the user's data isn't "half-gray," it's "not yet active." Conflates two different states (usage vs. lifecycle) in one channel.
- **Separate "Queued" section in Bundle Details.** Would double the vertical scroll and split same-SKU rows apart from the active one. Chose the inline subtitle because it preserves purchased-order grouping.

The chosen pattern costs 3 short string resources, 2 new private composables (`QueueIndicatorChip`, `QueueSubtitle`), and zero new design tokens.

---

## Phase 5 — Null-safe gauge

`EsimHomePackage.remainingBytes` / `usedBytes` / `usagePercent` became nullable at the DTO and domain layers for Option C. Previously the null branch of the gauge passed `usedAmount = 0f` — which rendered *"0 GB / 20 GB, 0%"* verbatim. That is the zero substitution the spec explicitly forbids.

Fixed by replacing the null-gauge branch in `StoreScreen.kt` with `PackageGaugeSyncing` — a small composable showing an indeterminate `CircularProgressIndicator` + the `gauge_syncing` label (*"Syncing…"*). No layout shift: it occupies the same bounding box as the gauge.

---

## Phase 6 — Summary fields

`BundleSummaryDto` and `EsimBundleSummary` gained:

- `totalDataRemainingBytes`, `totalDataRemainingFormatted`
- `activeRemainingBytes`, `activeRemainingFormatted` (nullable: null when no active bundle)
- `nextExpiryAt`

Carried cleanly through mapper → domain. No existing presentation code computed `totalPurchased − totalUsed` for a header, so there is no client-side computation to rip out — the spec's intent ("use server-computed remaining") is already satisfied the moment the field is available.

---

## Proof of theme fidelity

Every token used in new UI code, mapped to its existing theme source. No new design-system entries introduced.

### `QueueIndicatorChip` (Home carousel overlay)

| Property            | Token used                             | Source                                          |
| ------------------- | -------------------------------------- | ----------------------------------------------- |
| Background          | `extended.inputBackground`             | `CelvoTheme` — `ExtendedColors.inputBackground` |
| Text colour         | `extended.textSecondary`               | `CelvoTheme` — `ExtendedColors.textSecondary`   |
| Corner radius       | `RoundedCornerShape(999.dp)`           | Compose primitive (pill/capsule pattern)        |
| Horizontal padding  | `12.dp`                                | Matches existing small-chip padding in repo     |
| Vertical padding    | `4.dp`                                 | Matches existing small-chip padding in repo     |
| Typography          | `MaterialTheme.typography.labelSmall`  | `CelvoTheme` — typography scale                 |

### `QueueSubtitle` (Bundle Details row subtitle)

| Property     | Token used                              | Source                                        |
| ------------ | --------------------------------------- | --------------------------------------------- |
| Text colour  | `extended.textSecondary`                | `CelvoTheme` — `ExtendedColors.textSecondary` |
| Typography   | `MaterialTheme.typography.labelSmall`   | `CelvoTheme` — typography scale               |
| Top spacing  | `Spacer(Modifier.height(2.dp))`         | Matches neighbour row spacing                 |

### `PackageGaugeSyncing` (null-safe skeleton)

| Property          | Token used                                | Source                                           |
| ----------------- | ----------------------------------------- | ------------------------------------------------ |
| Spinner colour    | `MaterialTheme.colorScheme.primary`       | Material3 scheme (already used by other spinners in the app) |
| Label colour      | `extended.textSecondary`                  | `CelvoTheme` — `ExtendedColors.textSecondary`   |
| Label typography  | `MaterialTheme.typography.bodySmall`      | `CelvoTheme` — typography scale                 |
| Container size    | Matches gauge's `Modifier.size(…)`        | Reuses gauge layout envelope — zero layout shift |

No new colours were added. No new typography styles. No new shapes. No new motion curves. The chip's 999.dp corner is a pill primitive (full capsule radius) — not a new shape definition.

---

## Localised strings

Three new keys across two locale files each:

| Key               | EN              | KA             |
| ----------------- | --------------- | -------------- |
| `queue_next_up`   | "Next up"       | ⚠ pending      |
| `queue_nth`       | "#%1$d in queue"| ⚠ pending      |
| `gauge_syncing`   | "Syncing…"      | "სინქრონიზაცია…" (pre-existing) |

Within the ≤ 5 string budget. `queue_next_up` and `queue_nth` are present in `values/` (English) for both `feature/store` and `feature/myesim`; they are **not** yet present in `values-ka/` (Georgian) — see Open Questions.

---

## `/my-esims` regression check

Grepped for `my-esims|MyEsimRemoteService|MyEsimList` — 8 files. Cross-referenced against the 11 files in Phase 1's inventory: zero overlap. The MyEsimList flow, its view-model, remote service, and resources were not touched. The Option C shape is additive on the three targeted endpoints; `/my-esims` response parsing is independently typed and unaffected.
