### ChessEvaluator
 
This is an app which allows users to evaluate Chess Positions shown to them one at a time on screen. It uses Jetpack Compose for the UI, and Firebase for back-end data storage and user authentication.

TODO:

The app consists of six main screens:

1. # Log-in 
   * Allow Sign-in with e-mail and password and sign in with google
   * Looks good
2. # Dashboard
   * Buttons to Navigate to other pages.
   * Play, Review (Play for previously evaluated positions), Settings, Stats, Leaderboard, Donate
   * User elo/some other stats?
3. # Position Evaluation
   * Load Position from Database
   * Evaluation Slider
   * Evaluate button
   * Tags / option to add tags.
   * Shows the correct evaluation and how much user was off by
   * Shows live updated Elo for user and puzzle
   * New Puzzle button (for both screens)
4. # Profile/Stats
   * Elo history chart
   * Current Elo
   * Estimated elo by Puzzle Tags
5. # Leaderboard
   * Top players by Elo screen
   * Hardest puzzles by Elo screen (Can try them if you haven't seen them before but only once)
   * Can locate self on leaderboard.
6. # Settings
   * Time limit for puzzle solving
   * Eval type (centi-pawn/win %)
   * Light/Dark theme?

TODO: Get basic action for all UI screens and render the app.
TODO: Implement all buttons used in the Figma wireframe, without any functionality.
TODO: Implement functionality to move between screens and adjust settings widgets, etc.