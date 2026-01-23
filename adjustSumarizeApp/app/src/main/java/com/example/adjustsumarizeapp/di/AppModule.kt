package com.example.adjustsumarizeapp.di

import android.content.Context
import androidx.room.Room
import com.example.adjustsumarizeapp.data.local.AppDatabase
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.local.dao.SummaryHistoryDao
import com.example.adjustsumarizeapp.data.remote.ApiService
import com.example.adjustsumarizeapp.data.remote.AuthInterceptor
import com.example.adjustsumarizeapp.data.remote.TokenAuthenticator
import com.example.adjustsumarizeapp.data.repository.SummaryRepositoryImpl
import com.example.adjustsumarizeapp.data.repository.UserRepository
import com.example.adjustsumarizeapp.data.repository.UserRepositoryImpl
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import com.example.adjustsumarizeapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }
    
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        apiServiceProvider: javax.inject.Provider<ApiService> // Inject Provider instead of direct instance
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, apiServiceProvider)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)  // Add auth interceptor
            .authenticator(tokenAuthenticator)  // Add token refresh authenticator
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        tokenManager: TokenManager
    ): UserRepository {
        return UserRepositoryImpl(apiService, tokenManager)
    }
    
    // ==================== Database ====================
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration() // For simplicity, in production use proper migrations
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSummaryHistoryDao(database: AppDatabase): SummaryHistoryDao {
        return database.summaryHistoryDao()
    }
    
    // ==================== Repositories ====================
    
    @Provides
    @Singleton
    fun provideSummaryRepository(
        apiService: ApiService,
        summaryHistoryDao: SummaryHistoryDao,
        tokenManager: TokenManager
    ): SummaryRepository {
        return SummaryRepositoryImpl(apiService, summaryHistoryDao, tokenManager)
    }
    
    // ==================== Utils ====================
    
    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): com.example.adjustsumarizeapp.utils.NetworkMonitor {
        return com.example.adjustsumarizeapp.utils.NetworkMonitor(context)
    }
}

