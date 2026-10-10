# Akinatorjeet

An Akinator-style Android game. Think of a Bollywood celebrity, answer a handful of questions, and the app tries to guess who you picked.

What fun is a normal quiz app? Why not see some magic happen by answering a few simple questions.

It runs fully **offline**. There is no server and no AI model. The "brain" is a small scoring algorithm working on a hand-made dataset of 60 celebrities and 24 questions.

> **Status:** working MVP with two game modes. See [Roadmap](#roadmap) for planned features.

---

## Table of contents

1. [Features](#features)
2. [How to play](#how-to-play)
3. [Game modes](#game-modes)
4. [Tech stack](#tech-stack)
5. [How it works](#how-it-works)
6. [App flow and screens](#app-flow-and-screens)
7. [Project structure](#project-structure)
8. [The dataset](#the-dataset)
9. [The game engine](#the-game-engine)
10. [Architecture](#architecture)
11. [Look and feel](#look-and-feel)
12. [Build and run](#build-and-run)
13. [Testing](#testing)
14. [Known limitations](#known-limitations)
15. [Roadmap](#roadmap)
16. [Disclaimer](#disclaimer)

---

## Features

- **Classic Mode:** think of a celebrity and let the app read your mind.
- **Target Celeb Challenge:** pick a celebrity from a searchable list, answer for them truthfully, and earn a score and star rating based on how quickly the app finds them.
- Five answer options: Yes, Probably, Don't know, Probably not, No.
- Smart question choice: always asks the question that best splits the remaining candidates.
- Forgiving scoring: one wrong answer does not break the game.
- Up to 3 guesses per round.
- Instructions screen that explains the rules of the chosen mode.
- Smooth fade transitions between every screen.
- Custom font, colour theme and logo.
- Works fully offline.

---

## How to play

1. Open the app. A splash screen appears for about 3 seconds, then the start screen.
2. Choose a mode: **CLASSIC MODE** or **TARGET CELEB CHALLENGE**.
3. Read the instructions and continue.
4. Answer the questions with one of five options: **Yes, Probably, Don't know, Probably not, No**.
5. When the app is confident, it names a celebrity. Tell it whether it is right.
6. If it is wrong, it tries its next-best guess. After **3 wrong guesses** you win the round.
7. On the result screen, tap **PLAY AGAIN** to return to the start screen.

---

## Game modes

### Classic Mode

1. Think of any famous Bollywood celebrity from the dataset. Do not tell the app.
2. Answer each question truthfully.
3. The app tries to guess who you are thinking of in 15 questions or fewer.

If the app guesses right, the result screen says **"AKIJEET Guessed It!"**. If it fails after 3 wrong guesses, you see **"You Beat AKIJEET!"**.

### Target Celeb Challenge

1. Pick a target celebrity from the list. The list is sorted alphabetically and has a search box ("Search celebrity name...").
2. Answer every question truthfully for that celebrity.
3. The faster the app identifies your target, the higher your score.

When the app guesses your target, the result screen shows the target's name, your score out of 100, how many questions it took, and a star rating.

#### Scoring

```
questions asked <= 5   ->  100 points
questions asked >= 15  ->   50 points
otherwise              ->  100 - (questions asked - 5) x 5
```

| Questions asked | Score | Rating |
|---|---|---|
| 5 or 6 | 100 or 95 | 5 stars, "Master Mind!" |
| 7 or 8 | 90 or 85 | 4 stars, "Awesome!" |
| 9 to 11 | 80, 75 or 70 | 3 stars, "Great Job!" |
| 12 or more | 65 down to 50 | 2 stars, "Good Effort!" |

The score is only shown when the app wins. If the app fails to guess the target, the result screen says **"Couldn't guess <name>!"** and shows no score.

---

## Tech stack

| Part | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| Screens | Seven `AppCompatActivity` screens with XML layouts |
| UI components | Material Components, ConstraintLayout, CardView, RecyclerView (for the celebrity list) |
| Theme and resources | XML (`strings.xml`, `colors.xml`, `themes.xml`, `anim/`, `font/`, `drawable/`) |
| Game state | A `GameManager` singleton that holds the engine, the mode, the target and the wrong-guess counter |
| Data loading | Gson 2.11.0 (reads JSON from the `assets` folder) |
| Android versions | `minSdk` 24 (Android 7.0), `compileSdk` and `targetSdk` 36 |
| Build system | Gradle 8.13 with Android Gradle Plugin 8.13.2 and a version catalog (`gradle/libs.versions.toml`), Java 17 |
| Tests | JUnit 4 (plain unit tests, no emulator needed) |

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
SplashActivity ──> StartActivity ──┬── CLASSIC MODE ─────────────> InstructionsActivity ──────────┐
                                   │                                                              │
                                   └── TARGET CELEB CHALLENGE ──> InstructionsActivity            │
                                                                        │                         │
                                                                  SelectCelebActivity             │
                                                                        │                         │
                                                                        v                         v
                                          ResultActivity <── GuessActivity <──> QuestionActivity
                                          (won or lost)       (Yes / No)        (asks questions)
```

| Screen | Activity | Layout | What it does |
|---|---|---|---|
| Splash | `SplashActivity` | `activity_splash` | Shows the logo for 3 seconds, then opens the start screen and closes itself. This is the launcher activity. |
| Start | `StartActivity` | `activity_start` | Two buttons: **CLASSIC MODE** (`startButton`) and **TARGET CELEB CHALLENGE** (`targetModeButton`). Both open the instructions screen and pass the chosen mode along. |
| Instructions | `InstructionsActivity` | `activity_instructions` | Shows the rules for the chosen mode. In Classic Mode the button reads **START GAME**: it resets the engine and opens the question screen. In Target Mode it reads **SELECT CELEBRITY** and opens the celebrity list. |
| Select celebrity | `SelectCelebActivity` | `activity_select_celeb`, `item_celebrity` | Shows every celebrity in a `RecyclerView`, sorted by name, with a search box that filters the list as you type. Tapping a name starts a Target Mode game with that celebrity and opens the question screen. |
| Question | `QuestionActivity` | `activity_question` | Shows "Question #N" and the question text, with five answer buttons (`btnYes`, `btnProbably`, `btnDontKnow`, `btnProbablyNot`, `btnNo`). After each answer it asks the engine whether to guess or to ask another question. |
| Guess | `GuessActivity` | `activity_guess` | Shows "Is it..." and the guessed name. **YES!** opens the result screen as a win. **NO** rejects that celebrity, adds one wrong guess, and either returns to the question screen or ends the game after 3 wrong guesses. |
| Result | `ResultActivity` | `activity_result` | Shows the win or loss message. In Target Mode after a win it also shows a score card with the score, number of questions and star rating. **PLAY AGAIN** clears the task and returns to the start screen. |

### How the screens talk to each other

- The chosen mode travels from the start screen to `InstructionsActivity` as an intent extra (`EXTRA_IS_TARGET_MODE`).
- The chosen mode and the target celebrity are then stored in `GameManager` (`isTargetMode`, `targetCelebrity`) when the game is initialised.
- The celebrity name travels to `GuessActivity` as an intent extra (`EXTRA_CELEBRITY_NAME`).
- The win or loss travels to `ResultActivity` as `EXTRA_WON`. The result screen reads the mode, target and question count from `GameManager`.
- `QuestionActivity` stays open underneath `GuessActivity`. When a wrong guess closes the guess screen, `QuestionActivity.onRestart()` runs `displayNextStep()` again, which decides whether to ask another question or make the next guess.
- Every screen change uses `overridePendingTransition(R.anim.fade_in, R.anim.fade_out)`. Both animations are plain alpha fades of 800 ms.

---

## Project structure

```
app/src/main/
├── assets/
│   └── celebrities.json              <- all questions and celebrity data
├── java/com/example/akijeet/
│   ├── SplashActivity.kt             <- launcher screen, opens StartActivity after 3 seconds
│   ├── StartActivity.kt              <- mode selection (Classic or Target Challenge)
│   ├── InstructionsActivity.kt       <- rules for the chosen mode, starts the game or opens the list
│   ├── SelectCelebActivity.kt        <- searchable celebrity list (RecyclerView) for Target Mode
│   ├── QuestionActivity.kt           <- shows questions, collects the five answer types
│   ├── GuessActivity.kt              <- shows a guess, handles Yes / No
│   ├── ResultActivity.kt             <- win / lose screen, score card, Play again
│   ├── data/
│   │   ├── Models.kt                 <- Question, Celebrity, GameData
│   │   └── GameRepository.kt         <- loads celebrities.json using Gson
│   └── game/
│       ├── answer.kt                 <- the five answer options and their weights
│       ├── GameEngine.kt             <- scoring, next-question choice, guess rule
│       └── GameManager.kt            <- singleton: engine, mode, target, wrong guesses, score and rating
├── res/
│   ├── anim/                         <- fade_in.xml, fade_out.xml
│   ├── layout/                       <- activity_splash, _start, _instructions, _select_celeb,
│   │                                    _question, _guess, _result, item_celebrity
│   ├── font/                         <- southern_beach.ttf (custom font)
│   ├── drawable/                     <- background.xml (purple gradient), quizz.png (logo)
│   ├── mipmap-*/                     <- launcher icons
│   └── values/                       <- strings.xml, colors.xml, themes.xml
└── AndroidManifest.xml               <- declares the seven activities

app/src/test/java/com/example/akijeet/
├── GameEngineTest.kt                 <- plays the game as every celebrity
└── ExampleUnitTest.kt                <- default template test
```

### Manifest

`AndroidManifest.xml` declares seven activities. Only `SplashActivity` is exported and carries the `MAIN` / `LAUNCHER` intent filter. The other six are internal (`exported="false"`). The app uses the theme `Theme.AKIJEET`, which extends `Theme.MaterialComponents.DayNight.NoActionBar`.

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

- **60 celebrities:** 22 actors, 22 actresses, 8 singers and composers, and 8 directors and producers.
- **24 questions** covering:
  - gender and main profession (actor, music, director or producer)
  - the decade the film career started (before 1990, 1990s, 2000s, 2010 or later)
  - birth era (before 1970, 1985 or later, 1990 or later)
  - family in films, Hollywood or international work, and whether the person is alive
  - awards (National Film Award, Padma honours)
  - birthplace (Mumbai, North India, outside India)
  - a few fun ones (beauty pageant, "Khan" in the name, worked with Sanjay Leela Bhansali, child artist, started on television)

### Adding a celebrity

1. Open `celebrities.json`.
2. Copy an existing celebrity entry and change the name.
3. Set all 24 answers (`1`, `-1`, or `0`). Use `0` whenever you are not sure. Wrong data makes the game guess badly, so check facts first.
4. Run the unit test (see [Testing](#testing)). It checks that the engine can still guess everyone.

The new celebrity automatically appears in the Target Celeb Challenge list, because the list is built from the same file.

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

A simulation of this logic on the current 60-celebrity dataset, with truthful answers for every celebrity, guessed all 60 correctly. It needed 8 to 13 questions, about 9.8 on average.

---

## Architecture

```
celebrities.json
      |  (Gson, via GameRepository)
      v
  GameData ──> GameEngine <── GameManager (singleton) <── Activities
 (data classes)  (scoring and    (engine, game mode,        Splash, Start, Instructions,
                  next question)  target celebrity,          SelectCeleb, Question,
                                  wrong guesses,             Guess, Result
                                  score and rating)
```

- **`GameEngine`** knows the rules but nothing about the screen.
- **`GameManager`** is a Kotlin `object`. It does more than hold the engine:
  - `initGame(context, targetMode, target)` loads the data once, builds a fresh engine, resets the wrong-guess counter, and records the mode and the target celebrity.
  - `getEngine()` returns the current engine (creating one if needed).
  - `getAllCelebrities()` returns the full list sorted by name, for the celebrity picker.
  - `findCelebrityByName()` turns the name passed between screens back into a `Celebrity`.
  - `calculateScore()` and `getRatingStars()` turn the number of questions into the Target Mode score and rating.
- **Activities** read from and write to the shared engine and `GameManager`. Each one handles only its own screen and button taps.

---

## Look and feel

- **Background:** a purple radial gradient from `#7575B7` in the centre to `#36377D` at the edges (`drawable/background.xml`).
- **Cards and buttons:** light lavender cards (`#D4D4EC`) with dark navy (`#1E1E31`) buttons and text.
- **Font:** `Southern Beach` (`res/font/southern_beach.ttf`), used across all screens.
- **Logo:** `quizz.png`, shown on the splash and start screens.
- **Theme:** `Theme.AKIJEET`, based on Material Components (DayNight, no action bar).
- **Motion:** 800 ms fade-in and fade-out between all screens.

---

## Build and run

### Requirements

- Android Studio (a recent version that supports Android Gradle Plugin 8.13)
- JDK 17 or newer (the one bundled with Android Studio works)
- An Android phone (Android 7.0 or newer) with USB debugging enabled, or an emulator. A real phone is usually faster.

### Steps

1. Clone the repository:
   ```
   git clone https://github.com/akxatygi/Akinatorjeet.git
   ```
2. Open the project folder in Android Studio.
3. Wait for the Gradle sync to finish.
4. Select your device and press **Run**.

### Release build

For a signed release APK: **Build > Generate Signed App Bundle / APK**. Keep your keystore file (`.jks`) out of Git. The `.gitignore` should contain `*.jks` and `*.keystore`.

---

## Testing

`GameEngineTest` plays the whole game once for **each** celebrity in the dataset, answering honestly from the JSON, and checks that:

- the engine guesses the right celebrity, and
- it uses 15 questions or fewer.

Run it from Android Studio by right-clicking `GameEngineTest.kt` and choosing **Run 'GameEngineTest'**, or from the terminal:

```
./gradlew testDebugUnitTest
```

Run this test again after every change to `celebrities.json` or the tuning constants. The test covers the engine only, not the screens or the Target Mode scoring.

---

## Known limitations

- **Small dataset.** Only 60 people, so the game only works if you pick someone from the list.
- **Hand-made data.** Facts were compiled by hand and may contain errors. Anything uncertain is marked `0`. Time-sensitive facts (awards, whether someone is alive) can go out of date.
- **No learning.** If the app fails, it does not remember who you were thinking of.
- **Some celebrities are hard to tell apart**, so the app may need extra questions for them (for example, pairs that differ in only one fact).
- **Top scores are hard to reach.** The score curve gives 100 points for 5 questions or fewer, but with truthful answers the engine currently needs about 8 to 13 questions, so realistic scores in Target Mode are roughly 60 to 85.
- **Target Mode trusts the player.** The guess screen does not check whether the guessed celebrity is the chosen target, and wrong guesses do not lower the score. Tapping **YES!** on any guess counts as a win.
- **High scores are not saved.** The score is shown on the result screen only.
- **Back button on the result screen.** `QuestionActivity` stays in the back stack after a guess, so pressing Back on the result screen can send the player back into the question flow and reopen the guess screen. **PLAY AGAIN** is not affected because it clears the whole task.
- **Game state is in memory only.** `GameManager` keeps everything in static fields, so if Android removes the app process in the background, the game restarts from a fresh engine in Classic Mode.
- **No photos.** Celebrity photos are usually copyrighted, so none are included.

---

## Roadmap

Done:

- [x] Target Celeb Challenge mode with a score and star rating
- [x] Instructions screen for each mode
- [x] Searchable celebrity picker
- [x] Fade animations between screens
- [x] Custom font, colour theme and logo
- [x] Dataset grown from 30 to 60 celebrities and from 20 to 24 questions

Planned:

- [ ] Learning mode: when the app loses, ask who it was and save the new celebrity locally (Room database)
- [ ] More celebrities and better questions
- [ ] Animations between questions
- [ ] Save high scores for Target Mode
- [ ] Back / undo button for the last answer
- [ ] Licensed or open-licensed celebrity images
- [ ] Sound and haptic feedback
- [ ] Hindi language support through `strings.xml`
- [ ] Fix the back-stack behaviour after a guess (finish `QuestionActivity` or handle Back on the result screen)
- [ ] Check the guess against the target in Target Mode, and tune the score curve
- [ ] Move the "3 wrong guesses" limit into one shared constant

---

## Disclaimer

This is an independent, non-commercial student project. It is not affiliated with Akinator or with any person named in the dataset. Names are used only to identify public figures in a guessing game, and the information about them comes from publicly known facts compiled by hand. If you spot a wrong fact, please open an issue or pull request.

---

## Author

Built by **ni3k** as a learning project (Kotlin, Android Studio).

Contributions, bug reports, and dataset corrections are welcome.
