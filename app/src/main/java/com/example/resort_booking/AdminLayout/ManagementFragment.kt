package com.example.resort_booking.AdminLayout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.resort_booking.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [ManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManagementFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manageResort = view.findViewById<TextView>(R.id.tvManageResort)
        manageResort.setOnClickListener {
            val intent = android.content.Intent(requireContext(), ResortListActivity::class.java)
            startActivity(intent)
        }

        val manageUser = view.findViewById<TextView>(R.id.tvManageUser)
        manageUser.setOnClickListener {
            val intent = android.content.Intent(requireContext(), UserListActivity::class.java)
            startActivity(intent)
        }

        val manageService = view.findViewById<TextView>(R.id.tvManageService)
        manageService.setOnClickListener {
            val intent = android.content.Intent(requireContext(), UpdateRoomActivity::class.java)
            startActivity(intent)
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ManagementFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}