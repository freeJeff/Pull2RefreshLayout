package com.freejeff.pull2refreshlayout.lib

enum class RefreshState {
    INITIAL,
    PULLING,
    HOLDING,
    RELEASE_BACK,
    REFRESHING,
    RETURNING
}