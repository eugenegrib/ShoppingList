package com.goshopping.shoppinglist.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.databinding.FragmentBottomsheetBinding

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {


    private var _binding: FragmentBottomsheetBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomsheetBinding.inflate(inflater, container, false)


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem!!.itemId) {
                R.id.nav1 ->
                    Toast.makeText(
                    activity,
                    getString(R.string.setting_item_bottom),
                    Toast.LENGTH_SHORT
                ).show()


                R.id.nav2 ->
                    Toast.makeText(
                        activity,
                        getString(R.string.nav_item4),
                        Toast.LENGTH_SHORT
                    ).show()
            }
            true
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}