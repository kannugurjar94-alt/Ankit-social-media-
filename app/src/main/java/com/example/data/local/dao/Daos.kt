package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.FollowEntity
import com.example.data.local.entities.LikeEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.ReelEntity
import com.example.data.local.entities.SoundEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY totalViews DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET totalViews = totalViews + :increment WHERE id = :userId")
    suspend fun incrementViews(userId: String, increment: Long = 1L)

    @Query("UPDATE users SET isMonetized = :isMonetized, earnings = :earnings WHERE id = :userId")
    suspend fun updateMonetization(userId: String, isMonetized: Boolean, earnings: Double)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observeAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observePostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("UPDATE posts SET likesCount = likesCount + :delta WHERE id = :postId")
    suspend fun updateLikesCount(postId: String, delta: Int)

    @Query("UPDATE posts SET commentsCount = commentsCount + :delta WHERE id = :postId")
    suspend fun updateCommentsCount(postId: String, delta: Int)

    @Query("UPDATE posts SET viewsCount = viewsCount + 1 WHERE id = :postId")
    suspend fun incrementViews(postId: String)
}

@Dao
interface ReelDao {
    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    fun observeAllReels(): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observeReelsByAuthor(authorId: String): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE songTitle = :songTitle ORDER BY createdAt DESC")
    fun observeReelsBySong(songTitle: String): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Delete
    suspend fun deleteReel(reel: ReelEntity)

    @Query("UPDATE reels SET likesCount = likesCount + :delta WHERE id = :reelId")
    suspend fun updateLikesCount(reelId: String, delta: Int)

    @Query("UPDATE reels SET commentsCount = commentsCount + :delta WHERE id = :reelId")
    suspend fun updateCommentsCount(reelId: String, delta: Int)

    @Query("UPDATE reels SET viewsCount = viewsCount + 1 WHERE id = :reelId")
    suspend fun incrementViews(reelId: String)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun observeActiveStories(now: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Delete
    suspend fun deleteStory(story: StoryEntity)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE targetId = :targetId ORDER BY createdAt ASC")
    fun observeCommentsForTarget(targetId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}

@Dao
interface LikeDao {
    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE targetId = :targetId AND userId = :userId)")
    fun observeIsLiked(targetId: String, userId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE targetId = :targetId AND userId = :userId)")
    suspend fun isLikedSync(targetId: String, userId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Delete
    suspend fun deleteLike(like: LikeEntity)
}

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :user1 AND receiverId = :user2) 
           OR (senderId = :user2 AND receiverId = :user1) 
        ORDER BY timestamp ASC
    """)
    fun observeConversation(user1: String, user2: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE senderId = :userId OR receiverId = :userId 
        ORDER BY timestamp DESC
    """)
    fun observeAllUserMessages(userId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds ORDER BY usageCount DESC")
    fun observeAllSounds(): Flow<List<SoundEntity>>

    @Query("SELECT * FROM sounds WHERE id = :id LIMIT 1")
    suspend fun getSoundById(id: String): SoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: SoundEntity)

    @Query("UPDATE sounds SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: String)
}

@Dao
interface FollowDao {
    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun observeIsFollowing(followerId: String, followedId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun follow(follow: FollowEntity)

    @Delete
    suspend fun unfollow(follow: FollowEntity)

    @Query("UPDATE users SET followersCount = followersCount + :delta WHERE id = :userId")
    suspend fun updateFollowersCount(userId: String, delta: Int)

    @Query("UPDATE users SET followingCount = followingCount + :delta WHERE id = :userId")
    suspend fun updateFollowingCount(userId: String, delta: Int)
}
