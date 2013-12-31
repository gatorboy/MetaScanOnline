MetaScanOnline
==============


This document describes the Metascan Online Android Application features and usage. Metascan Online is a user interface on Android to scan the files and folders on the Android phone with the Metascan Online Public API. (https://www.metascan-online.com/en/public-api)

Features Implemented:
All the key features have been implemented as per the requirements. They are
•	Scan individual files
•	Scan multiple files and folders.
•	System scan.
•	Auto scan new/modified files in the background.

Development:
This application is developed on the Android Studio 0.3.2 and tested on Samsung Galaxy Tab 2 and Samsung Galaxy S3.

Design:
Metascan Online app is designed using the latest features of Android and supports the Android operating systems from Android 3.0.x - API level 11 (HONEYCOMB) to Android 4.4 - API level 19 (KITKAT).

The app contains swipe-able tabs 

1.	SETTINGS
2.	RESULTS
3.	DIRECTORY



SETTINGS: 
---------
Settings tab contains all the preferences that can be updated. 
API Key: User can set the API key by clicking on the textview under the Metascan API Key textview. It changes to edit text so that user can edit the API key.

Auto Scan: All the new/modified files are automatically scanned by the app, if this option is set. 

Notifications: Auto scan notifications are displayed by popping as a notification. The sound and vibrate features can be turned on/off here.

Notifications can be cleared or cancelled. If the notifications are cleared, they are deleted. If the notifications are canceled, they still exist in the system and re-appear next time a threat is detected.
It navigates to the Metascan Online scan results page if any of the threat is clicked.



DIRECTORY:
----------
Directory tab shows the directory structure of the file system. The feature implemented are 

  Folder Structure:  It displays all the files and folders with appropriate icons.
  
  Individual File Scan: A single file can be selected to scan.
  
  Multiple File Scan: User can select multiple files to add to the scan queue. A group of files and folders can also be selected at various locations to scan.
  
  A fully selected folder is indicated by a star highlighted in blue.
  A partially selected folder (only few files are selected in the folder) is indicated by a star partially highlighted in blue.
  All other files and folders with non-highlighted stars indicate that they are not selected for scan.
  
  Clear All: To make the de-selection process easy, there is a text “Clear All” to clear all the selected files in the current directory.
  Back: User can navigate to previous folder by clicking on the button indicated by the back arrow.
  System Scan: System scan button selects all the files, directories and sub-directories recursively available in the file-system and performs scan.
                
RESULTS:
---------
Results tab shows the scan results of all the files that are scanned. User can cancel the scan that is in-progress at any time by clicking on the Cancel button.
Each item shows the file name, status of the scan and percentage completed.
It navigates to the Metascan Online scan results page if any of the result is clicked.
         

