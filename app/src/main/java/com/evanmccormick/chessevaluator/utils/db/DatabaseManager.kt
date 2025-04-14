package com.evanmccormick.chessevaluator.ui.utils.db

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.*
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
    val elos: Map<String, Int>
)

data class LeaderboardUser(
    val id: String,
    val rank: Int,
    val username: String,
    val elo: Int
)

class DatabaseManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    suspend fun createUser(): DocumentSnapshot {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val initialElo = 1500

            val elosMap = hashMapOf(
                "5" to initialElo,
                "15" to initialElo,
                "30" to initialElo,
                "60" to initialElo,
                "120" to initialElo,
                "300" to initialElo,
            )
            val userEloData = hashMapOf(
                "username" to (user.displayName ?: "User_${uid.substring(0,6)}"),
                "elos" to elosMap
            )

            db.collection("users")
                .document(uid)
                .set(userEloData)
                .await()

            return db.collection("users").document(uid).get().await()
        } ?: run {
            throw Exception("No user is currently logged in.")
        }
    }

    suspend fun updateUsername(username: String) {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            db.collection("users").document(uid).update("username", username).await()
        } ?: run {
            throw Exception("No user is currently logged in.")
        }
    }

    suspend fun getUserInfo(): UserInfo {
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            var documentSnapshot = db.collection("users").document(uid).get()
                .await()
            if(documentSnapshot.exists()) {
                val username = documentSnapshot.getString("username")!!
                val elosMap = documentSnapshot.get("elos") as Map<String, Long>

                return UserInfo(username, elosMap.mapValues { it.value.toInt() })
            } else {
                documentSnapshot = createUser()
                val username = documentSnapshot.getString("username")!!
                val elosMap = documentSnapshot.get("elos") as Map<String, Long>

                return UserInfo(username, elosMap.mapValues { it.value.toInt() })
            }
        } ?: run {
            throw Exception("Error: No user is currently logged in.")
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
        try { user?.let {
            val uid = it.uid
            db.collection("users").document(uid).update(fieldPath, newElo)
                .await()
        } ?: run {
            // Handle the case where there is no user
            throw Exception("Error: No user is currently logged in.")
        } } catch (e: Exception) {
            print("Error updating user elo: ${e.message}")
        }
    }

    suspend fun updatePositionElo(id: String, newElo: Int, timeControl: Int) {
        val fieldPath = "elos.$timeControl"
        db.collection("positions").document(id).update(fieldPath, newElo).await()
    }

    suspend fun getRandomPosition(timeControl: Int ): Position {
        val positionsRef = db.collection("positions")

        //Generate a random index # less than the size of the collection
        //val sizeQuerySnapshot = positionsRef.get().await()
        val size = 20000 //Size is currently 1000

        val randomIndex = Random.nextInt(0, size)
        val documentId = "pos${randomIndex}"

        //Return the position associated with the generated index
        val document = positionsRef.document(documentId).get().await()
        val id = document.id
        val fen = document.getString("fen")!!
        val eval = document.getDouble("eval")!!.toFloat()/100

        // Get the elos map and extract elo for the current time control
        val elosMap = document.get("elos") as Map<String, Long>
        val elo = elosMap[timeControl.toString()]!!.toInt()
        val tags = document.get("tags") as List<String>

        return Position(id, fen, eval, elo, tags)
    }

    // Leaderboard functions

    suspend fun getLeaderboard(timeControl: Int, limit: Int = 30): List<LeaderboardUser> {
        try {
            // Query users collection, ordered by the specific timeControl elo
            val usersRef = db.collection("users")
            val querySnapshot = usersRef
                .orderBy("elos.$timeControl", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            // Map documents to LeaderboardUser objects with rank
            return querySnapshot.documents.mapIndexed { index, document ->
                val username = document.getString("username") ?: "Anonymous"
                val elosMap = document.get("elos") as? Map<String, Long> ?: mapOf()
                val elo = elosMap[timeControl.toString()]?.toInt() ?: 1500

                LeaderboardUser(
                    id = document.id,
                    rank = index + 1, // 1-based rank
                    username = username,
                    elo = elo
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
            val higherEloCount = db.collection("users").whereGreaterThan("elos.$timeControl", userElo).count().get(AggregateSource.SERVER).await().count

            // User's rank is higherEloCount + 1
            val rank = higherEloCount.toInt() + 1

            return LeaderboardUser(
                id = currentUser.uid,
                rank = rank,
                username = username,
                elo = userElo
            )
        } catch (e: Exception) {
            println("Error getting user leaderboard position: ${e.message}")
            return null
        }
    }
}
