# STATUS

## Badge requested: **Artifacts Available**

We apply for the **Artifacts Available** badge for the artifact accompanying the paper
*"How Does Killing Surviving Mutants Help Detect Real Bugs? A Controlled Experiment with
Assertion Generation"* (ISSTA 2026).

## Justification

The artifact meets the requirements for the *Available* badge:

- **Publicly archived with a DOI.** The complete artifact is deposited on **Zenodo**, a
  public archival repository that provides permanent storage and a citable DOI:
  **https://doi.org/10.5281/zenodo.XXXXXXXX** (DOI: `10.5281/zenodo.XXXXXXXX`).
  <!-- TODO(authors): replace XXXXXXXX with the real Zenodo DOI after archiving. -->
- **Author-created and relevant to the paper.** It contains the full implementation of
  our targeted, fault-based assertion-augmentation technique and the controlled
  mutant-killing experiment, implemented as an extension of **Defects4J 3.0.1**, together
  with the generated assertions and per-bug data reported in the paper.
- **Permanent and immutable.** The Zenodo record is a versioned snapshot that remains
  accessible independently of the source-code hosting.
- **Openly licensed.** The artifact is released under the MIT License (see `LICENSE`),
  permitting inspection, reuse, and redistribution.

Reviewers should access the artifact through the **Zenodo DOI** above, which provides public,
non-IP-tracking access (satisfying the reviewer-anonymity requirement). The GitHub repository is
a non-authoritative development mirror. The Zenodo DOI is reserved before publishing and embedded
in this record, so the archived snapshot self-references its own citable DOI.

We apply for *Available* only. The artifact is nonetheless documented and runnable — see
`README.md` (Getting Started and Step-by-step instructions) and `REQUIREMENTS.md` — should
reviewers wish to exercise it.

## Data availability statement (for the camera-ready paper)

Add the following to the paper, citing the final DOI (replace `XXXXXXXX`):

> *Data Availability.* The artifact — the implementation, the controlled-experiment pipeline,
> and all generated assertions and per-bug data — is publicly available on Zenodo:
> https://doi.org/10.5281/zenodo.XXXXXXXX.

Ensure this DOI matches the one entered in HotCRP.
