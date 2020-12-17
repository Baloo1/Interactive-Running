# Interactive Running

Application is done in the context of the course TRA105, Digitalization In Sports - Interactive Running at Chalmers

Developed by Johannes Mattsson & Frida Nordlund

## Requirements

### To run the app

- Runs on any Android phone with at least Android 5.1 installed.

- Requires an IMU present on the device with an accelerometer, not all androids have one.

### To develop

- IDE for Android development

- Gradle 6.5

- Android Gradle 4.1.1

- JDK 14.0.2

Lower versions may be compatible as well. But it has not been tested. Nothing specific from these versions are used.

## Handbook - How to use

Details about the app and how to use it

### Use cases

Use cases for the app. Strikethrough indicates partial or planned but not yet supported.

- As a user, I want to enter metrics about myself to improve the running metrics

- As a user, I want to track my running cadence, stride length and ground contact time from running

- ~As a user, I want to check my performance while running~

- ~As a user, I want to compare my running data to others of similar metrics~

- As a developer, I want to be able to debug the sensors

- As a developer, I want to be able to debug the calculations

### Features & improvements

Here we illustrate the different use cases.

The entry point for the app is the welcome screen

<img src="https://github.com/Baloo1/Interactive-Running/blob/main/examples/Interactive%20Running%20Example%201.jpg" width="320" height="693">

_Welcome Screen_


From here you can only move forward. The next image depicts the metrics use case, where the user can enter their metrics. It is not yet supported to save these metrics and to use these metrics
to allow the user to compare themselves to other users.

<img src="https://github.com/Baloo1/Interactive-Running/blob/main/examples/Interactive%20Running%20Example%202.jpg" width="320" height="693">

_User Metrics Screen_


If you fail to enter your speed here the app will crash. This could be improved with a check forcing the user to enter all fields.
The input fields are not optimal for the best UX for the specific input. Height should be sanitized on input and provide easier input than free text input.
Age should be dropdowns, opening a calender as help. The anonymous grey dropdowns should get styling and values.
Exercise field requires more knowledge about the users to define good values, but something as in 1-2 times a week, every day etc is a good starting point.
Goal time would also need tuning towards either specific routes/stretches/races or be more generic. Speed would need clarification as it is not clear what it means for the user.
Moving to the actual running screen and get some more instructions would be an improvement.
Also, the general contrast between fields and background could be improved.

The confirm button takes you to the next screen.

<img src="https://github.com/Baloo1/Interactive-Running/blob/main/examples/Interactive%20Running%20Example%203.jpg" width="320" height="693">

_Running Screen_


From here you can track your cadence, stride length and ground contact time. This is done by pressing the start button. After 3 seconds the tracking will start and after about 800 microseconds it will start showing the values in the UI. To stop tracking press stop. It will then finish and show the last values in the UI.

During the entire tracking, each value read from the sensor will be saved to a file to allow debugging. This is done every 800 microseconds as well. The performance cost by writing all the time is motivated by debugging needs. For a real-world application, this writing would not happen this often.

It is possible for you to see your values by running but to do so you cannot have your phone at the hip or somewhere that provides good measurements. If you have it in your hand it will give inaccurate values. So it is possible to do this but not really useful. In the future sending the metrics to a screen would be preferable. Either at a screen in front of the treadmill or to a smartwatch.

After pressing stop the plan was to be able to move on to statistics and compare yourself to others according to the earlier entered metrics. This is not implemented.

## Code structure

The app is structured into XML views in the resources folder, java files as fragments and activity that handle logic, sensor readings, propagation to calculations, and view updates.
The calculations are done from the InteractiveRunning class. The main entry point is MainActivity. Utils contains debugging helper for forcing the screen to be on and file writers. Observer pattern has been used to decouple views somewhat from the logic.

Checkstyles and findbugs/spotbugs have been used to ensure good code quality.

