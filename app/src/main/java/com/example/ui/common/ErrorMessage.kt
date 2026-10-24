package com.example.ui.common

import com.example.core.error.AppException

fun Throwable.toUserMessage(): String = when (this) {
    is AppException.Network -> "اتصال به اینترنت برقرار نیست. اتصال خود را بررسی کنید."
    is AppException.Timeout -> "پاسخی از سرور دریافت نشد. دوباره تلاش کنید."
    is AppException.Server -> "سرور در حال حاضر در دسترس نیست (کد ${statusCode})."
    is AppException.Parse -> "پاسخ سرور قابل خواندن نبود. لطفاً دوباره تلاش کنید."
    is AppException.Storage -> "ذخیره‌سازی اطلاعات روی دستگاه با خطا مواجه شد."
    is AppException.NotFound -> "محتوای مورد نظر پیدا نشد."
    else -> message?.takeIf { it.isNotBlank() } ?: "خطای پیش‌بینی‌نشده‌ای رخ داد."
}
