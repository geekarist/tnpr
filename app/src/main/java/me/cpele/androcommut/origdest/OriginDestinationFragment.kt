package me.cpele.androcommut.origdest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.cpele.androcommut.R
import me.cpele.androcommut.origdest.OriginDestinationViewModel.Effect

class OriginDestinationFragment : Fragment() {

    companion object {
        fun newInstance() = OriginDestinationFragment()
    }

    private lateinit var viewModel: OriginDestinationViewModel

    private lateinit var originButton: Button
    private lateinit var destinationButton: Button

    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.origin_destination_fragment, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProvider(this).get(OriginDestinationViewModel::class.java)

        listener = context as? Listener
            ?: throw IllegalStateException(
                "${context.javaClass.simpleName} has to implement ${Listener::class.simpleName}"
            )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originButton = view.findViewById(R.id.od_origin_button)
        destinationButton = view.findViewById(R.id.od_destination_button)

        val intention = OriginDestinationFragmentArgs
            .fromBundle(requireArguments())
            .let { args ->
                OriginDestinationViewModel.Intention.Load(
                    args.origin,
                    args.destination
                )
            }

        viewModel.dispatch(intention)

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            originButton.text = state?.origin
            destinationButton.text = state?.destination
        }

        viewModel.effectLive.observe(viewLifecycleOwner) { event ->
            event.consume { effect ->
                when (effect) {
                    is Effect.NavigateToAutosuggest.Origin ->
                        listener?.openAutosuggestOrigin(this)
                    is Effect.NavigateToAutosuggest.Destination ->
                        listener?.openAutosuggestDestination(this)
                }
            }
        }

        originButton.setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.OriginClicked)
        }

        destinationButton.setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.DestinationClicked)
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun openAutosuggestOrigin(fragment: Fragment)
        fun openAutosuggestDestination(fragment: Fragment)
    }
}