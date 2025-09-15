package com.lhr.flighttracker.features.setting.data.repositories

import com.lhr.flighttracker.features.setting.domain.entity.UserProfile
import com.lhr.flighttracker.features.setting.domain.entity.UserProfileFaker
import kotlinx.coroutines.delay
import javax.inject.Inject

// 定義 Repository 介面
interface UserRepository {
    suspend fun fetchUserProfiles(userIds: List<String>): List<UserProfile>
}

class FakeUserRepository @Inject constructor() : UserRepository {
    override suspend fun fetchUserProfiles(userIds: List<String>): List<UserProfile> {
        delay(1500)
        return userIds.map { id -> UserProfileFaker.create(id) }
    }
}