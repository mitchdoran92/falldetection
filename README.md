# ${1:Project Name}
The work produced throughout the year 2015 undertaking my Bachelor of Honours - Computer Science at Swinburne University is contained within this git repository.
##Project Description

##Research Abstract
Despite the vast research into automatic fall detection the bathroom still remains a black spot where falls are likely to occur undetected. A cheap easily accessible smartphone can monitor floor vibrations, detecting significant vibrations may indicate a fall has occurred. The baby boom has come to an end, populations are aging and birthrates declining. Along with the inevitability of age comes an increased risk of falling, the severity of injuries suffered from falls increase with age. Those worst affected by falls are those who live independently, for these people if a fall causes debilitating injuries they may face long laying periods, stuck immobile unable to call for help due to their injuries until someone finds them. Long laying periods compound injuries and add to the psychological trauma of the fall. Using the smartphone's tri-axial accelerometer to measure floor vibrations we hope to be able to detect falls as they occur and send out alerts in real time to reduce laying periods. Using the sum vector magnitude, the total acceleration across all three axis, and a threshold based algorithm we are able to classify vibrations caused by an event as falls if they surpass a dynamically calculated threshold. With careful calibration a single smartphone is able to determine whether measured vibrations come from an activity of daily living or a fall. Using a high pass filter over the accelerometer data helps to remove noise and increases the deviation between signal noise and significant vibrations. Synchronising multiple devices could help achieve more accurate and reliable classification of events and may be able to determine the location of the fall. This research is a first step into developing an automatic fall detection Android application which could help to save lives and reduce the burden of falls on societies.

##Repository Description
This repository contains the current state of my work produced throughout the year of my honours course. Some of this work was produced for assessment or presentation while other pieces, namely the Android Application source code, was never created with an audience in mind. Several documents may still contain comments or TODOs as reminders of the work that never quite got done.

## Android Application Installation
Installation notes for AndroidProject:

1. Download the source code and project files contained in /AndroidProject/
2. Launch Android Studio
3. Open the project folder through Android Studio
4. Gradle should download necessary libraries
5. Compile and build to run.

Works currently, November 2015, on both my Windows 8.1 machines, 64 bit.

## Android Application Usage
In its current state the application's primary function is to record and display the accelerometers sum vector magnitude within the LiveDetection Activity. With appropriate thresholds to remove noise and classify falls the application should display a mostly green plotted line graph. 

1. From the home activity the phone's primary function can be seen by pressing the Life Fall Detection button
2. Automatic calibration hasn't been implemented, the phone expects some input for threshold values to cut off noise and classify falls*
3. Prior to pressing Start Sensing it is essential the phone is already in position and won't be moved or touched after the Start Sensing button has been pressed.
4. After Start Sensing has been pressed the application will begin to continuously calibrate a new threshold value, points will be plotted based on deviations from this threshold value
4.1 Green
4.2 Yellow
4.3 Red

##Thesis Latex Source Code



* (suggested input values 3-4 and 7-8) 

## Contributing
1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## History
TODO: Write history

## Credits
TODO: Write credits

## License
TODO: Write license#
