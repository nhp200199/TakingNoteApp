# TakingNoteApp
## Introduction
This is an Andrdoid app that I made based on the instructions of a Pluralsight course. The main purpose of the app is to allow you to take notes from the courses you took.
## Contents of the app
 Here is what included in the app and also the things that I learned from it. Each commit reflects each lession. **Please note that there are somethings I didn't commit to Github directly**.
1. **Understanding Android Application Basics**
   - -Get to know some basic Android components (Activity, UI, Intent)
   - -Learn about activity's lifecycle
   - -Perform restoring activity state using ViewModel
2. **Working with Android Tools and Testing**
   - -Familiar with Android studio
   - -Using debug mode
   - -Learn basis of JVM, Intrumental testing
3. **Enhancing the Android Application Experience**
   - -Using more Android components (RecyclerView, CardView, MenuOptions, Navigation Drawer)
   - -Using Drawable and Values resources
   - -Customizing App Apperrance with Styles and Themes
   - -Create a Settings screen
4. **Managing Android App Data with SQLite**
   - -Understand what is SQLite
   - -Create a SQLite database 
   - -Design table for the notes, courses
   - -Perform operation on the database (query, update, add, delete)
   - -Perform main thread protection when doing with the database by using CursorLoader and AsyncTask
5. **Exposing Data and Infomation Outside Your Android app**
   - -In this lesson, the app expands to use Content Provider to not directly access to SQLite. The Content Provider has all the operation I can do to interact with the database from it.
   - -Content Provider provides a standard way for other apps to get data from my application.
   - -Also, the lession contains section about how to create notifications, set styles and interact with it.
6. **Leveraging the Power of the Android platform**
   - -Here we learn about the android threading model, how to protect the main thread from doing too much work (by using StrictMode, AsycTask, Handler).
   - -Another (and better) way to do background work is to use Service (IntentService, JobScheduler) because it can do work even if the app is killed.
   - -Broadcast Receivers: a way that your app can receive broadcast from system events or receive from / send to other apps.
   - -Alarm Manager: used to schedule time-sensitive task (often used in conjunction with broadcast receivers).
7. **Broadening Android App Appeal and Reach**
## Beyonds the course
In addition to the things that is wrapped in the course, here is things that I plan to do (there is some I did, some is just a plan that to archieve):
1. Complete the things left on the course's app
   
