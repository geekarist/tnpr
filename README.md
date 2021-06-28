# TNPR

TNPR is an public transport Android application. It allows to plan a journey in France near Paris, from a point A to a point B.

It uses [Navitia](https://navitia.io), which is "the open API for building cool stuff with transport data".

## Installation

- Open [github.com/geekarist/tnpr/releases](https://github.com/geekarist/tnpr/releases) _on your Android device_
- Look at the "Assets" of the latest release
- Download the `.apk` file
- Open the downloaded file
- Confirm the installation

## Usage

When you open the application, the initial screen lets you choose an origin and a destination for your journey. 

<img src="README_assets/1-choose-orig.png" height="400" />

Press the "Origin" button to choose an origin.

The next screen will suggest you a place based on any text you type: an address, a point of interest, etc. Start typing an address, e.g. "117 boulevard voltaire".

<img src="README_assets/2-suggest-origin.png" height="400px" />

Choose the first suggestion, which is in Paris, then you're back to the initial screen.

<img src="README_assets/3-choose-dest.png" height="400px" />

Before searching for journeys, you have to choose a destination so press the "Destination" button.

Type "cnit" in the next screen and press the first suggestion (CNIT is a business center near Paris).

<img src="README_assets/4-suggest-dest.png" height="400px" />

Back again to the initial screen, but now you can press the button with the 🔍 icon to find journeys from your origin to your destination.

<img src="README_assets/5-search-journeys.png" height="400px" />

Press the 🔍 button.

The next screen shows you the best journeys from your origin to your destination. The first item is the most "interesting" one because it has transfers. 

<img src="README_assets/6-choose-journey.png" height="400px" />

Press the first item.

The last screen displays your journey in detail. You see each section of it, including when you have to wait or transfer from a transport mode to another. 

<img src="README_assets/7-sections.png" height="400px" />

## Architecture

### System

![](README_assets/SystemArch.png)

TODO

### Dependencies

| Concrete | Abstract |
|---|---|
|![](README_assets/DepsArch-Concrete.png)|![](README_assets/DepsArch-Abstract.png)|

TODO

### Features

TODO

### Layers

TODO

## Contributing

TODO

## LICENSE

See [COPYING](COPYING).
