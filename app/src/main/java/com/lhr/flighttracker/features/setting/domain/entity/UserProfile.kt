package com.lhr.flighttracker.features.setting.domain.entity

import java.util.UUID


enum class FriendshipStatus {
    NOT_FRIEND, PENDING, ALREADY_FRIEND
}

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val isEmailVerified: Boolean,
    val phoneNumber: String,
    val isPhoneNumberVerified: Boolean,
    val avatarUrl: String?,
    var isSharingNearby: Boolean,
    val friendshipStatus: FriendshipStatus = FriendshipStatus.NOT_FRIEND
)


object UserProfileFaker {

    /**
     * 建立一個包含頭像的假使用者
     */
    fun create(): UserProfile {
        return UserProfile(
            name = "LHR",
            email = "lhr@example.com",
            isEmailVerified = true,
            phoneNumber = "+886 912 345 678",
            isPhoneNumberVerified = false,
            avatarUrl = "https://p3-pc-sign.douyinpic.com/tos-cn-i-0813c001/oEAAN9iIEAgzHUjfF8iNakAWUAgHTAAvBCXeA7~tplv-dy-aweme-images:q75.webp?biz_tag=aweme_images&from=327834062&lk3s=138a59ce&s=PackSourceEnum_SEARCH&sc=image&se=false&x-expires=1757782800&x-signature=ZYQv%2B8hqxacVrdwozko9glAVGnY%3D",
            isSharingNearby = false
        )
    }

    fun create(id: String): UserProfile {
        return UserProfile(
            name = "LHR",
            email = "lhr@example.com",
            isEmailVerified = true,
            phoneNumber = "+886 912 345 678",
            isPhoneNumberVerified = false,
            avatarUrl = "https://p3-pc-sign.douyinpic.com/tos-cn-i-0813c001/oEAAN9iIEAgzHUjfF8iNakAWUAgHTAAvBCXeA7~tplv-dy-aweme-images:q75.webp?biz_tag=aweme_images&from=327834062&lk3s=138a59ce&s=PackSourceEnum_SEARCH&sc=image&se=false&x-expires=1757782800&x-signature=ZYQv%2B8hqxacVrdwozko9glAVGnY%3D",
            isSharingNearby = false
        )
    }

    /**
     * 建立一個不包含頭像的假使用者
     */
    fun createWithoutAvatar(): UserProfile {
        return create().copy(
            name = "Guest",
            avatarUrl = null
        )
    }
}