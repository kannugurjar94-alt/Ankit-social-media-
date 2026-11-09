package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val age: Int,
    val passwordHash: String,
    val avatarUri: String,
    val bio: String = "Welcome to my AnkitVerse! ✨",
    val totalViews: Long = 0L,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isMonetized: Boolean = false,
    val earnings: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarUri: String,
    val mediaUri: String,
    val mediaType: String = "IMAGE", // "IMAGE" or "VIDEO"
    val caption: String = "",
    val songTitle: String? = null,
    val songArtist: String? = null,
    val songAudioUri: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarUri: String,
    val videoUri: String,
    val caption: String = "",
    val songId: String? = null,
    val songTitle: String = "Original Sound",
    val songArtist: String = "AnkitVerse Creator",
    val songAudioUri: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUri: String,
    val mediaUri: String,
    val mediaType: String = "IMAGE",
    val caption: String = "",
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val targetId: String, // postId or reelId
    val targetType: String, // "POST" or "REEL"
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUri: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "likes",
    primaryKeys = ["targetId", "userId"]
)
data class LikeEntity(
    val targetId: String,
    val userId: String,
    val targetType: String // "POST" or "REEL"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val senderUsername: String,
    val text: String = "",
    val mediaUri: String? = null,
    val mediaType: String? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val audioUri: String? = null,
    val coverArtUri: String? = null,
    val durationSec: Int = 30,
    val usageCount: Int = 1
)

@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followedId"]
)
data class FollowEntity(
    val followerId: String,
    val followedId: String
)
