# picture-mosaic
## Description
Create a picture mosaic by using several pictures. To specify the result an Image of your choice must be given and renamed accordingly.

## Requirements
* Java 1.8

Note: other versions of Java are untested but might work (or not).

## Installtion
TBD (Compile src into a runnable .jar works)

## Setup
Start **picture-mosaic** once to create the *resources* folder in your current directory. Once setup you should move some pictures into the *resources/Images* folder. Those will later end up creating the mosaic.
To specify how the mosaic should look like an Image file needs to be placed in the *resources* folder and named *Target* (note the missing of any file extension).

## Usage
Once setup you can simply run **picture-mosaic** again and it will create a mosaic with the given settings as specified in the *resources/settings.txt* file or the default settings when *resources/settings.txt* is not present.

## Settings
If no *settings.txt* is present a new one will be created while running. Should the value of *version* not correspond with the *versionString* in **picture-mosaic** it will refuse to load the settings and use the default ones instead.

A short list and description of the settings available will follow:
* version

Specifies the version of the settings file and helps **picture-mosaic** not load outdated settings.

* outputName

Base name all created mosaics will get when created. A "-" followed by a number may be appended to avoid name collisions.

* inputWorkerLimit

A numerical limit of how many threads are allowed to be spawned for loading images in the *resources/Images* folder and indexing them.

* targetWorkerLimit

A numerical limit of how many threads are allowed to be spawned for calculating the average colors of each spot in the grid on the *Target*.

* matchWorkerLimit

A numerical limit of how many threads are allowed to be spawned for matching average color values in the index against the average color values of each grid slot.

* placeWorkerLimit

A numerical limit of how many threads are allowed to be spawned for placing the images onto the canvas to create the mosaic.

* gridWidth

Numerical value of how many images are to be placed in a line to form the mosaic. More means a less "pixelated" result.

* gridHeight

Numerical value of how many images are to be placed in a collumn to form the mosaic. More means a less "pixelated" result.

* targetMultiplier

Numerical value of how big the resulting mosaic should be compared to the given *Target*. Higher means a greater end resolution which can lead to bigger memory usage. Only Integers (1, 2, 3 but not 1.86).

* alphaThreshhold

Numerical value under which alpha value (transparency) a pixel should be ignored for calculations. Less means more transparent pixels are used.

* adaptionCount

As projecting a grid with a specific dimension (e.g.: 3x3 onto a 10x10 Image) onto an Image can end up with slot sizes like 3.33 (which are not useable for Images), **picture-mosaic** will change the dimension of the grid (e.g. 3.33 to 3) and check if this dimension hasn't lost too much in size compared to the *Target* (see *gridErrorThresh*). If it has, *gridWidth* or *gridHeight* (depending on which shrinked too much) are multiplied with the *adaptionStep* and checked again.
This iteration will seize until either a suitable dimension has been found or the amount of steps that have been taken exceeds this value.

* adaptionStep

Numerical value to be multiplied with the *gridWidth* or *gridHeight* each *adaption step*. Accepts float values(e.g.: 1.1, 1.03, 3.12)

*gridErrorThreshhold

Numerical value for determining if the *gridWidth* and *gridHeight* have lost too much in their conversion to an Integer (e.g.: 3.33 to 3 = 0.33 lost). Expects values between from 0.0 to under 1.0 (e.g.: 0.15 or 0.35).

* keepRatio

Boolean value (**true** or **false**) if the used Images (in *resources/Images*) should try to keep their Aspect ratio when resized. If this is **false** *overlapImages* has no effect.

* overlapImages

Boolean value (**true** or **false**) if the Images are allowed to overlap over each other and therefore prevent "holes" that would appear with no overlapping Images and keeping their ratio. The topmost Image is influenced by the last placed images by the *PlacementWorker*s.
