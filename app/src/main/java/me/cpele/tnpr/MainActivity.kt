package me.cpele.tnpr

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.FlowPreview
import me.cpele.afk.ViewModelFactory
import me.cpele.tnpr.autosuggest.AutosuggestFragment
import me.cpele.tnpr.autosuggest.AutosuggestFragmentDirections
import me.cpele.tnpr.autosuggest.AutosuggestTrigger
import me.cpele.tnpr.origdest.OriginDestinationFragment
import me.cpele.tnpr.origdest.OriginDestinationFragmentDirections
import me.cpele.tnpr.tripselection.TripSelectionFragment
import me.cpele.tnpr.tripselection.TripSelectionFragmentDirections

@FlowPreview
class MainActivity : AppCompatActivity(),
    OriginDestinationFragment.Listener,
    AutosuggestFragment.Listener,
    TripSelectionFragment.Listener {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory { MainViewModel() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel.eventLive.observe(this) { event ->
            event.consume { effect ->
                when (effect) {
                    is MainViewModel.Effect.SuggestionIdentified -> {
                        val fragment = supportFragmentManager.findFragmentById(effect.fragmentId)
                        fragment?.findNavController()?.navigate(
                            AutosuggestFragmentDirections.actionAutosuggestFragmentToOriginDestinationFragment(
                                effect.originId,
                                effect.originLabel,
                                effect.destinationId,
                                effect.destinationLabel
                            )
                        )
                    }
                }
            }
        }
    }

    override fun openAutosuggestOrigin(fragment: Fragment, id: String?, label: String?) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestTrigger.ORIGIN,
                label
            )
        )

    override fun openAutosuggestDestination(fragment: Fragment, id: String?, label: String?) =
        fragment.findNavController().navigate(
            OriginDestinationFragmentDirections.actionOriginDestinationToAutosuggest(
                AutosuggestTrigger.DESTINATION,
                label
            )
        )

    override fun openTripSelection(
        fragment: Fragment,
        originId: String,
        originLabel: String,
        destinationId: String,
        destinationLabel: String
    ) = fragment.findNavController().navigate(
        OriginDestinationFragmentDirections.actionOriginDestinationToTripSelection(
            originId,
            originLabel,
            destinationId,
            destinationLabel
        )
    )

    override fun takeAutosuggestion(
        fragment: Fragment,
        trigger: AutosuggestTrigger,
        id: String,
        label: String
    ) = viewModel.dispatch(MainViewModel.Action.Suggestion(fragment.id, trigger, id, label))

    override fun openTrip(fragment: Fragment, tripId: String) =
        fragment.findNavController()
            .navigate(TripSelectionFragmentDirections.actionJourneysFragmentToRoadmapFragment(tripId))
}