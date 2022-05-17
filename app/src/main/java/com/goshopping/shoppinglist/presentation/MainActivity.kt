package com.goshopping.shoppinglist.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.core.view.WindowCompat
import androidx.core.view.updateMargins
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.resources.TextAppearance
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.databinding.ActivityMainBinding
import com.goshopping.shoppinglist.domain.StartApplication
import com.goshopping.shoppinglist.presentation.fragments.BottomNavigationDrawerFragment
import com.goshopping.shoppinglist.presentation.fragments.BottomNavigationDrawerFragmentEdit
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModel
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModelFactory


class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    lateinit var navHostFragment: NavHostFragment
    lateinit var bottomAppBar: BottomAppBar
    lateinit var behavior: BottomAppBar.Behavior
    lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    lateinit var sharedPreferences: SharedPreferences


    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        if (key == "chooseTheme") {
            recreate()
        }
        if (key == "fontStyle") {
            recreate()
        }
        if (key == "fontSize") {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        when (sharedPreferences.getString("chooseTheme", "")) {
            "RED" -> setTheme(R.style.Theme_ShoppingList_Red)
            "GREY" -> setTheme(R.style.Theme_ShoppingList_Grey)
            "YELLOW" -> setTheme(R.style.Theme_ShoppingList_Yellow)
            "BLUE" -> setTheme(R.style.Theme_ShoppingList_Blue)
            "GREEN" -> setTheme(R.style.Theme_ShoppingList_Green)
            "ORANGE" -> setTheme(R.style.Theme_ShoppingList_Orange)
        }
        when (sharedPreferences.getString("fontStyle", "")) {
            "Light" -> setTheme(R.style.light)
            "Medium" -> setTheme(R.style.medium)
            "Cursive" -> setTheme(R.style.cursive)
            "Bold" -> setTheme(R.style.bold)
        }

        when (sharedPreferences.getString("fontSize", "")) {
            "Very small" -> setTheme(R.style.fontSizeVerySmall)
            "Small" -> setTheme(R.style.fontSizeSmall)
            "Normal" -> setTheme(R.style.fontSizeNormal)
            "Big" -> setTheme(R.style.fontSizeBig)
            "Very Big" -> setTheme(R.style.fontSizeVeryBig)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


      /*  when (sharedPreferences.getString("fontStyle", "")) {
            "Light" -> textMainItem.setTextAppearance(R.style.light)
            "Medium" -> textMainItem.setTextAppearance(R.style.medium)
            "Cursive" -> textMainItem.setTextAppearance(R.style.cursive)
            "Bold" -> textMainItem.setTextAppearance(R.style.bold)
        }*/


        viewModelFactory = MainViewModelFactory(
            (application as StartApplication).mainDatabase.mainItemDao(),
            (application as StartApplication).database.itemDao()
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        bottomAppBar = binding.bottomAppBar
        behavior = bottomAppBar.behavior
        binding.mainCardeView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        setMarginFragment(true)


        /**
         * Чтобы fab предлагал создать список
         */
        viewModel.allMainItems.observe(this) {
            startOrStopAnimation(it.isNotEmpty())
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.fabMain.setOnClickListener {

            if (navController.currentDestination?.id == R.id.mainFragment) {

                behavior.slideDown(bottomAppBar) // use this to hide it

                binding.fabMain.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                binding.bottomAppBar.setFabAlignmentModeAndReplaceMenu(
                    BottomAppBar.FAB_ALIGNMENT_MODE_END,
                    R.menu.bottom_app_bar
                )
                setMarginFragment(false)
                navController.navigate(
                    R.id.action_mainFragment_to_newListFragment,
                    null,
                    null,
                    null
                )
            } else {
                onBackPressed()
            }
        }

        binding.bottomAppBar.setNavigationOnClickListener {
            if (navController.currentDestination?.id == R.id.firstFragment) {
                BottomNavigationDrawerFragment().show(
                    supportFragmentManager,
                    BottomNavigationDrawerFragment().tag
                )
            } else {
                BottomNavigationDrawerFragmentEdit().show(
                    supportFragmentManager,
                    BottomNavigationDrawerFragmentEdit().tag
                )
            }
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    Toast.makeText(
                        this,
                        getString(R.string.nav_item2),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
        }
    }


    fun startOrStopAnimation(high: Boolean) {
        if (!high) {
            val sa1 = ScaleAnimation(
                0.9F,
                1.1F,
                0.9F,
                1.1F,
                Animation.RELATIVE_TO_SELF,
                0.5F,
                Animation.RELATIVE_TO_SELF,
                0.5F
            )
            sa1.duration = 500
            sa1.repeatCount = Animation.INFINITE
            sa1.repeatMode = Animation.REVERSE
            sa1.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
            })
            binding.fabMain.startAnimation(sa1)
        } else {
            binding.fabMain.clearAnimation()
        }
    }

    fun FAB() {
        if (navController.currentDestination?.id != R.id.mainFragment) {
            binding.fabMain.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            binding.bottomAppBar.setFabAlignmentModeAndReplaceMenu(
                BottomAppBar.FAB_ALIGNMENT_MODE_END,
                R.menu.bottom_app_bar
            )
            behavior.slideDown(bottomAppBar) // use this to hide it

        } else {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setMarginFragment(mainScreen: Boolean) {
        when (sharedPreferences.getString("chooseTheme", "")) {
            "RED" -> binding.backImage.setImageResource(R.drawable.back_for_red_theme)
            "GREY" -> binding.backImage.setImageResource(R.drawable.back_for_grey_theme)
            "YELLOW" -> binding.backImage.setImageResource(R.drawable.back_for_yellow_theme)
            "BLUE" -> binding.backImage.setImageResource(R.drawable.back_for_blue_theme)
            "GREEN" -> binding.backImage.setImageResource(R.drawable.back_for_green_theme)
            "ORANGE" -> binding.backImage.setImageResource(R.drawable.back_for_orange_theme)
        }

        val param = binding.mainCardeView.layoutParams as ViewGroup.MarginLayoutParams
        if (mainScreen) {
            binding.backImage.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(700L)
                    .setListener(null)
            }
            param.updateMargins(top = Resources.getSystem().displayMetrics.heightPixels / 6)
            binding.mainCardeView.layoutParams = param

        } else {
            param.updateMargins(top = Resources.getSystem().displayMetrics.heightPixels / 16)
            binding.backImage.animate()
                .alpha(0f)
                .setDuration(300L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.backImage.visibility = View.GONE
                    }
                })
            binding.mainCardeView.layoutParams = param
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        behavior.slideUp(bottomAppBar) // use this to show it
        setMarginFragment(true)
        binding.bottomAppBar.setFabAlignmentModeAndReplaceMenu(
            BottomAppBar.FAB_ALIGNMENT_MODE_CENTER,
            R.menu.bottom_app_bar
        )
        binding.fabMain.setImageResource(R.drawable.ic_baseline_add_24)
    }
}



