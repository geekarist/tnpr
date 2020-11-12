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
import androidx.navigation.fragment.findNavController
import me.cpele.androcommut.R

class OriginDestinationFragment : Fragment() {

    companion object {
        fun newInstance() = OriginDestinationFragment()
    }

    private lateinit var viewModel: OriginDestinationViewModel

    private lateinit var originButton: Button
    private lateinit var destinationButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.origin_destination_fragment, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(OriginDestinationViewModel::class.java)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originButton = view.findViewById(R.id.od_origin_button)
        destinationButton = view.findViewById(R.id.od_destination_button)

        viewModel.dispatch(
            OriginDestinationViewModel.Intention.Load(
                arguments?.getString("origin"),
                arguments?.getString("destination")
            )
        )

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            originButton.text = state?.origin
            destinationButton.text = state?.destination
        }

        viewModel.effectLive.observe(viewLifecycleOwner) { event ->
            event.consume { effect ->
                val navTarget = when (effect) {
                    is OriginDestinationViewModel.Effect.NavigateToAutosuggest.Origin ->
                        R.id.action_originDestinationFragment_to_autosuggestFragment
                    is OriginDestinationViewModel.Effect.NavigateToAutosuggest.Destination ->
                        R.id.action_originDestinationFragment_to_autosuggestFragment
                }
                findNavController().navigate(navTarget)
            }
        }

        originButton.setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.OriginClicked)
        }

        view.findViewById<View>(R.id.od_destination_button).setOnClickListener {
            viewModel.dispatch(OriginDestinationViewModel.Intention.DestinationClicked)
        }
    }
}