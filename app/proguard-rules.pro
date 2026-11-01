# Keep Room Database schema and entities
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Dagger / Hilt generated components
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class dagger.hilt.** { *; }

# Keep Compose Runtime
-keep class androidx.compose.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Models and Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.spendsight.app.domain.model.** { *; }
-keep class com.spendsight.app.data.parser.** { *; }

