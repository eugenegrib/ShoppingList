package com.goshopping.shoppinglist.presentation.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.databinding.FragmentBottomsheetEditBinding
import com.goshopping.shoppinglist.presentation.SettingActivity

class BottomNavigationDrawerFragmentEdit : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomsheetEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomsheetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                R.id.setting_bottom ->
                    startActivity(Intent(activity, SettingActivity::class.java))
                R.id.share_bottom ->
                    Toast.makeText(
                        activity,
                        "Share",
                        Toast.LENGTH_SHORT
                    ).show()
                R.id.star_bottom ->
                    Toast.makeText(
                        activity,
                        "Rate",
                        Toast.LENGTH_SHORT
                    ).show()
                R.id.about_us_bottom ->
                    Toast.makeText(
                        activity,
                        "About Us",
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