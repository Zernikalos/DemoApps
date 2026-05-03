package dev.zernikalos.zernikalosdemoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

/**
 * Entry screen: pick one of the standalone engine demos. Navigation pushes the chosen fragment
 * onto the back stack so the system back gesture returns here.
 */
class SamplePickerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_sample_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btn_open_fox).setOnClickListener {
            findNavController().navigate(R.id.action_sample_picker_to_fox)
        }
        view.findViewById<MaterialButton>(R.id.btn_open_soldier).setOnClickListener {
            findNavController().navigate(R.id.action_sample_picker_to_soldier)
        }
        view.findViewById<MaterialButton>(R.id.btn_open_stormtrooper).setOnClickListener {
            findNavController().navigate(R.id.action_sample_picker_to_stormtrooper)
        }
    }
}
