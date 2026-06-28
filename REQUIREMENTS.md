# REQUIREMENTS

This artifact extends **Defects4J 3.0.1** and runs on **macOS** and **Linux**. A
self-contained **Docker image** is provided (see `Dockerfile` and the *Run with Docker*
section of `README.md`) so that reviewers do not need to install the toolchain by hand.

## Hardware

- **Architecture:** x86_64 (amd64). Also runs on Apple Silicon (arm64) — natively for the
  host tools, and via emulation for the provided `linux/amd64` Docker image.
- **Memory:** ≥ 8 GB RAM recommended (mutation analysis is memory-intensive).
- **Disk:** a few GB free. `init.sh` downloads the Defects4J project repositories and the
  Major / EvoSuite / Randoop / Gradle dependencies; the built Docker image is several GB.
- **Network:** required during the build / `init.sh` step (downloads project repos and
  tools). Not required afterwards to run the bundled example.

## Software

All of the following are preinstalled in the Docker image. For a native install they must
be present on the host (pages 2–3 of `doc.pdf` list exact versions):

- **Java 11** (required by Defects4J 3.0.1)
- **Perl** + CPAN modules (DBI, DBD::CSV, JSON, JSON::Parse, XML::Parser, List::Util,
  String::Interpolate, URI, …; see `cpanfile`)
- **Python 3** (standard library only; no third-party packages needed)
- **Subversion ≥ 1.8**, **git**, **jq**, **unzip**, **curl** / **wget**
- **Ant** is bundled via Major — no separate installation needed

## Operating system

- macOS and Linux are supported.
- Windows is **not** supported natively; use WSL2 or the provided Docker image.

## Reference environments and expected runtime

The experiments in the paper were run on a **Mac mini (Apple M4)**, a **MacBook Pro
(2021, Apple M1 Pro)**, and a **Linux x86_64** machine (Intel Core i7-950, 4c/8t). The
single **Cli-2** example (`defects4j get_project Cli 2b` → `full_state_analysis.sh`) takes
about **13 minutes** on the M1 Pro; it is longer in CI and under amd64 emulation.
