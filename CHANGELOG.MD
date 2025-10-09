## [1.0.9]
- add dark/light mode.  
- fix blurry painting images rendered on the screen.  
- fix datapack output to include colors and decent painting names.  
- add creator credit field for datapack export

## [1.0.8]
- Add 1.21 option.  
- Add underlying datapack exporter to accommodate 1.21+ changes to minecraft's internal workings with paintings.

## [1.0.7]
- Add progress bar when loading images
- Add auto-detector for painting sizes. (it uses the image size to autofill the size fields)
- Base window is now 960 pixels wide instead of 720 to better fit the changes in the UI
- Fixes
- Fix error cards being out of whack
- Image loading improvement : threads are now used and greatly improve loading times

## [1.0.6]  
- Expand available painting sizes.
- Picking a size now fills the size input fields, rather then having two different inputs.
- Add dropdown menu to pick a minecraft version so the output resourcepack is compatible with it.
- Dropdown menu will disable/enable the 'unique id' text input for the compatible versions.
- Fixes
- Fix export button sabotaging the import button functionality

## [1.0.4]
- Add remove button to none-errored cards
- M ove card layout and logic hooks to fxml files
- Improve modularity of cards
- Rework internal error system
- Rework internal error-card communication
- Fix issue where card would reset when naming a valid card
- Improve handling of removing cards

## [1.0.3]
- fix custom size width and height fields outputting 0x0 for painting sizes
