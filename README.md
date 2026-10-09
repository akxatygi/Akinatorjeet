# Akinatorjeet

An Akinator-style Android game. Think of a Bollywood celebrity, answer a handful of questions, and the app tries to guess who you picked.

What fun is an normal quiz app why not see magic happen by answering some simple questions 

It runs fully **offline**. There is no server and no AI model. The "brain" is a small scoring algorithm working on a hand-made dataset of 30 celebrities and 20 questions.

> **Status:** working MVP. See [Roadmap](#roadmap) for planned features.

---

## Table of contents

1. [How to play](#how-to-play)
2. [Tech stack](#tech-stack)
3. [How it works](#how-it-works)
4. [App flow and screens](#app-flow-and-screens)
5. [Project structure](#project-structure)
6. [The dataset](#the-dataset)
7. [The game engine](#the-game-engine)
8. [Architecture](#architecture)
9. [Look and feel](#look-and-feel)
10. [Build and run](#build-and-run)
11. [Testing](#testing)
12. [Known limitations](#known-limitations)
13. [Roadmap](#roadmap)
14. [Disclaimer](#disclaimer)

---

## How to play

1. Open the app. A splash screen appears for about 3 seconds, then the start screen.
2. Think of one celebrity from the dataset (an actor, singer, composer, or director).
3. Tap **Start**. The app asks yes/no style questions such as "Is the person male?".
4. Answer with one of five options: **Yes, Probably, Don't know, Probably not, No**.
5. When the app is confident, it names a celebrity. Tell it whether it is right.
6. If it is wrong, it tries its next-best guess. After **3 wrong guesses** you win the round.
7. On the result screen, tap **Play again** to start over.

---

## Tech stack

| Part | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| Screens | Classic Android Views: five `AppCompatActivity` screens with XML layouts |
| Theme resources | XML (`strings.xml`, `colors.xml`, `themes.xml`) |
| Game state | A `GameManager` singleton that holds the engine and the wrong-guess counter |
| Data loading | Gson (reads JSON from the `assets` folder) |
| Build system | Gradle 8.13 with Android Gradle Plugin 8.13.2 and a version catalog (`gradle/libs.versions.toml`) |
| Tests | JUnit 4 (plain unit tests, no emulator needed) |

Every screen is an `Activity` with its own XML layout. `GameViewModel` is in the project but no screen uses it (see [Architecture](#architecture)).

---

## How it works

The idea in plain language:

1. Every celebrity has **facts** attached (male? actor? alive? won a National Film Award? ...).
2. Every question is linked to one of those facts.
3. Each answer **raises the score** of celebrities that match and **lowers the score** of those that do not.
4. The next question is the one that **splits the remaining candidates closest to half and half**, which is what makes the game feel smart.
5. When one celebrity is clearly ahead (or no question can separate the rest), the app guesses.

Because answers add or subtract points instead of hard-eliminating people, one mistaken answer from the player does not break the game.

---

## App flow and screens

```
SplashActivity ──> StartActivity ──> QuestionActivity ──> GuessActivity ──> ResultActivity
   (3 seconds)        (Start)         (asks questions)     (Yes / No)        (won or lost)
                                            ^                   |
                                            └───── wrong guess ─┘
```

| Screen | Activity | Layout | What it does |
|---|---|---|---|
| Splash | `SplashActivity` | `activity_splash` | Shows the logo for 3 seconds, then opens the start screen and closes itself. This is the launcher activity. |
| Start | `StartActivity` | `activity_start` | The **Start** button (`startButton`) calls `GameManager.initGame()` to load the data and reset the engine, then opens the question screen. |
| Question | `QuestionActivity` | `activity_question` | Shows "Question #N" and the question text, with five answer buttons (`btnYes`, `btnProbably`, `btnDontKnow`, `btnProbablyNot`, `btnNo`). After each answer it asks the engine whether to guess or to ask another question. |
| Guess | `GuessActivity` | `activity_guess` | Shows the guessed name. **Yes** opens the result screen as a win. **No** rejects that celebrity, adds one wrong guess, and either returns to the question screen or ends the game after 3 wrong guesses. |
| Result | `ResultActivity` | `activity_result` | Shows "I guessed it!" or "You beat me!" with a subtitle, and a **Play again** button that clears the task and returns to the start screen. |

### How the screens talk to each other

- The celebrity name travels to `GuessActivity` as an intent extra (`EXTRA_CELEBRITY_NAME`).
- The win or loss travels to `ResultActivity` as `EXTRA_WON`.
- `QuestionActivity` stays open underneath `GuessActivity`. When a wrong guess closes the guess screen, `QuestionActivity.onRestart()` runs `displayNextStep()` again, which decides whether to ask another question or make the next guess.
- The shared game state lives in `GameManager`, so all screens see the same engine.

---

## Project structure

```
app/src/main/
├── assets/
│   └── celebrities.json            <- all questions and celebrity data
├── java/com/example/akijeet/
│   ├── SplashActivity.kt           <- launcher screen, opens StartActivity after 3 seconds
│   ├── StartActivity.kt            <- Start button, initialises the game
│   ├── QuestionActivity.kt         <- shows questions, collects the five answer types
│   ├── GuessActivity.kt            <- shows a guess, handles Yes / No
│   ├── ResultActivity.kt           <- win / lose screen and Play again
│   ├── data/
│   │   ├── Models.kt               <- Question, Celebrity, GameData
│   │   └── GameRepository.kt       <- loads celebrities.json using Gson
│   ├── game/
│   │   ├── answer.kt               <- the five answer options and their weights
│   │   ├── GameEngine.kt           <- scoring, next-question choice, guess rule
│   │   └── GameManager.kt          <- singleton that owns the engine and wrong-guess count
│   └── ui/
│       └── GameViewModel.kt        <- unused for now
├── res/
│   ├── layout/                     <- activity_splash, _start, _question, _guess, _result
│   ├── font/                       <- southern_beach (custom font)
│   ├── drawable/                   <- orange gradient background, logo (quizz.png)
│   └── values/                     <- strings.xml, colors.xml, themes.xml
└── AndroidManifest.xml             <- declares the five activities

app/src/test/java/com/example/akijeet/
└── GameEngineTest.kt               <- plays the game as every celebrity
```

### Manifest

`AndroidManifest.xml` declares five activities. Only `SplashActivity` is exported and carries the `MAIN` / `LAUNCHER` intent filter. The other four are internal (`exported="false"`). The app uses the theme `Theme.AKIJEET`, and the icon, round icon and label come from the standard `mipmap` and `strings` resources.

---

## The dataset

All game data lives in one file: `app/src/main/assets/celebrities.json`.

### Format

```json
{
  "questions": [
    { "id": "male", "text": "Is the person male?" }
  ],
  "celebrities": [
    { "name": "Shah Rukh Khan", "answers": { "male": 1, "actor": 1 } }
  ]
}
```

It is parsed into three Kotlin data classes in `Models.kt`: `Question(id, text)`, `Celebrity(name, answers)` and `GameData(questions, celebrities)`.

- Every celebrity has an answer for **every** question id.
- Answer values:

| Value | Meaning |
|---|---|
| `1` | Yes |
| `-1` | No |
| `0` | Not sure or debatable. The engine ignores it (it counts for or against nobody). A missing key is treated the same way. |

### Current content

- **30 celebrities:** 14 actors, 10 actresses, 3 musicians, 3 directors.
- **20 questions:** gender, profession, era, family background, birthplace, awards (National Film Award, Padma), and a few fun ones (beauty pageant, "Khan" in the name, Hollywood work).

### Adding a celebrity

1. Open `celebrities.json`.
2. Copy an existing celebrity line and change the name.
3. Set all 20 answers (`1`, `-1`, or `0`). Use `0` whenever you are not sure. Wrong data makes the game guess badly, so check facts first.
4. Run the unit test (see [Testing](#testing)). It checks that the engine can still guess everyone.

### Adding a question

1. Add `{ "id": "your_id", "text": "Your question?" }` to `questions`.
2. Add `"your_id": 1/-1/0` to **every** celebrity. A good question splits the list roughly in half and does not give everyone the same answer.

---

## The game engine

The engine is plain Kotlin in `game/GameEngine.kt`, with no Android code in it. That is why it can be tested quickly on a computer.

### Scoring

Every celebrity starts at a score of 0. For each answer:

```
score += answer weight x celebrity's fact (1, -1 or 0)
```

| Player answer | Weight |
|---|---|
| Yes | +1.0 |
| Probably | +0.5 |
| Don't know | 0 |
| Probably not | -0.5 |
| No | -1.0 |

### Choosing the next question

1. Remove celebrities the player has already rejected.
2. Take the celebrities whose score is within `MARGIN` of the leader (the "candidates").
3. For each unasked question, count how many candidates would answer yes, no, or unknown.
4. Skip questions that cannot separate anyone (nobody says yes, or nobody says no).
5. Score each remaining question with `cost = |yes - no| + 0.5 x unknown` and pick the lowest cost, which is the most even split.

### When to guess

`shouldGuess()` checks these rules in order:

1. No remaining question can separate the candidates, so guess.
2. 15 questions have been asked, so guess.
3. Fewer than 5 questions have been asked, so keep asking.
4. Only one candidate is left, so guess.
5. Otherwise guess only if the leader is ahead of the runner-up by at least `LEAD_TO_GUESS` points.

If every celebrity has been rejected, the engine has nothing left to offer and the game ends as a loss for the app.

### Wrong guesses

When the player says "No, that's not who I'm thinking of", the celebrity is added to a rejected list and can no longer be chosen. The next-best celebrity becomes the new guess. The counter lives in `GameManager.wrongGuesses`, and the game ends after 3.

### Tuning

These constants are in `GameEngine.kt`:

| Constant | Default | Effect |
|---|---|---|
| `MARGIN` | 3.0 | How close to the leader a celebrity must be to stay a candidate |
| `MIN_QUESTIONS` | 5 | The app never guesses before this many questions |
| `MAX_QUESTIONS` | 15 | The app always guesses by this point |
| `LEAD_TO_GUESS` | 4.0 | Lower means faster but less accurate; higher means slower but safer |

The limit of 3 wrong guesses is currently written directly into `GuessActivity`.

In a design-time simulation of this logic against the 30-celebrity dataset, truthful answers led to a correct guess for all 30 celebrities in about 9 questions on average, and a single wrong answer from the player still produced the right guess about 93% of the time.

---

## Architecture

```
celebrities.json
      |  (Gson, via GameRepository)
      v
  GameData ──> GameEngine <── GameManager (singleton) <── Activities
 (data classes)  (scoring and    (owns the engine and        Splash, Start, Question,
                  next question)  the wrong-guess count)     Guess, Result
```

- **`GameEngine`** knows the rules but nothing about the screen.
- **`GameManager`** is a Kotlin `object`. `initGame()` loads the data once, builds a fresh engine and resets the wrong-guess counter. `getEngine()` returns the current engine (creating one if needed). `findCelebrityByName()` turns the name passed between screens back into a `Celebrity`.
- **Activities** read from and write to the shared engine. Each one handles only its own screen and button taps.
- **`GameViewModel`** (with its `GameUiState`: `Start`, `Asking`, `Guessing`, `Won`, `Lost`) is a ready-made alternative controller. No activity uses it. It is kept in case the app moves to a ViewModel-based design later.

---

## Look and feel

- **Colors:** orange `#FF7C52`, light orange `#FFCA5C` and dark brown `#2B0E0A`.
- **Font:** `Southern Beach` (`res/font/southern_beach`).
- **Theme:** `Theme.AKIJEET` for the XML screens.
- **Assets:** an orange gradient background and the `quizz.png` logo.

---

## Build and run

### Requirements

- Android Studio (a recent version that supports Android Gradle Plugin 8.13)
- JDK 21 (the one bundled with Android Studio works)
- An Android phone with USB debugging enabled, or an emulator. A real phone is usually faster.

### Steps

1. Clone the repository and open the project folder in Android Studio.
2. Wait for the Gradle sync to finish.
3. Select your device and press **Run**.

### Release build

For a signed release APK: **Build > Generate Signed App Bundle / APK**. Keep your keystore file (`.jks`) out of Git. The `.gitignore` should contain `*.jks` and `*.keystore`.

---

## Testing

`GameEngineTest` plays the whole game once for **each** of the 30 celebrities, answering honestly from the JSON, and checks that:

- the engine guesses the right celebrity, and
- it uses 15 questions or fewer.

Run it from Android Studio by right-clicking `GameEngineTest.kt` and choosing **Run 'GameEngineTest'**, or from the terminal:

```
./gradlew testDebugUnitTest
```

Run this test again after every change to `celebrities.json` or the tuning constants. The test covers the engine only, not the screens.

---

## Known limitations

- **Small dataset.** Only 30 people, so the game only works if you pick someone from the list.
- **Hand-made data.** Facts were compiled by hand and may contain errors. Anything uncertain is marked `0`. Time-sensitive facts (awards, whether someone is alive) can go out of date.
- **No learning.** If the app fails, it does not remember who you were thinking of.
- **Some celebrities are hard to tell apart**, so the app may need extra questions for them (for example, pairs that differ in only one fact).
- **Back button on the result screen.** `QuestionActivity` stays in the back stack after a guess, so pressing Back on the result screen can send the player back into the question flow and reopen the guess screen. **Play again** is not affected because it clears the whole task.
- **Game state is in memory only.** `GameManager` keeps everything in static fields, so if Android removes the app process in the background, the game restarts from a fresh engine.
- **No photos.** Celebrity photos are usually copyrighted, so none are included.

---

## Roadmap(stuff i want to add)

- [ ] Add a new mode where the user will pick a celeb beforehand and will try to get a highscore by predicting it in least amount of question
- [ ] Learning mode: when the app loses, ask who it was and save the new celebrity locally (Room database)
- [ ] More celebrities and better questions
- [ ] Animations between questions
- [ ] Back / undo button for the last answer
- [ ] Licensed or open-licensed celebrity images
- [ ] Sound and haptic feedback
- [ ] Hindi language support through `strings.xml`
- [ ] Fix the back-stack behaviour after a guess (finish `QuestionActivity` or handle Back on the result screen)
- [ ] Move the "3 wrong guesses" limit into one shared constant
- [ ] Remove the unused `GameViewModel`, or put it to use

---

## Disclaimer

This is an independent, non-commercial student project. It is not affiliated with Akinator or with any person named in the dataset. Names are used only to identify public figures in a guessing game, and the information about them comes from publicly known facts compiled by hand. If you spot a wrong fact, please open an issue or pull request.

---

## Author

Built by **ni3k** as a learning project (Kotlin, Android Studio).
