package com.freejeff.pull2refreshlayout.lib

enum class RefreshState {
    STATE_INITIAL,
    STATE_PREPARE,
    STATE_PULLING,
    STATE_HOLDING,
    STATE_REFRESHING,
    STATE_RETURNING
}