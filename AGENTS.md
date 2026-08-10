# Agent guide: All the Mons MEGA Cells Decompression Optimizer

This is the independent public project
`jan-guenter/allthemons-megacells-decompression-optimizer`.

| Property | Value |
| --- | --- |
| Mod ID | `atmons_megacells_optimizer` |
| Maven coordinates | `io.github.jan-guenter:allthemons-megacells-decompression-optimizer` |
| Current version | `0.1.0` |
| Java / Gradle | 21 / wrapper 8.10.2 |
| License for original add-on code | MIT |

## Invariants

- Dedicated-server-only deployment must continue to support an unmodified
  matching client.
- Register no content, recipes, menus, payloads, packet channels, or client
  hooks. Persist no world, grid, cell, item, or block-entity state.
- Never copy, shade, patch in place, or redistribute MEGA Cells, AE2,
  Minecraft, NeoForge, or mod-pack artifacts.
- Retain MEGA Cells' own bulk-cell scan and its pattern instances. Do not
  infer patterns from storage contents or recreate decompression recipes.
- Compare the completed ordered pattern list conservatively. A changed count,
  element, or order must invoke AE2's original refresh method.
- Do not intercept `setPatternPriority`; its immediate stock refresh is part
  of the behavior contract.
- Removing the last module must clear a previously non-empty provider once.
- `changeAwareRefreshEnabled=false` must pass through the original refresh
  call on every stock-eligible tick.
- Every Mixin injection/redirect remains required. Unsupported signatures or
  versions fail startup rather than silently disabling the optimization.
- Do not claim production-world, unmodified-client, crafting-matrix, or Spark
  verification unless it was actually run and recorded.

## Authoritative evidence

Read [`docs/compatibility-1.2.0.md`](docs/compatibility-1.2.0.md) before
changing compatibility claims. The exact current identities are:

- All the Mons 1.2.0, commit `c7bb230f21d14d26859d0b92548f089b3a493ad9`;
- NeoForge 21.1.248 and Minecraft 1.21.1;
- AE2 19.2.17;
- MEGA Cells CurseForge `622112:7952621`, version 4.11.0, JAR SHA-256
  `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55`;
- MEGA Cells source tag 4.11.0, commit
  `f46ab8f9a2c14f0de3e09241241b49fb2d06a81a`; and
- motivating Spark profile SHA-256
  `f45b8cb37351db0ed17b4ceeed9d9db223765057e1ec4218d7d07f4a811abf1d`.

## Exact patch surface

The target is `gripe._90.megacells.misc.DecompressionService`:

```text
public <init>(IGrid, ICraftingService)V
public onServerEndTick()V
public getAvailablePatterns()Ljava/util/List;
public setPatternPriority(int, IGridNode)V
private final patterns:Ljava/util/List;
private final grid:Lappeng/api/networking/IGrid;
private installedModules:I
```

The required redirect target occurs exactly once inside `onServerEndTick`:

```text
ICraftingService.refreshGlobalCraftingProvider(ICraftingProvider)V
```

At method head the Mixin copies the previous patterns. MEGA Cells then clears
and rebuilds its live list. The redirect passes through in stock mode, invokes
the stock AE2 call on inequality, or records a skip on equality. At return,
the no-call `installedModules == 0` path performs one refresh if a previously
non-empty list was cleared. All diagnostics are memory-only and weakly keyed
by the live service.

## Build and verification

Run:

```bash
./gradlew --no-daemon clean check build
```

The gate includes pure decision tests, an exact reflection-signature test,
ModDevGradle server-side loading, required Mixin application, Checkstyle, and
`verifyProductionJar`. Independently inspect the production JAR and metadata.
The release gate also includes a disposable full-pack fresh-world smoke using
the exact All the Mons 1.2.0 server archive: confirm mod discovery, the guard,
the diagnostics command, a `Done` state, and a clean stop. This does not replace
live-grid behavior tests, an unmodified-client check, or a controlled Spark
comparison.

For a runtime gate, use a disposable extraction of the exact server archive,
add only the production JAR, accept the EULA only in that disposable copy,
start a fresh world, confirm the guard and command, and stop cleanly. Do not
touch a production world. Exercise real decompression crafting and an
unmodified client separately.

## Release procedure

1. Update code, tests, README, AGENTS, compatibility report, and changelog.
2. Increase stable SemVer `mod_version`.
3. Run the complete gate and inspect the JAR.
4. Commit and merge through a pull request for post-initial releases; version
   policy requires a greater version.
5. Create annotated tag `v<mod_version>` at the reviewed main commit.
6. Verify the release workflow, production JAR/checksum assets, and Maven
   package coordinates and bytes.
7. Update the private root workspace gitlink and exact release identity.

## Updating to a new All the Mons version

A pack update is a new compatibility investigation, not a metadata bump.

1. Resolve the exact pack tag/commit, server archive, Minecraft, NeoForge,
   AE2, MEGA Cells, and relevant add-on file identities and hashes.
2. Diff the complete mod ledger, configs, KubeJS, datapacks, and server scripts.
3. Obtain the exact MEGA Cells and AE2 binaries and exact correlated sources.
   Inspect their metadata, Mixin configs, target bytecode, and changelogs.
4. Re-read `DecompressionService`, `BulkCellInventory`, `CompressionChain`,
   `DecompressionPattern`, AE2 `CraftingService`, and the global-provider
   cache contract. Confirm equality, invalidation, and priority semantics.
5. Scan every installed Mixin configuration for the target service and audit
   other mods that register or wrap global crafting providers.
6. Test modules 0/1/many; empty/non-empty cells; cell insertion/removal and
   replacement; filter, compression card, cutoff, and chain changes; priority;
   grid split/merge; chunk reload; restart; and multiple grids.
7. Change strict dependency ranges and reflection/Mixin targets only after
   evidence review. Fail loudly on changed assumptions.
8. Run local tests, full-pack dedicated-server startup, real crafting tests,
   an unmodified-client connection, and controlled before/after Spark profiles.
9. Record only observed results, update all documentation, release a new
   semantic version, and pin it in the private workspace.
