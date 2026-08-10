# Changelog

## 0.1.0 - 2026-08-10

- Add a required, exact-target Mixin for MEGA Cells 4.11.0.
- Retain the stock bulk-cell decompression-pattern scan while skipping AE2
  global-provider refreshes when the resulting ordered pattern list is equal.
- Refresh once when removal of the last module clears a previously non-empty
  provider.
- Add an independent stock-mode switch, in-memory diagnostics, operator
  status/reset commands, compatibility guards, tests, CI, Releases, and Maven
  publication.
