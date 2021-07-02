## Architecture

### System

Here is a view of the interactions between the TNPR application and external components (web services, SDKs...):

![](README_assets/system.png)

For now, TNPR only uses one external component which is the Navitia web service.

Navitia provides two routes that TNPR uses:
1. Find places (`/places`). It is used by the [place suggestion feature](README.md#place-suggestion-feature).
2. Find journeys (`/journeys`). It is used by the [journey search feature](README.md#journey-search-feature).

### Dependencies

| Concrete                                 | Abstract                                 |
|------------------------------------------|------------------------------------------|
| ![](README_assets/deps-concrete.png) | ![](README_assets/deps-abstract.png) |

TODO

### Features

![](README_assets/features.png)

TODO

### Layers

![](README_assets/layers.png)