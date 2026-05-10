# LLM-Enhanced Learning Assistant App - SIT305 Task 6.1D/10.1D

An Android application built for SIT305 that provides personalized AI-generated quizzes based on student interests, with hint generation, answer explanations, quiz history tracking, profile analytics, and a simulated upgrade flow. The backend is powered by Llama 3.1 via Groq.

## Features

### Core (from 6.1D)
- User registration and login with local Room database
- Interest selection (up to 10 topics) to personalize learning
- AI-generated quizzes based on selected interests
- Per-question hint generation without revealing the answer
- Per-question answer explanations on the results screen
- Loading, error, and retry states for all API calls
- Session persistence using SharedPreferences
- Expand/collapse quiz questions with smooth animations
- Screen transition animations

### New in 10.1D
- Profile screen with user stats (total questions, correct, incorrect)
- AI-powered summary of incorrect answers via a new backend endpoint
- Quiz history screen with expandable cards showing color-coded answer review
- Upgrade screen with three subscription tiers (Starter, Intermediate, Advanced)
- Simulated Google Pay payment dialog matching the real Google Pay UI
- Share profile stats to any app via Android share intent
- Room-based quiz history persistence across sessions

## Tech Stack
- Java (Android SDK)
- Room Database (users + quiz history)
- SharedPreferences for session management
- ViewBinding for all layouts
- Retrofit + OkHttp + Gson for API communication
- Material Components theming
- Flask backend with Groq API (Llama 3.1 8B Instant)

## Project Structure

### Data Layer
- `User.java` - Room entity for user accounts
- `UserDao.java` - Database queries for user authentication
- `QuizHistory.java` - Room entity for storing answered questions
- `QuizHistoryDao.java` - Queries for history, stats, and clearing data
- `AppDatabase.java` - Room database instance (version 2)
- `SessionManager.java` - SharedPreferences wrapper for login session

### Network Layer
- `ApiService.java` - Retrofit interface for all backend endpoints
- `ApiClient.java` - Retrofit client instance
- `QuizResponse.java` - Response model for quiz data
- `HintResponse.java` - Response model for hint data
- `ExplanationResponse.java` - Response model for explanation data
- `SummaryResponse.java` - Response model for AI summary data

### Models
- `Task.java` - Model for generated task cards

### Activities
- `WelcomeActivity.java` - Login screen
- `SignupActivity.java` - Registration screen
- `InterestsActivity.java` - Topic selection screen
- `HomeActivity.java` - Dashboard with generated task list and profile access
- `TaskActivity.java` - Quiz screen with questions, options, and hints
- `ResultsActivity.java` - Score screen with AI explanations, saves history to Room
- `ProfileActivity.java` - User stats, AI summary, share, and navigation to History/Upgrade
- `HistoryActivity.java` - Scrollable list of all past quiz questions
- `UpgradeActivity.java` - Three-tier upgrade cards with simulated Google Pay

### Adapters
- `TaskAdapter.java` - Adapter for task list RecyclerView
- `QuestionAdapter.java` - Adapter for quiz questions with expand/collapse
- `HistoryAdapter.java` - Adapter for history items with expand/collapse and color-coded answers

### Layouts
- `activity_welcome.xml` - Login screen
- `activity_signup.xml` - Sign up screen
- `activity_interests.xml` - Interest selection grid
- `activity_home.xml` - Dashboard with greeting and task list
- `activity_task.xml` - Quiz screen with loading, error, and question list
- `activity_results.xml` - Results screen with score and explanation cards
- `activity_profile.xml` - Profile screen with stats, AI summary, and share
- `activity_history.xml` - History list screen
- `activity_upgrade.xml` - Upgrade tiers screen
- `dialog_google_pay.xml` - Simulated Google Pay payment dialog
- `item_task.xml` - Card for each generated task
- `item_question.xml` - Card for each quiz question with hint section
- `item_result.xml` - Card for each result with AI explanation
- `item_history.xml` - Card for each history entry with expandable answers

## Backend

`main.py` - Flask server with four endpoints:

- `GET /getQuiz?topic=<topic>` - Generates 3 quiz questions on the given topic
- `POST /getHint` - Returns a hint for a question without revealing the answer
- `POST /explainAnswer` - Returns an explanation of the correct answer
- `POST /summarize` - Returns a summary of the student's incorrect answers for the profile screen

## Backend Setup

1. Navigate to the backend folder
2. Create a virtual environment: `python3 -m venv pyEnv`
3. Activate: `source pyEnv/bin/activate`
4. Install dependencies: `pip install flask groq`
5. Add your Groq API key in `main.py`
6. Run: `python main.py`

## Design Choices

- Room was chosen over MongoDB Atlas for data persistence since the app is a single-user learning tool where offline access is more practical than remote syncing.
- The Google Pay payment flow is simulated because Google Play Billing requires a paid developer account ($25 USD). The UI and interaction pattern match what a real integration would look like.
- Sharing uses Android's built-in share intent, which provides functional end-to-end sharing through the system share sheet without needing a custom link server or QR code generator.
