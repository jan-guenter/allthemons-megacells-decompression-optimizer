# All the Mons MEGA Cells Decompression Optimizer

A dedicated-server-only add-on that removes redundant AE2 crafting-provider
rebuilds caused by MEGA Cells' decompression-pattern service. It is narrowly
targeted at the hot path observed in the server's Spark profile; it does not
change bulk-cell storage, compression ratios, item contents, recipes, or
crafting results.

## Compatibility

Version `0.1.0` is intentionally strict:

| Component | Supported version |
| --- | --- |
| All the Mons | 1.2.0 |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 through the 21.1 line |
| Applied Energistics 2 | 19.2.17 |
| MEGA Cells | 4.11.0 |
| Java | 21 |

The mod is installed only on the dedicated server. Matching All the Mons
clients remain unmodified.

## What it changes

MEGA Cells 4.11.0 scans its bulk cells for decompression patterns at the end
of every server tick. It then asks AE2 to refresh the entire global crafting
provider even when the new pattern list is equal to the previous tick's list.
That request also causes AE2 to rebuild its craftable-pattern set.

This add-on keeps MEGA Cells' own scan and pattern objects. It compares the
completed list with the previous tick and calls AE2's original refresh method
only when the list actually changes. It also performs one clearing refresh
when the last decompression module is removed and a non-empty pattern list was
previously advertised.

Changes that trigger a refresh include insertion or removal of a bulk cell,
filter/compression/cutoff changes that alter its decompression patterns, and
pattern-order changes. MEGA Cells' separate pattern-priority setter continues
to refresh immediately and is not intercepted.

## Install and configure

1. Stop the server.
2. Put the production JAR in the server's `mods` directory.
3. Start the server and check the log for the exact-version guard message.
4. Run `/atmonsmegacells status` as an operator.

NeoForge writes the server config to
`<world>/serverconfig/atmons_megacells_optimizer-server.toml`.

| Setting | Default | Meaning |
| --- | --- | --- |
| `decompressionPatterns.changeAwareRefreshEnabled` | `true` | Skip AE2 refreshes when the scanned pattern list is unchanged. Set to `false` for stock MEGA Cells refresh scheduling. |
| `diagnostics.diagnosticsEnabled` | `true` | Periodically log per-service counters. |
| `diagnostics.diagnosticsLogIntervalTicks` | `1200` | Diagnostic log interval; 1200 ticks is about one minute at 20 TPS. |

The config is independent of world data. Removing the JAR and restarting
restores stock behavior.

## Diagnostics

`/atmonsmegacells status` requires permission level 2 and reports live service
count, scans, module/pattern counts, performed and skipped refreshes, the
module-removal clearing fix, stock pass-through calls, and refresh timing.

`/atmonsmegacells reset` requires permission level 4 and clears in-memory
counters. Neither command scans additional cells or writes world state.

## Performance evidence and limitations

The motivating 180-second Spark profile sampled
`DecompressionService.onServerEndTick` for about 3.9 seconds total, or roughly
1.10 ms per tick. About 3.79 seconds was below AE2's
`refreshGlobalCraftingProvider`; MEGA Cells' actual bulk-cell scan was only
about 60 ms total. The related AE2 crafting-service rebuild was about
0.74 ms per tick. Avoiding unchanged refreshes may therefore recover roughly
1.6-1.8 ms from a comparable active tick, but that is a profile-based estimate,
not a measured before/after result.

The build gate verifies the exact target signatures, required Mixin call site,
decision behavior, server-side mod loading, Checkstyle, and production-JAR
contents. See [`docs/compatibility-1.2.0.md`](docs/compatibility-1.2.0.md) for
artifact identities and the precise verification boundary.

The initial release also passed a disposable full-pack fresh-world smoke with
the exact All the Mons 1.2.0 server archive: the add-on and guard loaded, the
server reached `Done`, `/atmonsmegacells status` ran, and the server stopped
and saved cleanly. That empty test world had no live AE2 grid.

Before production rollout, exercise decompression crafting with module and
cell insertion/removal, compression-card and cutoff changes, priority changes,
restart, grid split/merge, and an unmodified client. Take controlled before
and after Spark profiles under comparable load. No production-world or
unmodified-client test is claimed by this release documentation.

## Building

Use Java 21:

```bash
./gradlew --no-daemon clean check build
```

The production JAR is in `build/libs`. A sources JAR is generated for Maven
consumers but is not a server mod artifact.

## Releases and Maven

Release tags must exactly match `v<mod_version>`. GitHub Actions rebuilds,
tests, publishes the Maven module, creates checksums, and creates the GitHub
Release.

```text
io.github.jan-guenter:allthemons-megacells-decompression-optimizer:0.1.0
```

GitHub Packages repository:

```text
https://maven.pkg.github.com/jan-guenter/allthemons-megacells-decompression-optimizer
```

GitHub Packages consumers may need GitHub credentials even for public
packages.

## License

The add-on's original code is MIT licensed. MEGA Cells, Applied Energistics 2,
Minecraft, NeoForge, and the mod pack remain separate projects under their
own licenses and are not redistributed here.
