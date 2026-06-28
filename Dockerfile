# syntax=docker/dockerfile:1
#
# Self-contained image for the Defects4J coupling-effect / assertion-generation
# artifact. It mirrors the proven-green CI environment (Java 11 + Subversion +
# Perl/CPAN + Python 3 + jq; Ant ships bundled via Major, so no system Ant/Maven
# is needed) and bakes in `init.sh`, which downloads the project repositories,
# EvoSuite, Randoop, Gradle deps, and build-analyzer. NOTE: the Major mutation
# framework is NOT downloaded — it is vendored in the repo (major/) and copied
# into the image by `COPY .`, so major/ must never be added to .dockerignore.
# The entrypoint runs the assertion-generation pipeline on a Defects4J bug
# (default: Cli-2) and validates the generated outputs.
#
# Build:  docker build -t coupling-effect:cli-2 .
# Run:    docker run --rm coupling-effect:cli-2            # runs Cli 2b
#         docker run --rm coupling-effect:cli-2 Lang 11b   # any bug with a coverages/ file
#         docker run --rm -it coupling-effect:cli-2 bash   # interactive shell

FROM eclipse-temurin:11-jdk-jammy

# Defects4J + Perl/Python toolchain expects a UTF-8 locale; avoids spurious
# Perl "Setting locale failed" warnings during the pipeline.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    DEBIAN_FRONTEND=noninteractive \
    DEFECTS4J_HOME=/opt/defects4j

# ---------------------------------------------------------------------------
# System dependencies (same set as the CI workflow, plus libexpat1-dev which
# XML::Parser needs to compile against a clean base image).
# ---------------------------------------------------------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
        git \
        perl \
        cpanminus \
        build-essential \
        libexpat1-dev \
        jq \
        unzip \
        wget \
        curl \
        ca-certificates \
        subversion \
        python3 \
        python3-pip \
    && rm -rf /var/lib/apt/lists/* \
    && svn --version --quiet

# ---------------------------------------------------------------------------
# Perl / CPAN modules required by the Defects4J framework. Copying cpanfile
# first keeps this heavy layer cached unless dependencies actually change.
# ---------------------------------------------------------------------------
COPY cpanfile ${DEFECTS4J_HOME}/cpanfile
WORKDIR ${DEFECTS4J_HOME}
RUN cpanm --notest \
        DBI \
        DBD::CSV \
        JSON \
        JSON::PP \
        JSON::Parse \
        XML::Parser \
        XML::Parser::PerlSAX \
        List::Util \
        File::Basename \
        File::Path \
        File::Copy \
        Getopt::Long \
        Time::HiRes \
        String::Interpolate \
        URI \
        Proc::ProcessTable \
    && cpanm --notest --installdeps .

# Avoid git "dubious ownership" failures and give Defects4J's checkout a git
# identity to use when it initializes per-bug working repositories.
RUN git config --global --add safe.directory '*' \
    && git config --global user.email "ci@defects4j.local" \
    && git config --global user.name  "Defects4J CI" \
    && git config --global init.defaultBranch master

# ---------------------------------------------------------------------------
# Bring in the artifact and initialize Defects4J (downloads project repos,
# Major, EvoSuite, Randoop, Gradle deps, build-analyzer). Requires network.
# ---------------------------------------------------------------------------
COPY . ${DEFECTS4J_HOME}
RUN chmod +x ./init.sh && ./init.sh

# Put Defects4J (and the custom commands) on PATH. Make the framework probe
# load-bearing (`&&`, not `;`) so a broken Perl/CPAN setup fails the build here
# rather than shipping a broken image.
ENV PATH=${DEFECTS4J_HOME}/framework/bin:${PATH}
RUN which defects4j && defects4j info -p Cli >/dev/null

# ---------------------------------------------------------------------------
# Entrypoint: run the assertion-generation example for a given bug.
# ---------------------------------------------------------------------------
COPY docker/run-example.sh /usr/local/bin/run-example.sh
RUN chmod +x /usr/local/bin/run-example.sh

# Per-bug analysis happens in a writable workspace, not the read-only artifact.
WORKDIR /work
CMD ["run-example.sh", "Cli", "2b"]
