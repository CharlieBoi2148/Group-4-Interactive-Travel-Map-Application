**Interactive Travel Map Application**

Software Requirements Specification (SRS)

 

**Project Team Members:**

Wilson Buhendwa

Gage Buckley

Charles Burbury

 

Software Engineering — Group 4

1. Introduction  
   1. Purpose  
   2. Scope  
   3. Intended Audience  
2. Overall Description  
   1. Document Perspective  
   2. Document Functions  
   3. User Characteristics  
   4. End-User Operating Environment  
   5.  Design and Implementation Constraints  
3. Functional Requirements  
   1. FR1: Create Travel Pin  
   2. FR2: Edit Travel Pin  
   3. FR3: Delete Travel Pin  
   4. FR4: View Pins and Trips  
   5. FR5: Organize Trip  
   6. FR6: Visualize Map  
   7. FR7: Upload Media  
   8. FR8: Calculate Distances  
   9. FR9: Filter and Search  
   10. FR10: View Timeline  
   11. FR11: Set Pin Privacy  
   12. FR12: Generate Share Link  
   13. FR13: View Travel Statistics  
   14. FR14: Manage User Account  
   15. FR15: Persist User Data  
4. Non-Functional Requirements  
   1. NFR1: Usability  
   2. NFR2: Performance  
   3. NFR3: Reliability  
   4. NFR4: Security  
   5. NFR5: Portability  
   6. NFR6: Maintainability  
5. References

1. **Introduction**  
   1. Purpose  
      This document will outline the functional and non-functional requirements for the interactive Travel Map system as agreed upon by all developers and clients. This document is intended for both system users and developers.  
        
   2. Scope  
      This SRS covers the requirements of the Interactive Travel Map Application, a web-based platform that enables users to document, organize, and share their travel experiences through a visual geographic interface. This document specifies what the system must do and the criteria for success, not how it will be implemented.  
        
   3. Intended Audience  
      The intended audience for this application is travelers who wish to maintain a structured visual record of their personal journeys.  
        
2. **Overall description**  
   1. Document Perspective  
      The software specification document describes the functional and non-functional requirements of an interactive travel map-based application. For functional requirements, the document outlines essential tasks and requirements, including travel pin management, trip organization, map & visualization, search & analytics, and user account & system management. The document also describes non-functional requirements.  
        
   2. Document Functions  
      This document provides the client with a basic guide on how the user will operate the software. The developers are provided with a basic roadmap for software development. 

   3. User Characteristics  
      The application is designed with average web users in mind. So, the user is expected to have basic knowledge on how to operate a web browser and interact with web-based applications. No advanced technical skills are required.

   4. End-user operating Environment  
      The application will be a web-based platform that can be used by anyone with network connectivity to access core functionalities. Users can access the application through a web browser.  
        
   5. Design and Implementation Constraints  
      The user must have a device with an active network connection to use the application. The application requires integration with a third-party geographic mapping API to provide map data and location services.

3. **Functional Requirements:**  
1. **FR1: Create Travel Pin**

   	Use Case 1

1. Name: 	Create Travel Pin  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: User is logged in  
5. Main Flow:  
1. User selects a location on the map or clicks “Add Pin”  
2. User enters a location name, country, region, visit date.  
3. System captures coordinates  
4. User may add notes and optional media  
5. User clicks “Save”

		    vi. Post Conditions: New travel pin is created, stored, and displayed on the map

2. **FR2: Edit Travel Pin**

   Use Case 2

1. Name: 	Edit Travel Pin  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: At least one pin exists and user is logged in  
5. Main Flow:  
1. User selects an existing pin  
2. User modifies location details, notes, date, media, or settings  
3. User clicks “Save Changes”  
4. Updated information is reflected within 1 second  
6. Post Conditions: The selected travel pin is updated successfully.

3. **FR3: Delete Travel Pin**

   	Use Case 3

1. Name: 	Delete Travel Pin  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: At least one pin exists and user is logged in  
5. Main Flow:  
1. User selects a pin  
2. User clicks “Delete”  
3. System prompts for confirmation  
4. Pin is removed within 1 second  
6. Post Conditions: The pin and associated metadata are permanently deleted.

   

4. **FR4: View Pins and Trips**

   Use Case 4

1. Name: 	View Pins and Trips  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: User is logged in  
5. Main Flow:  
1. User opens dashboard or map view  
2. System loads all user trips and associated pins  
3. Pins and trips display within 2 seconds  
6. Post Conditions: User can view all previously created trips and pins.

   

   

   

   

   

   

   

5. **FR5: Organize Trip**

   Use Case 5

1. Name: 	Organize Trip  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: At least one pin exists  
5. Main Flow:  
1. User creates a new trip  
2. User enters trip name, description, start/end dates  
3. User assigns pins to the trip  
4. System sorts pins chronologically by visit date  
6. Post Conditions: Pins are organized under a specific trip.

   

6. **FR6: Visualize Map**

   	Use Case 6

1. Name: 	Visualize Map  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: User is logged in  
5. Main Flow:  
1. User accesses the interactive map  
2. System renders map using geographic API  
3. User pins are displayed as markers  
4. Routes between pins may be visualized  
6. Post Conditions: The map displays accurate geographic locations of user pins.

   	

   

   

 


7. **FR7: Upload Media**

   Use Case 7

1. Name: 	Upload Media  
2. Priority: Essential   
3. Actor: User   
4. Preconditions: Creating or editing a pin  
5. Main Flow:  
   1. User selects “Upload Media”  
      2. User selects photo, video, or audio file  
      3. System uploads file and associates it with the pin  
      4. Media preview is displayed within 2 seconds  
6. Post Conditions: Media file is securely stored and linked to the selected pin.

   

8. **FR8: Calculate Distances**

   	Use Case 8

1. Name: 	Calculate Distances  
2. Priority: Essential   
3. Actor: System  
4. Preconditions: At least two pins exist and are associated with a trip  
5. Main Flow:  
   1. System calculates distance between selected pins  
      2. System calculates total trip distance  
      3. System calculates total accumulated travel distance  
6. Post Conditions: Distance values are displayed in user’s selected measurement unit.

   

   

   

9. **FR9: Filter and Search**

   	Use Case 9

1. Name: Filter and Search  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in and at least one pin or trip exists  
5. Main Flow:  
   1. User applies date range filter  
      2. User selects trip filter  
      3. User enters keyword search  
      4. System displays filtered results within 2 seconds  
6. Post Conditions: Only matching pins and trips are displayed.

   

10. **FR10: View Timeline**

    Use Case 10

1. Name: View Timeline  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in and at least one pin exists  
5. Main Flow:  
   1. User selects “Timeline View”  
      2. System sorts all pins chronologically by visit date and time  
      3. Timeline loads within 2 seconds  
6. Post Conditions: User can view travel history in chronological order.

11. **FR11: Set Pin Privacy**

    Use Case 11

1. Name: Set Privacy  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in and at least one pin or trip exists  
5. Main Flow:  
   1. User selects privacy setting (Private, Friends-only, Public)  
      2. System updates access control rules  
      3. Changes apply immediately  
6. Post Conditions: Pin visibility follows selected privacy setting.

   

12. **FR12: Generate Share Link**

    Use Case 12

1. Name: Generate Share Link  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in, and the selected pin or trip is set to public or friends-only  
5. Main Flow:  
   1. User selects a public or friends-only pin/trip  
      2. User clicks “Generate Link”  
      3. System generates unique shareable URL  
      4. Link becomes active within 1 second  
6. Post Conditions: Authorized users can access shared content via link.

   

   

   

   

13. **FR13: View Travel Statistics**

    Use Case 13

1. Name: View Travel Statistics  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in and at least one pin exists  
5. Main Flow:  
   1. User navigates to Statistics Dashboard  
      2. System calculates total countries visited  
      3. System calculates total cities/states visited  
      4. System calculates total distance traveled  
      5. Statistics load within 2 seconds  
6. Post Conditions: Travel insights are displayed accurately.

   

14. **FR14: Manage User Account**

    Use Case 14

1. Name: Manage User Account  
2. Priority: Essential   
3. Actor: User  
4. Preconditions: User is logged in and has an existing account  
5. Main Flow:  
   1. User updates name, profile picture, home location, or measurement preference  
      2. User saves changes  
      3. System updates account within 1 second  
6. Post Conditions: User profile information is updated.

   

   

   

15. **FR15: Persist User Data**

    Use Case 15

1. Name: Persist User Data  
2. Priority: Essential   
3. Actor: System  
4. Preconditions: User has previously created an account and saved data exists  
5. Main Flow:  
1. User logs out or closes application  
2. User logs back in  
3. System reloads all pins, trips, statistics, and media within 3 seconds  
6. Post Conditions: All user data is preserved across sessions.

4. **Non-Functional requirements**  
1. Usability  
   1. This application should be user-friendly and intuitive to use.  
   2. A new user of this application should be able to successfully create a pin within 2 minutes of launching the application for the first time.  
2. Performance  
   1. The application should respond to user input within 2.5 seconds or less and display information quickly across all supported browsers and devices.  
   2. Creating a pin should take no more than 3 seconds, with visual confirmation.   
3. Reliability  
   1. The application will operate without frequently crashing or losing data during normal usage.  
   2. The application will maintain an uptime of at least 99% monthly.   
4. Security  
   1. Pins and trips may only be edited by their owner or those with edit permissions.  
   2. User accounts shall be protected with a username and a password.  
5. Portability  
   1. The application will be functional across all major modern web browsers.  
   2. The application will function correctly across both desktop and mobile devices.  
6. Maintainability  
   1. The application’s software will be easy to debug and maintain for future updates.  
   2. The software shall be structured to allow individual components to be updated or replaced without affecting the rest of the system

   **5\.  References**

   OpenStreetMap — [https://www.openstreetmap.org](https://www.openstreetmap.org) 

   

		

