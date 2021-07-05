## Architecture

### System

Here is a view of the interactions between the TNPR application and external components (web services, SDKs...):

![](README_assets/system.png)

For now, TNPR only uses one external component which is the Navitia web service.

Navitia provides two routes that TNPR uses:
1. Find places (`/places`). It is used by the [place suggestion feature](README.md#place-suggestion-feature).
2. Find journeys (`/journeys`). It is used by the [journey search feature](README.md#journey-search-feature).

### Dependencies

The dependencies of TNPR can be represented in two ways: 
- Abstract, where each dependency is labeled in a generic way, independant of the framework: "standard library", "type-safe HTTP client"... 
    This exact representation of the dependencies could apply to the same application not targeting Android, but instead a web browser or a desktop OS.
- Concrete, where each dependency is labeled specifically as the implementation that is used in TNPR: "standard libary" → `kotlin.stdlib`, "type-safe HTTP client" → `com.squareup.retrofit2`. 

| Concrete                                 | Abstract                                 |
|------------------------------------------|------------------------------------------|
| ![](README_assets/deps-concrete.png) | ![](README_assets/deps-abstract.png) |

Each diagram is split into high-level packages: Application, Network, UI, Runtime environment and Language.

Read on to learn about these packages.

#### Application (`afk`)

This package contains TNPR's source code and the app's author's internal code. 

`afk` is the name of a *custom framework* that contains classes that are reusable in any Android app.

TODO

#### TODO

### Features

![](README_assets/features.png)

TODO

### Layers

![](README_assets/layers.png)