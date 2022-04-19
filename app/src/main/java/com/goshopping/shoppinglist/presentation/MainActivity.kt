package com.goshopping.shoppinglist.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.updateMargins
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.databinding.ActivityMainBinding
import com.goshopping.shoppinglist.presentation.fragments.BottomNavigationDrawerFragment
import com.goshopping.shoppinglist.presentation.fragments.BottomNavigationDrawerFragmentEdit


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding

    lateinit var navHostFragment: NavHostFragment
    lateinit var bottomAppBar: BottomAppBar
    lateinit var behavior: BottomAppBar.Behavior
    /**
     * создаем SharedPreferences
     */
    lateinit var sharedPref: SharedPreferences
    var highScore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bottomAppBar = binding.bottomAppBar
         behavior = bottomAppBar.behavior

        binding.mainCardeView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        setMarginFragment(true)
        /**
         * получаем значение из SharedPreferences
         */
        sharedPref =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        startOrStopAnimation(sharedPref.getBoolean(getString(R.string.first_run), false))

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.fabMain.setOnClickListener {
            /**
             * используем, когда надо убрать предложение создать первый спсиок
             */
            if (!highScore) {
                sharedPref.edit().putBoolean(getString(R.string.first_run), true).apply()
                startOrStopAnimation(sharedPref.getBoolean(getString(R.string.first_run), false))
            }
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

        val param = binding.mainCardeView.layoutParams as ViewGroup.MarginLayoutParams
        if (mainScreen) {
            binding.imageBugs.apply {
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
            binding.imageBugs.animate()
                .alpha(0f)
                .setDuration(300L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.imageBugs.visibility = View.GONE
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


