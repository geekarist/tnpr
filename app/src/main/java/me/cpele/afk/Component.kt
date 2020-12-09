package me.cpele.afk

import androidx.lifecycle.LiveData

/**
 * User facing business logic component.
 *
 * A [Component] processes [IntentionT]s.
 * It applies some logic to it.
 * It holds and exposes a [StateT], and produces [ConsequenceT]s.
 * The [ConsequenceT]s can be consumed via [Event].
 *
 * @param IntentionT User intention
 * @param StateT Component state
 * @param ConsequenceT Consequence for the user (payload of a consumable [Event]
 *
 * The naming follows [github.com/.../Decompose](https://github.com/arkivanov/Decompose).
 */
interface Component<IntentionT, StateT, ConsequenceT> {
    fun dispatch(intention: IntentionT)
    val stateLive: LiveData<StateT>
    val eventLive: LiveData<Event<ConsequenceT>>
}
