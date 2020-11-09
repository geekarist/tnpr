package me.cpele.afk

import androidx.lifecycle.LiveData

interface Model<IntentionT, StateT, EffectT> {
    fun dispatch(intention: IntentionT)
    val stateLive: LiveData<StateT>
    val effectLive: LiveData<Event<EffectT>>
}
