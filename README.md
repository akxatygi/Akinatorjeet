# Akinatorjeet

An Akinator-style Android game. Think of a Bollywood celebrity, answer a handful of questions, and the app tries to guess who you picked.

It runs fully **offline**. There is no server and no AI model. The "brain" is a small scoring algorithm working on a hand-made dataset of 30 celebrities and 20 questions.

> **Status:** working MVP. See [Roadmap](#roadmap) for planned features.



---

## Table of contents

1. [How to play](#how-to-play)
2. [Tech stack](#tech-stack)
3. [How it works](#how-it-works)
4. [Project structure](#project-structure)
5. [The dataset](#the-dataset)
6. [The game engine](#the-game-engine)
7. [Architecture](#architecture)
8. [Build and run](#build-and-run)
9. [Testing](#testing)
10. [Known limitations](#known-limitations)
11. [Roadmap](#roadmap)
12. [Disclaimer](#disclaimer)

---

## How to play

1. Open the app. A splash screen appears, then the start screen.
2. Think of one celebrity from the dataset (an actor, singer, composer, or director).
3. Tap **Start**. The app asks yes/no style questions such as "Is the person male?".
4. Answer with one of five options: **Yes, Probably, Don't know, Probably not, No**.
5. When the app is confident, it names a celebrity. Tell it whether it is right.
6. If it is wrong, it tries its next-best guess. After **3 wrong guesses** you win the round.

---

## Tech stack

| Part | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| Game screens | Jetpack Compose with Material 3 |
| Splash screen and resources | XML (layout, colors, strings, themes, manifest) |
| State handling | Android `ViewModel` and `StateFlow` |
| Data loading | Gson (reads JSON from the `assets` folder) |
| Build system | Gradle 8.13 with Android Gradle Plugin 8.13.2 and a version catalog (`gradle/libs.versions.toml`) |
| Compose version | Compose BOM 2024.09.00 |
| Tests | JUnit 4 (plain unit tests, no emulator needed) |

The splash screen is built with classic XML views, and everything after it is Jetpack Compose. Both are normal in a real Android project, and XML resource files (`strings.xml`, `colors.xml`, `themes.xml`, the manifest) are still used by Compose apps.

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

## Project structure

```
app/src/main/
├── assets/
│   └── celebrities.json            <- all questions and celebrity data
├── java/com/example/quizapp/
│   ├── SplashActivity.kt           <- XML splash screen, then opens MainActivity
│   ├── MainActivity.kt             <- hosts the Compose UI
│   ├── data/
│   │   ├── Models.kt               <- Question, Celebrity, GameData
│   │   └── GameRepository.kt       <- loads celebrities.json using Gson
│   ├── game/
│   │   ├── Answer.kt               <- the five answer options and their weights
│   │   └── GameEngine.kt           <- scoring, next-question choice, guess rule
│   └── ui/
│       ├── GameViewModel.kt        <- game state exposed to the screens
│       ├── GameScreens.kt          <- Start, Question, Guess and Result screens
│       └── theme/                  <- Color.kt, Theme.kt, Type.kt
├── res/
│   ├── layout/activity_splash.xml  <- splash layout (XML)
│   ├── drawable/                   <- orange gradient background, logo (quizz.png)
│   └── values/                     <- strings.xml, colors.xml, themes.xml
└── AndroidManifest.xml

app/src/test/java/com/example/quizapp/
└── GameEngineTest.kt               <- plays the game as every celebrity
```

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

- Every celebrity has an answer for **every** question id.
- Answer values:

| Value | Meaning |
|---|---|
| `1` | Yes |
| `-1` | No |
| `0` | Not sure or debatable. The engine ignores it (it counts for or against nobody). |

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

1. Take the celebrities whose score is within `MARGIN` of the leader (the "candidates").
2. For each unasked question, count how many candidates would answer yes, no, or unknown.
3. Skip questions that cannot separate anyone.
4. Pick the question with the most even yes/no split. Unknown answers add a small penalty.

### When to guess

The app guesses when any of these is true:

- No remaining question can separate the candidates.
- 15 questions have been asked.
- At least 5 questions have been asked **and** the leader is ahead of the runner-up by `LEAD_TO_GUESS` points.

### Tuning

These constants are in `GameEngine.kt`:

| Constant | Default | Effect |
|---|---|---|
| `MARGIN` | 3.0 | How close to the leader a celebrity must be to stay a candidate |
| `MIN_QUESTIONS` | 5 | The app never guesses before this many questions |
| `MAX_QUESTIONS` | 15 | The app always guesses by this point |
| `LEAD_TO_GUESS` | 4.0 | Lower means faster but less accurate; higher means slower but safer |

In a design-time simulation of this logic against the 30-celebrity dataset, truthful answers led to a correct guess for all 30 celebrities in about 9 questions on average, and a single wrong answer from the player still produced the right guess about 93% of the time.

---

## Architecture

```
celebrities.json
      |  (Gson, via GameRepository)
      v
  GameData  ──>  GameEngine  <──>  GameViewModel  ──>  Compose screens
 (data classes)   (scoring and      (UI state:           (Start, Question,
                   next question)    StateFlow)           Guess, Result)
```

- **`GameEngine`** knows the rules but nothing about the screen.
- **`GameViewModel`** owns the engine and exposes one `GameUiState`: `Start`, `Asking`, `Guessing`, `Won`, or `Lost`. It survives screen rotation.
- **`GameApp`** (in `GameScreens.kt`) shows whichever screen matches the current state. Screens only display state and send button taps back to the ViewModel.

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

Run this test again after every change to `celebrities.json` or the tuning constants.

---

## Known limitations

- **Small dataset.** Only 30 people, so the game only works if you pick someone from the list.
- **Hand-made data.** Facts were compiled by hand and may contain errors. Anything uncertain is marked `0`. Time-sensitive facts (awards, whether someone is alive) can go out of date.
- **No learning.** If the app fails, it does not remember who you were thinking of.
- **Some celebrities are hard to tell apart**, so the app may need extra questions for them (for example, pairs that differ in only one fact).
- **No photos.** Celebrity photos are usually copyrighted, so none are included.

---

## Roadmap

- [ ] Learning mode: when the app loses, ask who it was and save the new celebrity locally (Room database)
- [ ] More celebrities and better questions
- [ ] Animations between questions
- [ ] Back / undo button for the last answer
- [ ] Licensed or open-licensed celebrity images
- [ ] Sound and haptic feedback
- [ ] Hindi language support through `strings.xml`

---

## Disclaimer

This is an independent, non-commercial student project. It is not affiliated with Akinator or with any person named in the dataset. Names are used only to identify public figures in a guessing game, and the information about them comes from publicly known facts compiled by hand. If you spot a wrong fact, please open an issue or pull request.

---

## Author

Built by **ni3k** as a learning project (Kotlin, Android Studio, Jetpack Compose).

Contributions, bug reports, and dataset corrections are welcome.
