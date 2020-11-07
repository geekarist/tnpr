package me.cpele.androcommut

import androidx.lifecycle.LiveData

interface Model<IntentionT, StateT, EffectT> {
    val stateLive: LiveData<StateT>
    val effectLive: LiveData<Event<EffectT>>
    fun dispatch(intention: IntentionT)
}
