package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.FollowDao
import com.example.data.local.dao.LikeDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PostDao
import com.example.data.local.dao.ReelDao
import com.example.data.local.dao.SoundDao
import com.example.data.local.dao.StoryDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.FollowEntity
import com.example.data.local.entities.LikeEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.ReelEntity
import com.example.data.local.entities.SoundEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        ReelEntity::class,
        StoryEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        MessageEntity::class,
        SoundEntity::class,
        FollowEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun reelDao(): ReelDao
    abstract fun storyDao(): StoryDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun messageDao(): MessageDao
    abstract fun soundDao(): SoundDao
    abstract fun followDao(): FollowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ankitverse_live.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
