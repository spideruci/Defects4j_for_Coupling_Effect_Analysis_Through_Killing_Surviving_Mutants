# 📄 Artifact Documentation

This repository contains the artifact accompanying our ISSTA 2026 paper submission.
Our artifact extends **Defects4J version 3.0.1**  https://github.com/rjust/defects4j/tree/v3.0.1
Which contains the necessary code to generate fault-revealing augmentations from real bugs and surviving mutants.


## Table of Contents

- [Artifact Description](#artifact-description)
- [Table of Real Bugs Detectable Through Assertion Augmentation](#table-of-real-bugs-detectble-through-assertion-augmentation)
- [Code](#code)
  - [How to Get Started](#how-to-get-started)
  - [Source Code](#source-code)
- [Mutation Analysis](#mutation-analysis)
- [Real Bug Detection](#real-bug-detection)
- [Results](#results)
- [Reproducibility](#reproducibility)
- [Threats to Validity](#threats-to-validity)
- [License](#license)


# Artifact Description

This repository contains the multifaceted artifacts associated with our paper, including both the dataset and the supporting code.
The contents of this repository are briefly introduced in this README.
Detailed information—such as the experimental setup and the new commands we added to Defects4J to support our analysis—is provided in the accompanying documentation.




👉 [Open Artifact Documentation (doc.pdf)](doc.pdf)

# Table of Real Bugs Detectble Through Assertion Augmentation

This table lists all 104 program versions and bug IDs for which we successfully generate test assertions in originally passing covering tests, enabling them to detect the corresponding real bugs as triggering tests.
For these program versions, we perform mutation analysis and targeted mutant-killing activities.


| Subject | Bug IDs |
|--------|--------|
| Cli | 1, 2, 4, 10, 16, 21, 22, 26, 31, 34, 36 |
| Math | 6, 13, 14, 23, 24, 33, 44, 57, 58, 62, 64, 66, 68, 74, 76, 81, 84 |
| Jsoup | 3, 25, 31, 32, 50, 56, 62, 63, 72, 76, 77, 87 |
| Lang | 6, 32, 56 |
| Csv | 10, 16 |
| Chart | 1, 3, 7, 8, 16, 20 |
| Codec | 4, 14, 16 |
| Collections | 17, 21 |
| Gson | 2, 3, 8 |
| JacksonCore | 2, 3, 10, 12, 15, 16, 22, 26 |
| JacksonXml | 2 |
| JxPath | 4 |
| Compress | 2, 3, 4, 9, 22, 25, 34, 35, 39 |
| Time | 1, 2, 6, 19, 22, 23 |
| JacksonDatabind | 2, 11, 12, 18, 22, 23, 24, 30, 31, 37, 44, 53, 59, 63, 78, 87, 92, 103, 111, 112 |


# Code
The analysis and full experimental pipeline are implemented and streamlined through tight integration with Defects4J.  
To support our analysis, we introduce **nine new Defects4J commands**, several of which are useful beyond this study.  
For example, the command `defects4j patch [-b|-f]` switches the current program version between the buggy and fixed variants.  
All nine commands, along with their intended workflows and usage examples, are described on pages 4–9 of the accompanying documentation ([doc.pdf](doc.pdf)).

## How to Get Started with the Experiment

Our implementation supports experiments on **Mac mini (M4)**, **MacBook Pro (2021, Apple M1 Pro)**, and **Linux x86_64 systems** equipped with an **Intel Core i7-950 CPU** (4 cores / 8 threads, 3.07 GHz).  
We therefore expect the implementation to run on most **macOS** and **Linux-based** machines.

The first section of the accompanying documentation ([doc.pdf](doc.pdf)) describes how to set up the environment, install dependencies, and run the full analysis for a given program version.  
In particular, pages 2–3 list the required dependencies and their versions for configuring Defects4J.

Pages 3–4 provide a step-by-step example demonstrating how to run the experiment for **Cli-2**, which takes approximately **13 minutes** on a **MacBook Pro (2021, M1 Pro)**.

## Source Code

The direct customization is in the current repositories, with the main new and customized defects4j command customization under framework/bin. 
Specifically, the coverage related to each bugs are pre-computed under "coverages" directory. 
There are some other utilities directories that are used for the experiments, including "mutation_testing_utils", "mutation_testing_utils_shaded",and "state_utils".

For the jar file named state_utils/folder_utils/transform.jar, it is used to generate concrete assertion code from assertion specifications, which is introduced in Section 4 in [doc.pdf](doc.pdf).
Their source-code implementation is included in SourceCodeInstrumentation folder.

For the jar file named state_utils/folder_utils/o.jar, it is used to instrument test code to observe program states. Their source-code implementation is included in observerForSpecification folder.

















The complete artifact documentation is provided in **doc.pdf**, which describes the design and implementation of our framework, explains how to run the experiments, and highlights important methodological considerations and limitations.

👉 [Open Artifact Documentation (doc.pdf)](doc.pdf)

The remainder of this README focuses on the structure and contents of the dataset repository.




Program versions where we successfully generate assertions on passing covering tests that detects the real bugs.





Generated Assertions for Real Bugs
List of Accepted Assertions 


Generated Assertions from Mutants
List of Accepted Assertions
List of meal-bug detecting mutant-based Assertions








Folder "" for Source Code for test code instrumentation for state collection
Folder "" for Source Code for test code assertion generation

Extended 9 defects4j commands and their corresponding code stay in, including ......

dataset -> 
generated real-bug detecting assertion options
generated mutant-killing assertions options
statistics of if mutant-killing assertions detect real bugs.

Analysis Scripts




Analyzing Scripts
Runtimes
