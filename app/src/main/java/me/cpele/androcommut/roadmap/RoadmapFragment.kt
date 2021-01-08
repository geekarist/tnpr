package me.cpele.androcommut.roadmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R

class RoadmapFragment : Fragment() {

    companion object {
        fun newInstance() = RoadmapFragment()
    }

    private val viewModel: RoadmapViewModel by viewModels {
        ViewModelFactory {
            RoadmapViewModel(
                CustomApp.instance.tripCache
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.roadmap_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args =
            arguments?.let { RoadmapFragmentArgs.fromBundle(it) } ?: throw IllegalArgumentException(
                "Invalid arguments: $arguments"
            )
        viewModel.load(args.tripId)
    }

}