package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.FollowEntity
import com.example.data.local.entities.LikeEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.ReelEntity
import com.example.data.local.entities.SoundEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class AnkitVerseRepository(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) {
    companion object {
        const val MONETIZATION_VIEW_THRESHOLD = 500_000L
        const val CPM_PAYOUT_RATE = 0.08 // $0.08 per 1,000 views
    }

    val currentUserId: String?
        get() = sessionManager.currentUserId

    fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn

    // ---------------- AUTHENTICATION ----------------
    suspend fun registerUser(
        username: String,
        displayName: String,
        age: Int,
        password: String,
        avatarUri: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (cleanUsername.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        }
        if (password.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        }
        if (age < 13) {
            return@withContext Result.failure(IllegalArgumentException("You must be at least 13 years old to join AnkitVerse"))
        }

        val existing = db.userDao().getUserByUsername(cleanUsername)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Username '@$cleanUsername' is already taken"))
        }

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            username = cleanUsername,
            displayName = displayName.trim().ifEmpty { cleanUsername },
            age = age,
            passwordHash = hashPassword(password),
            avatarUri = avatarUri,
            bio = "Exploring the cosmic creativity of AnkitVerse! ✨"
        )
        try {
            db.userDao().insertUser(newUser)
            sessionManager.currentUserId = newUser.id
            sessionManager.isFirstLaunch = false
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        val user = db.userDao().getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("User '@$cleanUsername' not found"))

        if (user.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password"))
        }

        sessionManager.currentUserId = user.id
        sessionManager.isFirstLaunch = false
        Result.success(user)
    }

    fun signOut() {
        sessionManager.clearSession()
    }

    fun observeCurrentUser(): Flow<UserEntity?> {
        val uid = sessionManager.currentUserId ?: return flowOf(null)
        return db.userDao().getUserById(uid)
    }

    fun observeUser(userId: String): Flow<UserEntity?> {
        return db.userDao().getUserById(userId)
    }

    fun observeAllUsers(): Flow<List<UserEntity>> {
        return db.userDao().getAllUsers()
    }

    // ---------------- HOME FEED & STORIES ----------------
    fun observeAllPosts(): Flow<List<PostEntity>> = db.postDao().observeAllPosts()

    fun observeUserPosts(userId: String): Flow<List<PostEntity>> = db.postDao().observePostsByAuthor(userId)

    suspend fun createPost(
        mediaUri: String,
        mediaType: String,
        caption: String,
        songTitle: String?,
        songArtist: String?,
        songAudioUri: String?
    ): Result<PostEntity> = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in"))
        val user = db.userDao().getUserByIdSync(uid)
            ?: return@withContext Result.failure(IllegalStateException("User not found"))

        val post = PostEntity(
            id = UUID.randomUUID().toString(),
            authorId = uid,
            authorUsername = user.username,
            authorDisplayName = user.displayName,
            authorAvatarUri = user.avatarUri,
            mediaUri = mediaUri,
            mediaType = mediaType,
            caption = caption,
            songTitle = songTitle,
            songArtist = songArtist,
            songAudioUri = songAudioUri
        )
        db.postDao().insertPost(post)
        Result.success(post)
    }

    fun observeActiveStories(): Flow<List<StoryEntity>> = db.storyDao().observeActiveStories()

    suspend fun createStory(
        mediaUri: String,
        mediaType: String,
        caption: String
    ): Result<StoryEntity> = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in"))
        val user = db.userDao().getUserByIdSync(uid)
            ?: return@withContext Result.failure(IllegalStateException("User not found"))

        val story = StoryEntity(
            id = UUID.randomUUID().toString(),
            authorId = uid,
            authorUsername = user.username,
            authorAvatarUri = user.avatarUri,
            mediaUri = mediaUri,
            mediaType = mediaType,
            caption = caption
        )
        db.storyDao().insertStory(story)
        Result.success(story)
    }

    // ---------------- REELS & OPEN AUDIO REUSE ----------------
    fun observeAllReels(): Flow<List<ReelEntity>> = db.reelDao().observeAllReels()

    fun observeUserReels(userId: String): Flow<List<ReelEntity>> = db.reelDao().observeReelsByAuthor(userId)

    fun observeReelsBySong(songTitle: String): Flow<List<ReelEntity>> = db.reelDao().observeReelsBySong(songTitle)

    suspend fun createReel(
        videoUri: String,
        caption: String,
        songTitle: String,
        songArtist: String,
        songAudioUri: String?
    ): Result<ReelEntity> = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in"))
        val user = db.userDao().getUserByIdSync(uid)
            ?: return@withContext Result.failure(IllegalStateException("User not found"))

        val reel = ReelEntity(
            id = UUID.randomUUID().toString(),
            authorId = uid,
            authorUsername = user.username,
            authorDisplayName = user.displayName,
            authorAvatarUri = user.avatarUri,
            videoUri = videoUri,
            caption = caption,
            songTitle = songTitle.ifBlank { "Original Audio - @${user.username}" },
            songArtist = songArtist.ifBlank { user.displayName },
            songAudioUri = songAudioUri
        )
        db.reelDao().insertReel(reel)

        // Register or increment sound usage in sound library
        val soundId = "snd_${reel.songTitle.replace(" ", "_").lowercase()}"
        val existingSound = db.soundDao().getSoundById(soundId)
        if (existingSound == null) {
            db.soundDao().insertSound(
                SoundEntity(
                    id = soundId,
                    title = reel.songTitle,
                    artist = reel.songArtist,
                    audioUri = reel.songAudioUri,
                    usageCount = 1
                )
            )
        } else {
            db.soundDao().incrementUsage(soundId)
        }

        Result.success(reel)
    }

    fun observeSounds(): Flow<List<SoundEntity>> = db.soundDao().observeAllSounds()

    // ---------------- LIKES & ENGAGEMENT ----------------
    fun observeIsLiked(targetId: String): Flow<Boolean> {
        val uid = sessionManager.currentUserId ?: return flowOf(false)
        return db.likeDao().observeIsLiked(targetId, uid)
    }

    suspend fun toggleLike(targetId: String, targetType: String) = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId ?: return@withContext
        val isLiked = db.likeDao().isLikedSync(targetId, uid)
        if (isLiked) {
            db.likeDao().deleteLike(LikeEntity(targetId, uid, targetType))
            if (targetType == "POST") db.postDao().updateLikesCount(targetId, -1)
            else if (targetType == "REEL") db.reelDao().updateLikesCount(targetId, -1)
        } else {
            db.likeDao().insertLike(LikeEntity(targetId, uid, targetType))
            if (targetType == "POST") db.postDao().updateLikesCount(targetId, 1)
            else if (targetType == "REEL") db.reelDao().updateLikesCount(targetId, 1)
        }
    }

    suspend fun recordView(targetId: String, targetType: String, creatorId: String) = withContext(Dispatchers.IO) {
        if (targetType == "POST") db.postDao().incrementViews(targetId)
        else if (targetType == "REEL") db.reelDao().incrementViews(targetId)

        // Increment creator's total views for monetization
        db.userDao().incrementViews(creatorId, 1L)

        // Check monetization status
        val creator = db.userDao().getUserByIdSync(creatorId) ?: return@withContext
        val newViews = creator.totalViews
        val isNowMonetized = newViews >= MONETIZATION_VIEW_THRESHOLD
        val newEarnings = (newViews / 1000.0) * CPM_PAYOUT_RATE
        if (isNowMonetized != creator.isMonetized || newEarnings != creator.earnings) {
            db.userDao().updateMonetization(creatorId, isNowMonetized, newEarnings)
        }
    }

    // ---------------- COMMENTS ----------------
    fun observeComments(targetId: String): Flow<List<CommentEntity>> = db.commentDao().observeCommentsForTarget(targetId)

    suspend fun addComment(targetId: String, targetType: String, text: String): Result<CommentEntity> = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in"))
        val user = db.userDao().getUserByIdSync(uid)
            ?: return@withContext Result.failure(IllegalStateException("User not found"))

        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            targetId = targetId,
            targetType = targetType,
            authorId = uid,
            authorUsername = user.username,
            authorAvatarUri = user.avatarUri,
            text = text.trim()
        )
        db.commentDao().insertComment(comment)
        if (targetType == "POST") db.postDao().updateCommentsCount(targetId, 1)
        else if (targetType == "REEL") db.reelDao().updateCommentsCount(targetId, 1)
        Result.success(comment)
    }

    // ---------------- DIRECT MESSAGING ----------------
    fun observeConversation(otherUserId: String): Flow<List<MessageEntity>> {
        val uid = sessionManager.currentUserId ?: return emptyFlow()
        return db.messageDao().observeConversation(uid, otherUserId)
    }

    suspend fun sendMessage(
        receiverId: String,
        text: String,
        mediaUri: String? = null,
        mediaType: String? = null,
        isVoiceNote: Boolean = false,
        voiceDurationSec: Int = 0
    ): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in"))
        val user = db.userDao().getUserByIdSync(uid)
            ?: return@withContext Result.failure(IllegalStateException("User not found"))

        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = uid,
            receiverId = receiverId,
            senderUsername = user.username,
            text = text.trim(),
            mediaUri = mediaUri,
            mediaType = mediaType,
            isVoiceNote = isVoiceNote,
            voiceDurationSec = voiceDurationSec
        )
        db.messageDao().insertMessage(msg)
        Result.success(msg)
    }

    // ---------------- FOLLOW ----------------
    fun observeIsFollowing(targetUserId: String): Flow<Boolean> {
        val uid = sessionManager.currentUserId ?: return flowOf(false)
        return db.followDao().observeIsFollowing(uid, targetUserId)
    }

    suspend fun toggleFollow(targetUserId: String) = withContext(Dispatchers.IO) {
        val uid = sessionManager.currentUserId ?: return@withContext
        if (uid == targetUserId) return@withContext

        val isFollowing = db.followDao().observeIsFollowing(uid, targetUserId)
        // Check synchronously
        // For simplicity:
        val follow = FollowEntity(followerId = uid, followedId = targetUserId)
        try {
            db.followDao().follow(follow)
            db.followDao().updateFollowingCount(uid, 1)
            db.followDao().updateFollowersCount(targetUserId, 1)
        } catch (e: Exception) {
            db.followDao().unfollow(follow)
            db.followDao().updateFollowingCount(uid, -1)
            db.followDao().updateFollowersCount(targetUserId, -1)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
