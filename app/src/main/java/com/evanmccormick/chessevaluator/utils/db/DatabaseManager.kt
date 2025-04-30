package com.evanmccormick.chessevaluator.ui.utils.db

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

data class Position(
    val id: String,
    val fen: String,
    val eval: Float,
    val elo: Int,
    val tags: List<String>,
)

data class UserInfo(
    val username: String,
    val elos: Map<String, Int>,
    val survivalScores: Map<String, Int>,
)

data class LeaderboardUser(
    val id: String, val rank: Int, val username: String, val elo: Int
)

data class SurvivalLeaderboardUser(
    val id: String,
    val rank: Int,
    val username: String,
    val score: Int,
)

class DatabaseManager @Inject constructor(
    private val auth: FirebaseAuth, private val db: FirebaseFirestore
) {

    suspend fun createUser(): DocumentSnapshot {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val initialElo = 1500
            val initialSurvivalScore = 0

            val username = when {
                !user.displayName.isNullOrBlank() -> user.displayName
                user.email != null -> user.email!!.substringBefore('@')
                else -> "User_${uid.take(6)}"
            }

            val elosMap = hashMapOf(
                "5" to initialElo,
                "15" to initialElo,
                "30" to initialElo,
                "60" to initialElo,
                "120" to initialElo,
                "300" to initialElo,
            )

            val survivalScoresMap = hashMapOf(
                "5" to initialSurvivalScore,
                "15" to initialSurvivalScore,
                "30" to initialSurvivalScore,
                "60" to initialSurvivalScore,
                "120" to initialSurvivalScore,
                "300" to initialSurvivalScore,
            )

            val userEloData = hashMapOf(
                "username" to (username ?: "Anonymous"),
                "elos" to elosMap,
                "survivalScores" to survivalScoresMap,
            )

            db.collection("users").document(uid).set(userEloData).await()

            return db.collection("users").document(uid).get().await()
        } ?: run {
            throw Exception("No user is currently logged in.")
        }
    }

    suspend fun getUserInfo(): UserInfo {
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            var documentSnapshot = db.collection("users").document(uid).get().await()
            if (documentSnapshot.exists()) {
                val username = documentSnapshot.getString("username")!!
                val elosMap = documentSnapshot.get("elos") as Map<String, Long>
                val survivalScoresMap = documentSnapshot.get("survivalScores") as Map<String, Long>

                return UserInfo(
                    username,
                    elosMap.mapValues { it.value.toInt() },
                    survivalScoresMap.mapValues { it.value.toInt() })
            } else {
                documentSnapshot = createUser()
                val username = documentSnapshot.getString("username")!!
                val elosMap = documentSnapshot.get("elos") as Map<String, Long>
                val survivalScoresMap = documentSnapshot.get("survivalScores") as Map<String, Long>

                return UserInfo(
                    username,
                    elosMap.mapValues { it.value.toInt() },
                    survivalScoresMap.mapValues { it.value.toInt() })
            }
        } ?: run {
            throw Exception("Error: No user is currently logged in.")
        }
    }

    suspend fun updateUsername(username: String) {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            db.collection("users").document(uid).update("username", username).await()
        }
    }

    suspend fun getUserElo(timeControl: Int): Int {
        val userInfo = getUserInfo()
        val elo = userInfo.elos[timeControl.toString()]!!
        return elo
    }

    suspend fun updateUserElo(newElo: Int, timeControl: Int) {
        val user = auth.currentUser
        val fieldPath = "elos.${timeControl}"
        try {
            user?.let {
                val uid = it.uid
                db.collection("users").document(uid).update(fieldPath, newElo).await()
            } ?: run {
                // Handle the case where there is no user
                throw Exception("Error: No user is currently logged in.")
            }
        } catch (e: Exception) {
            print("Error updating user elo: ${e.message}")
        }
    }

    suspend fun updateUserSurvivalScore(score: Int, timeControl: Int) {
        val user = auth.currentUser
        val fieldPath = "survivalScores.$timeControl"
        try {
            user?.let {
                val uid = it.uid
                val survivalInfo = getUserSurvivalLeaderboardInfo(timeControl)
                if (survivalInfo != null && survivalInfo.score < score){
                    db.collection("users").document(uid).update(fieldPath, score).await()
                }
            } ?: run {
                // Handle the case where there is no user
                throw Exception("Error: No user is currently logged in.")
            }
        } catch (e: Exception) {
            print("Error updating user elo: ${e.message}")
        }
    }

    suspend fun updatePositionElo(id: String, newElo: Int, timeControl: Int) {
        val fieldPath = "elos.$timeControl"
        db.collection("positions").document(id).update(fieldPath, newElo).await()
    }

    suspend fun getRandomPosition(timeControl: Int, size: Int = 9300): Position {
        val positionsRef = db.collection("positions")

        val randomIndex = Random.nextInt(0, size)
        val documentId = "pos${randomIndex}"

        //Return the position associated with the generated index
        val document = positionsRef.document(documentId).get().await()
        val id = document.id
        val fen = document.getString("fen")!!
        val eval = document.getDouble("eval")!!.toFloat() / 100

        // Get the elos map and extract elo for the current time control
        val elosMap = document.get("elos") as Map<String, Long>
        val elo = elosMap[timeControl.toString()]!!.toInt()
        val tags = document.get("tags") as List<String>

        return Position(id, fen, eval, elo, tags)
    }

    suspend fun getPositionInEloRange(minElo: Int, maxElo: Int, timeControl: Int): Position {
        val positionsRef = db.collection("positions")
        val timeControlStr = timeControl.toString()

        val query = positionsRef.whereGreaterThanOrEqualTo("elos.$timeControlStr", minElo)
            .whereLessThanOrEqualTo("elos.$timeControlStr", maxElo)

        // Execute the query
        val querySnapshot = query.get().await()

        if (querySnapshot.isEmpty) {
            // If no matching positions, fall back to random position
            return getRandomPosition(timeControl)
        }

        // Get the total count of matching documents
        val matchingCount = querySnapshot.size()

        // Select a random document from the results
        val randomIndex = Random.nextInt(0, matchingCount)
        val document = querySnapshot.documents[randomIndex]

        // Extract position data
        val id = document.id
        val fen = document.getString("fen")!!
        val eval = document.getDouble("eval")!!.toFloat() / 100
        val elosMap = document.get("elos") as Map<String, Long>
        val elo = elosMap[timeControlStr]!!.toInt()
        val tags = document.get("tags") as List<String>

        return Position(id, fen, eval, elo, tags)
    }

// Leaderboard functions

    suspend fun getLeaderboard(timeControl: Int, limit: Int = 30): List<LeaderboardUser> {
        try {
            // Query users collection, ordered by the specific timeControl elo
            val usersRef = db.collection("users")
            val querySnapshot = usersRef.orderBy(
                    "elos.$timeControl", com.google.firebase.firestore.Query.Direction.DESCENDING
                ).limit(limit.toLong()).get().await()

            // Map documents to LeaderboardUser objects with rank
            return querySnapshot.documents.mapIndexed { index, document ->
                val username = document.getString("username") ?: "Anonymous"
                val elosMap = document.get("elos") as? Map<String, Long> ?: mapOf()
                val elo = elosMap[timeControl.toString()]?.toInt() ?: 1500

                LeaderboardUser(
                    id = document.id, rank = index + 1, // 1-based rank
                    username = username, elo = elo
                )
            }
        } catch (e: Exception) {
            // Log error and return empty list
            println("Error fetching leaderboard: ${e.message}")
            return emptyList()
        }
    }

    // Get a user's position on the leaderboard for a specific time control
    suspend fun getUserLeaderboardInfo(timeControl: Int): LeaderboardUser? {
        val currentUser = auth.currentUser ?: return null

        try {
            // First get current user's elo for this time control
            val userInfo = getUserInfo()
            val userElo = userInfo.elos[timeControl.toString()]!!
            val username = userInfo.username

            // Then count how many users have higher elo
            val higherEloCount =
                db.collection("users").whereGreaterThan("elos.$timeControl", userElo).count()
                    .get(AggregateSource.SERVER).await().count

            // User's rank is higherEloCount + 1
            val rank = higherEloCount.toInt() + 1

            return LeaderboardUser(
                id = currentUser.uid, rank = rank, username = username, elo = userElo
            )
        } catch (e: Exception) {
            println("Error getting user leaderboard position: ${e.message}")
            return null
        }
    }

    suspend fun getSurvivalLeaderboard(timeControl: Int, limit: Int = 30): List<SurvivalLeaderboardUser> {
        try {
            // Query users collection, ordered by the specific timeControl elo
            val usersRef = db.collection("users")
            val querySnapshot = usersRef.orderBy(
                    "survivalScores.$timeControl",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                ).limit(limit.toLong()).get().await()

            // Map documents to LeaderboardUser objects with rank
            return querySnapshot.documents.mapIndexed { index, document ->
                val username = document.getString("username") ?: "Anonymous"
                val survivalScoresMap =
                    document.get("survivalScores") as? Map<String, Long> ?: mapOf()
                val score = survivalScoresMap[timeControl.toString()]?.toInt() ?: 0

                SurvivalLeaderboardUser(
                    id = document.id, rank = index + 1, username = username, score = score
                )
            }
        } catch (e: Exception) {
            // Log error and return empty list
            println("Error fetching leaderboard: ${e.message}")
            return emptyList()
        }
    }

    // Get a user's position on the leaderboard for a specific time control
    suspend fun getUserSurvivalLeaderboardInfo(timeControl: Int): SurvivalLeaderboardUser? {
        val currentUser = auth.currentUser ?: return null

        try {
            // First get current user's elo for this time control
            val userInfo = getUserInfo()
            val score = userInfo.survivalScores[timeControl.toString()]!!
            val username = userInfo.username

            // Then count how many users have higher elo
            val higherEloCount =
                db.collection("users").whereGreaterThan("survivalScores.$timeControl", score)
                    .count().get(AggregateSource.SERVER).await().count

            // User's rank is higherEloCount + 1
            val rank = higherEloCount.toInt() + 1

            return SurvivalLeaderboardUser(
                id = currentUser.uid, rank = rank, username = username, score = score
            )
        } catch (e: Exception) {
            println("Error getting user leaderboard position: ${e.message}")
            return null
        }
    }
}
