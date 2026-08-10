# All the Mons 1.2.0 compatibility and performance evidence

This report records the exact artifacts reviewed for release 0.1.0. No
third-party artifact is redistributed by this repository.

## Pack and dependency identities

| Component | Exact evidence |
| --- | --- |
| All the Mons | 1.2.0, pack tag commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Server archive | `ServerFiles-1.2.0.zip`, SHA-256 `de112ed8d79b3ff027e399a5108b706f6a2db3be74b15d0db6f6b9d6ac268e6c` |
| Minecraft / NeoForge | 1.21.1 / 21.1.248 |
| MEGA Cells distribution | CurseForge project `622112`, file `7952621`, `megacells-4.11.0.jar` |
| MEGA Cells JAR | SHA-256 `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55` |
| MEGA Cells source | tag `4.11.0`, commit `f46ab8f9a2c14f0de3e09241241b49fb2d06a81a` |
| AE2 | 19.2.17, JAR SHA-256 `460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95` |

The shipped MEGA Cells class was independently disassembled. Its erased
members and the `ICraftingService.refreshGlobalCraftingProvider` invocation
match the source tag. MEGA Cells' own Mixin configuration does not target
`DecompressionService`; the reviewed All the Mons core and existing local
performance add-ons do not target it either.

## Profile evidence

The supplied profile `reO1syIxkU.sparkprofile` has SHA-256
`f45b8cb37351db0ed17b4ceeed9d9db223765057e1ec4218d7d07f4a811abf1d`.
It covers 180.115 seconds, 3560 ticks, one sampled server thread, and two
players.

| Sampled path | Total | Approximate per tick |
| --- | ---: | ---: |
| `DecompressionService.onServerEndTick` | 3904 ms | 1.097 ms |
| Its AE2 global-provider refresh | 3792 ms | 1.065 ms |
| MEGA Cells pattern scan outside that refresh | about 60 ms | about 0.017 ms |
| Related AE2 crafting-service end-tick work | about 2642 ms | 0.742 ms |

The profile proves that redundant refresh work was expensive in that sample.
It does not prove the eventual saving because no equivalent post-install
profile existed when this release was built.

## Semantic audit

`DecompressionService.onServerEndTick` clears and rebuilds an ordered list of
`IPatternDetails` from each `BulkCellInventory`, then refreshes AE2 every tick
while at least one decompression module exists. `DecompressionPattern.equals`
compares its encoded definition. `BulkCellInventory` caches its immutable
decompression pattern list until the relevant cell configuration invalidates
it.

The optimizer snapshots the preceding ordered list, allows the stock scan to
run, and compares the completed list with `List.equals`. Any changed element,
count, or order refreshes conservatively. The Mixin redirects only the one
refresh call inside `onServerEndTick`; the separate priority setter remains
stock. With optimization disabled, the original call site is passed through
on every stock-eligible tick.

The stock method does not request a refresh when `installedModules` becomes
zero. The optimizer requests one only when that transition has cleared a
previously non-empty advertised list. This prevents stale cached crafting
patterns without creating a redundant refresh for an already-empty provider.

## Verification boundary

`./gradlew --no-daemon clean check build` compiled against the exact MEGA Cells
CurseForge file, applied the required server-side Mixin, loaded the entrypoint
and exact-version/signature guard in ModDevGradle's test environment, ran the
decision tests and Checkstyle, and verified a project-only production JAR.

A disposable extraction of the exact All the Mons 1.2.0 server archive was
then started with only the production JAR added. The full pack discovered the
add-on, the exact-version guard passed, the server reached `Done`,
`/atmonsmegacells status` executed successfully, and `stop` saved every
dimension and exited with status 0. The fresh world contained no live AE2 grid,
so the command correctly reported zero live services.

Neither this document nor the initial release claims a production-world test,
unmodified-client connection, live decompression-pattern transition matrix, or
controlled before/after Spark result.
