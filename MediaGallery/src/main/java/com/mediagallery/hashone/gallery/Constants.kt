package com.mediagallery.hashone.gallery

//import com.puzzle.maker.instagram.post.db.TemplateTable
//import com.puzzle.maker.instagram.post.enums.AdapterItemTypes
//import com.puzzle.maker.instagram.post.model.DataBean
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object Constants {

    const val TOTAL_OFFER_TIME = 48 * 60 * 60 * 1000
    const val TOTAL_OFFER_END_TIME = 10 * 24 * 60 * 60 * 1000
    const val OFFER_LAST_CHANCE_LIMIT = 24

    const val SERVER_TIME_24 = 24 * 60 * 60 * 1000

    //TODO: For Testing
//    const val TOTAL_OFFER_TIME = 1 * 2 * 60 * 1000
//    const val TOTAL_OFFER_END_TIME = 1 * 1 * 2 * 60 * 1000

    var storedCalendar: Calendar = Calendar.getInstance()
    var currentCalendar: Calendar = Calendar.getInstance()

    var IMAGE_CROP_ORIGINAL_WIDTH: Float = 0F
    var IMAGE_CROP_ORIGINAL_HEIGHT: Float = 0F

    const val CACHE_DAY_LIMIT = 0.24

    val IS_DEBUG = true

    var storageSpace = 200

    //TODO: Application Names
    const val APP_CARO = "caro"
    const val APP_STORYSTAR = "storystar"
    const val APP_POSTPLUS = "postplus"

    val INDEX_SEARCH = -3
    val INDEX_FAVORITE = -2
    val INDEX_POPULAR = -1
    val INDEX_3x4 = 1
    val INDEX_3x3 = 4
    val INDEX_3x2 = 3
    val INDEX_3x1 = 2

    /*In App Purchase*/
    val BASE64KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5L8CVJS9ZKpwlhORQTMZrVvY1u0qCuM6p+LYYlfIOwQgiBZXN4OAe+YzrRFQfasfjINrbx2xDwoJeCCpq1tcSV+toqBJnMAvTtrO3Uxz8k1/oN3tgC/PyZXQ2FCvvz3sI6uGlL7+fLNUk+LpXlS/JNiU/PEgXzZKrxsVFEpNxiziydXgMeWuUPcgsyOln+ZXtlY9MmPoE+nPMw6R6M+KAJz2cl3dkXQsQtGGki0fSVxFqSFKK4E7J7g8b1jmH2cOkBjD1pgNiWNLx8IgjEFx4ZPmYyqv+ViSTcCs/6rVJt9Hkt5fGs7L+tSzjc7O0gb7Bbs6wxifHjGJ0TEAu+wxMQIDAQAB"
    val IN_APP_TEST_PURCHASE = "android.test.purchased"
    val IN_APP_TEST_PURCHASE_CANCELED = "android.test.canceled"
    val IN_APP_TEST_PURCHASE_REFUNDED = "android.test.refunded"
    val IN_APP_TEST_PURCHASE_ITEM_UNAVAIABLE = "android.test.item_unavailable"

    /*Product Ids*/
    val ALL_SALE_SUB_MONTH = "all_sale_month"
    val ALL_SALE_SUB_YEAR = "all_sale_year"

    val SALE_SUB_MONTH = "sale_month"
    val SALE_SUB_YEAR = "sale_year"

    val SUB_MONTH = "sub_month"
    val SUB_YEAR = "sub_year"

    val ALL_SALE_LIFETIME = "all_sale_lifetime"
    val LIFETIME_PACK = "sub_lifetime"
    val LIFETIME_PACK_OFFER = "sub_lifetime_offer"

    val IMAGE_EXTENSION = ".jpg"
    val IMAGE_EXTENSION_JPG = ".jpg"
    val JSON_EXTENSION = ".json"
    val RATE_IMAGE_NAME = "rate_image$IMAGE_EXTENSION"
    val SHARE_IMAGE_NAME = "share_image$IMAGE_EXTENSION"
    val IMAGE_QUALITY = 100
    val IMAGE_QUALITY_M = 85
    val IMAGE_QUALITY_LOLLIPOP = 80

    val FONT_VERSION = 5

    var GRID_PIXEL = 1080
    var GRID_PIXEL_M = 1024
    var GRID_PIXEL_LOLLIPOP = 1024
    var DARFT_GRID_PIXEL = 256

    val SQUARE_FRAME_WIDTH = 1080
    val SQUARE_FRAME_HEIGHT = 1080

    val PORTRAIT_FRAME_WIDTH = 1080
    val PORTRAIT_FRAME_HEIGHT = 1440

    //TODO: Saved Screen
    var filesList = ArrayList<File>()

    val IMAGE_SELECT = 0
    val IMAGE_SAVE = 1

    val DIALOG_TYPE = "DIALOG_TYPE"
    val RESPONSE_PROMO = "RESPONSE_PROMO"

    val FAB_SHOW_SAVED_LIMIT = 10
    val FAB_SHOW_3x4_LIMIT = 5
    val FAB_SHOW_3x3_LIMIT = 7
    val FAB_SHOW_3x2_LIMIT = 7
    val FAB_SHOW_3x1_LIMIT = 10
    val FAB_SHOW_DRAFT_LIMIT = 10
    val FAB_SHOW_STICKER_LIMIT = 15
    val FAB_SHOW_PALETTE_LIMIT = 15
    var mLastClickTime = 0L


    val AppPrefix = "http://play.google.com/store/apps/details?id="


    val TEST_DOMAIN = "https://test.justapps.me/"
    val LIVE_DOMAIN = "https://justapps.me/"
    val DEEPLINK_DOMAIN = "https://c958h.app.goo.gl/"

    val APP_UPDATE_COUNT = "APP_UPDATE_COUNT"

    val IS_REFERRAL_SENT = "IS_REFERRAL_SENT"
    val ANDROID_DEVICE_TOKEN = "ANDROID_DEVICE_TOKEN"

    //TODO: Language Preference
    val DEFAULT_LANGUAGE = "DEFAULT_LANGUAGE"
    val DEFAULT_LANGUAGE_NAME = "DEFAULT_LANGUAGE_NAME"

    //TODO: DEFAUTL PREFERENCE
    val STORAGE_PATH = "STORAGE_PATH"
    val STORAGE_INTERNAL_PATH = "STORAGE_INTERNAL_PATH"
    val IS_PREMIUM_PURCHASED = "IS_PREMIUM_PURCHASED"
    val PREMIUM_SKUID = "PREMIUM_SKUID"
    val AD_STATUS = "AD_STATUS"
    val FEEDBACK_EMAIL = "FEEDBACK_EMAIL"
    val PRIVACY_URL = "PRIVACY_URL"
    val PRO_PRIVACY_URL = "PRO_PRIVACY_URL"
    val INSTA_ID = "INSTA_ID"
    val BITLY_URL = "BITLY_URL"
    val HASHTAG = "HASHTAG"
    val IS_DM_VISIBLE = "IS_DM_VISIBLE"
    val IS_MULTIPLE_IMAGE_SNACK_SHOWN = "IS_MULTIPLE_IMAGE_SNACK_SHOWN"
    val INTRO_SET = "INTRO_SET"
    val IS_BLANK_CANVAS_SHOWN = "IS_BLANK_CANVAS_SHOWN"
    val IS_SALE_SHOWN = "IS_SALE_SHOWN"
    val FAVORITE_COUNT = "FAVORITE_COUNT"
    val IS_SPECIAL_OFFER_SHOWN = "IS_SPECIAL_OFFER_SHOWN"
    val IS_WEEK_CLOSE = "IS_WEEK_CLOSE"
    val IS_WEEK_NOTIFICATION_ENABLED = "IS_WEEK_NOTIFICATION_ENABLED"
    val IS_SHOWN_WEEK_NOTIFICATION_UI = "IS_SHOWN_WEEK_NOTIFICATION_UI"

    val SPECIAL_OFFER_OPEN_COUNT = "SPECIAL_OFFER_OPEN_COUNT"
    val SPECIAL_OFFER_END_OPEN_COUNT = "SPECIAL_OFFER_END_OPEN_COUNT"

    /*Share, Rate*/
    val APP_OPEN_COUNT = "APP_OPEN_COUNT"
    val NEW_APP_OPEN_COUNT = "NEW_APP_OPEN_COUNT"
    val STORY_SAVED_COUNT = "STORY_SAVED_COUNT"
    val IS_SHARE_SELECTED = "IS_SHARE_SELECTED"
/*    val ACTION_READY_FOR_SHARE = "${Utils.getAppPackageName()}.ACTION_READY_FOR_SHARE"
    val IS_RATE_SELECTED = "IS_RATE_SELECTED"
    val ACTION_TEMP_RATE_CLICK = "${Utils.getAppPackageName()}.ACTION_TEMP_RATE_CLICK"
    val ACTION_READY_FOR_RATE = "${Utils.getAppPackageName()}.ACTION_READY_FOR_RATE"
    val ACTION_REMOVE_RATE = "${Utils.getAppPackageName()}.ACTION_REMOVE_RATE" */

    val ACTION_UPDATE_FOLDER_COUNT = "ACTION_UPDATE_FOLDER_COUNT"



    //TODO: Reference
    //1. https://developer.android.com/google/play/billing/test
    //2. https://medium.com/androiddevelopers/preparing-your-apps-for-the-latest-features-in-google-plays-billing-system-210ed5e50eaa
}