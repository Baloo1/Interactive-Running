# Interactive Running

Application done in the context of the course TRA105, Digitalization In Sports - Interactive Running at Chalmers

It was developed by Johannes Mattsson & Frida Nordlund

## Requirements

### To run the app

- Can be run on any Android phone with atleast Android 5.1 installed.  

- Requires an IMU present on the device with an accelerometer, not all androids have one.

### To develop

- IDE for Android development

- Gradle 6.5

- Android Gradle 4.1.1

- JDK 14.0.2

It is possible that lower versions are compatible as well. But it has not been tested. Nothing specific from these versions are used.

## Handbook - How to use

Details about the app and how to use it

### Use cases

Use cases for the app. Strikethrough indicates partial or planned but not yet supported.

- As a user I want to enter metrics about myself to improve the running metrics

- As a user I want to track my running cadence, stride length and ground contact time from running

- ~As a user I want to check my performance while running~

- ~As a user I want to compare my running data to others of similar metrics~

- As a developer I want to be able to debug the sensors

- As a developer I want to be able to debug the calculations

### Features & improvements

Here the different use cases is illustrated.

The entry point for the app is the welcome screen

*insert welcome screen*

From here you can only move forward. The next image depicts the metrics use case, where the user can enter their metrics. It is not yet supported to save this metrics and to use these metrics
to allow the user to compare themselves to other users.

*insert user data screen*

If you fail to enter your speed here the app will crash. This could be improved with a check forcing the user to enter all fields.
The input fields are not the optimal for best UX for the specific input. Height should be sanitized on input and provide easier input than a free text input.
Age should be dropdowns, possibly opening a calender as help. The anonymus grey dropdowns should get styling and values.
Exersice field reqires more knowledge about the users to define good values, but someting along the lines of 1-2 times a week, every day etc is a good starting point.
Goal time would also need tuning towards either specific routes/stretches/races or be more generic. Speed would need clarification as it is not clear what it means for the user.
It should most likely be moved to the actual running screen and get some more instructions.
Also the general contrast between fields and background could be improved.

The confirm button takes you to the next screen.

*insert running view*

From here you can track your cadence, stride length and ground contact time. This is done by pressing the start button. After 3 seconds the tracking will start and after about 800 microseconds 
it will start showing the values in the UI. To stop tracking press stop. It will then finalize and show the last values in the UI.

During the entire tracking each value read from the sensor will be saved to a file to allow debugging. This is done every 800 microseconds as well. The performance cost by writing all the time is motivated by debugging needs. For a real world application this writing would not happen this often. 

It is possible for you to see your values by running but to do so you cannot have your phone at the hip or somewhere that provides good measurements. If you have it in your hand it will give inaccureate values. So it is possible to do this but not really. In the future sending the metrics to a screen would be preferable. Either at a scren in front of the treadmill or to a smartwatch.

After pressing stop the plan was to be able to move on to statistics and compre yourself to others according to the earlier entered metrics. This is not implemented.

## Code structure

The app is structured into XML views in the reosources folder, java files as fragments and activity that handle logic, sensor readings, propagation to calculations, and view updates.
The calculations is done from the InteractiveRunning class. The main entrypoint is MainActivity. Utils containt debugging helper for forcing screen to be on and file writers. Observer pattern has been used to decouple views somewhat from the logic. 

Checkstyles and findbugs have been used to ensure good code quality.
