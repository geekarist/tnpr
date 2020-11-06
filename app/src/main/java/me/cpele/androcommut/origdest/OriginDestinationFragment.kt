package me.cpele.androcommut.origdest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.cpele.androcommut.R

class OriginDestinationFragment : Fragment() {

    companion object {
        fun newInstance() = OriginDestinationFragment()
    }

    private lateinit var viewModel: OriginDestinationViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.origin_destination_fragment, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(OriginDestinationViewModel::class.java)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        view.findViewById<View>(R.id.od_origin_edit_text).setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_UP) {
                viewModel.dispatch(OriginDestinationViewModel.Intention.OriginClicked)
            }
            true
        }

        view.findViewById<View>(R.id.od_destination_edit_text).setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_UP) {
                viewModel.dispatch(OriginDestinationViewModel.Intention.DestinationClicked)
            }
            true
        }
    }
}