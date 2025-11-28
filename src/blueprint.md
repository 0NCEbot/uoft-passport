# Project Specification for Group TUT0301-08 

## Team Name: UofT Passport

## Domain: Campus Exploration and Mapping

A UofT campus exploration app where students can check-in to landmarks across campus. UofT Passport encourages exploration and engagement with the university environment, allowing users to check in at points of interest, plan routes, and track their progress.  
---

# User Stories

1. As a user, I want to browse a list of UofT landmarks so that I can see what places I can visit and learn more about each one.  
2. As a user, I want to “check in” to a location so that my visit is recorded in my passport.  
3. As a user, I want to view my overall progress (e.g., number of landmarks visited, a filled passport, a pinned map) so that I can track my exploration achievements.  
4. As a user, I want to plan a route that visits multiple landmarks so that I can efficiently explore several locations in one trip.  
5. As a user, I want to leave private notes on places I’ve visited so that I can remember experiences or tips.  
6. As a user, I want to see all my past visits and undo a visit.  
7. As a user, I want to filter the list of landmarks by different parameters (number of visits, landmark type)  
8. As a user, I want to be able to edit or delete my note on a place I’ve visited.  
9. As a user, I want to login/register to use the program.

---

# Use Cases

## Use Case 1: Browse Landmarks

### Main Flow

* User opens the *Browse Landmarks* section.  
* System retrieves a list of landmarks from the local dataset.  
* Landmarks are displayed in both list and map views.  
* User selects a landmark to view details (name, description, image, location).

### Alternative Flows

* User filters landmarks by type or location → system updates the displayed list.

## Use Case 2: Check In to a Location

### Main Flow

* User selects a landmark from the list or map.  
* System displays details and a “Check In” option.  
* User clicks “Check In.”  
* System records the visit (time, date, and landmark name) in the user’s profile.  
* System updates the passport to mark the location as visited.

### Alternative Flows

* Duplicate check-in attempt → system notifies user that landmark is already visited.  
* User cancels check-in → system closes the landmark window without saving.  
* Undo visit →  
  * User selects a previously visited landmark.  
  * System displays an option to “Undo Visit.”  
  * User confirms, and the system removes the visit record from their profile.  
  * System updates the passport and statistics accordingly.

## Use Case 3: View Exploration Progress

### Main Flow

* User navigates to the *My Progress* section.  
* System retrieves user data on visited landmarks.  
* System displays:  
  * Total landmarks visited vs. available  
  * Colour-coded map or list showing visited locations  
  * Optional passport graphic displaying stamps for completed visits

### Alternative Flows

* No visits yet → system shows a friendly message encouraging first check-in.

## Use Case 4: Plan A Route

### Main Flow

* User chooses *Plan a Route*.  
  System prompts for a start point, optional intermediate landmarks, and a destination.  
* System uses Google Maps API (walking mode) to retrieve route details and estimated distance/time.  
* Route is displayed with ordered steps and a list of landmarks.  
* User may start the route and check off each landmark as they go.  
* Upon completion, the system displays total time and distance.

### Alternative Flows

* Invalid or unrecognized location → system requests valid input.  
* API unavailable → system offers manual (self-guided) route mode.

## Use Case 5: Add Personal Note

### Main Flow

* User selects a visited landmark from their passport or list.  
* System displays landmark details, including a notes section.  
* User writes a new note.  
* System saves the note to the user’s data file, associating it with that landmark.  
* System confirms the note has been saved.

### Alternative Flows

* User cancels note editing → system discards unsaved changes.

## Use Case 6: View Visit History

### Main Flow

* User navigates to the Visit History section from the main menu or the progress screen.  
* System retrieves the user’s visit records from storage (e.g., JSON file or database).  
  System displays a list of landmarks that have been visited.  
* User selects a landmark to view all recorded visit instances.  
* System displays each visit instance with details of date and time of visit.

### Alternative Flows

* Undo visit →  
  * User selects a specific visit instance to remove.  
  * System prompts for confirmation (e.g., “Are you sure you want to remove this visit?”).  
  * Upon confirmation, system deletes that visit entry from the user’s data.  
  * System updates relevant statistics and progress views.  
  * System displays a confirmation message (e.g., “Visit removed successfully”).

---

# MVP

| Lead  | Use Case | User Story |
| :---- | :---- | :---- |
| Darren | Browse landmarks | 1 |
| Nathan | Check into a location  | 2 |
| Kitas | View exploration progress | 3 |
| Dan | Plan a route | 4 |
| Joey | Add personal note | 5 |
| Steven | View visit history | 6 |

Login/register is to be implemented with no particular lead, due to our prior experience with this in M3 Lab 1\.  
Filter landmarks, edit/delete note is planned for later development, if time allows

# Proposed Entities for the Domain

UserProfile  
\- userId: String  
\- username: String  
\- createdAt: Instant  
\- visitedLandmarks: List\<Visit\>  
\- privateNotes: List\<Note\>

Landmark  
\- landmarkId: String  
\- name: String  
\- type: LandmarkType  
\- description: String  
\- address: String  
\- latitude: Double  
\- longitude: Double  
\- imageUrl: String  
\- openHours: String  
\- totalVisitCount: Int

Visit  
\- visitId: String  
\- landmark: Landmark  
\- visitedAt: Instant

Note  
\- noteId: String  
\- landmark: Landmark  
\- content: String  
\- createdAt: Instant  
\- updatedAt: Instant

RoutePlan  
\- routeId: String  
\- start: GeoPoint  
\- waypoints: List\<GeoPoint\>  
\- destination: GeoPoint  
\- travelMode: TravelMode  
\- estimatedTimeMin: Int  
\- estimatedDistanceM: Int  
\- polylineEncoded: String  
\- steps: List\<RouteStep\>  
\- createdAt: Instant  
\- completedAt: Instant

RouteStep  
\- index: Int  
\- instruction: String  
\- distanceM: Int  
\- durationSec: Int

RouteStepList  
\- index: Int  
\- instruction: String  
\- distanceM: Int  
\- durationSec: Int

GeoPoint  
\- lat: Double  
\- lng: Double  
\- label: String

Passport  
\- user: UserProfile  
\- stamps: Set\<Landmark\>  
\- completionPercent: Double

ProgressSummary  
\- visitedCount: Int  
\- totalLandmarks: Int  
\- completionPercent: Double  
\- lastVisitedAt: Instant

Enums  
\- LandmarkType { LIBRARY, CAFE, STUDY\_SPOT, LANDMARK, OTHER }  
\- TravelMode { WALK, BIKE, TRANSIT, DRIVE }  
\- RouteStatus { PLANNED, IN\_PROGRESS, COMPLETED }

Uses Relationships  
UserProfile uses Visit, Note  
Visit uses Landmark  
Note uses Landmark  
RoutePlan uses GeoPoint, RouteStep  
ProgressSummary uses Visit, Landmark  
Landmark uses Visit

# Proposed APIs

Google Maps Platform

- [Maps Static API](https://developers.google.com/maps/documentation/maps-static)  
  - Main service provided:   
    - Retrieve static map images for displaying landmarks  
  - Status: Successfully tested with OkHttp in Java.  
- [Routes API](https://developers.google.com/maps/documentation/routes)  
  - Main service provided:  
    - Provide directions and distance estimates for routes  
  - Status: Successfully tested with OkHttp in Java.  
- [Places API](https://developers.google.com/maps/documentation/places/)  
  - Main service provided:  
    - Provide details about landmarks — names, types, open hours  
  - Status: Successfully tested with OkHttp in Java.

