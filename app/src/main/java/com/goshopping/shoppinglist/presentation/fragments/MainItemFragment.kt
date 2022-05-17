package com.goshopping.shoppinglist.presentation.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.MainFragmentBinding
import com.goshopping.shoppinglist.domain.MoreFunctionsMainItem
import com.goshopping.shoppinglist.domain.StartApplication
import com.goshopping.shoppinglist.domain.SwipeHelper
import com.goshopping.shoppinglist.domain.adapters.MainListAdapter
import com.goshopping.shoppinglist.presentation.MainActivity
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModel
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModelFactory
import kotlinx.coroutines.launch

class MainItemFragment : Fragment(), MoreFunctionsMainItem {
    private lateinit var recyclerViewAdapter: MainListAdapter
    lateinit var item: MainItem
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    var mainList: List<MainItem> = listOf()
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough()
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = MainViewModelFactory(
            (activity?.application as StartApplication).mainDatabase.mainItemDao(),
            (activity?.application as StartApplication).database.itemDao()
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        when (sharedPreferences.getString("fontSize", "")) {
            "Very small" -> binding.textView.textSize = 16F
            "Small" ->  binding.textView.textSize = 20F
            "Normal" ->  binding.textView.textSize = 24F
            "Big" ->  binding.textView.textSize = 28F
            "Very Big" ->  binding.textView.textSize = 32F
        }

        val inflater = TransitionInflater.from(activity)
        exitTransition = inflater.inflateTransition(R.transition.fade)
        viewModel.createMainItemsList()
        viewModel.allMainItems.observe(viewLifecycleOwner) { items ->
            items.let {
                recyclerViewAdapter.submitList(it)
                mainList = it
            }
        }
        val resId: Int = R.anim.layout_animation_fall_down
        val animation: LayoutAnimationController =
            AnimationUtils.loadLayoutAnimation(activity, resId)
        recyclerViewAdapter = MainListAdapter(this) { openFragment(it) }
        binding.firstRV.adapter = recyclerViewAdapter
        binding.firstRV.layoutAnimation = animation
        val swipeHelper: SwipeHelper = object : SwipeHelper(binding.firstRV) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder?,
                underlayButtons: MutableList<UnderlayButton>?
            ) {
                underlayButtons?.add(
                    UnderlayButton(
                        "Delete",
                        Color.parseColor("#FF3C30"),
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                recyclerViewAdapter.notifyItemChanged(pos)
                                lifecycleScope.launch {
                                    MaterialAlertDialogBuilder(context!!)
                                        .setTitle(resources.getString(R.string.delete))
                                        .setMessage(resources.getString(R.string.title))
                                        .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                                            // Respond to negative button press
                                        }
                                        .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                                            val mainItem = recyclerViewAdapter.currentList[pos]
                                            viewModel.deleteItemMain(mainItem)
                                        }
                                        .show()
                                }
                            }
                        }
                    )
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.firstRV)
    }

    fun openFragment(mainItem: MainItem) {
        val action = MainItemFragmentDirections.actionMainFragmentToEditShopList(
            mainItem.id,
            mainItem.parentName
        )

        findNavController().navigate(action)
        with((activity as MainActivity)) {
            setMarginFragment(false)
            FAB()
        }
    }

    override fun updateMainItem(mainItem: MainItem) {
    }

    override fun clearFocus() {
        activity?.currentFocus?.clearFocus()
    }

    override fun requestFocus(view: View) {
        view.requestFocus()
    }

    override fun deleteMainItem(mainItem: MainItem) {
        viewModel.deleteItemMain(mainItem)
    }
}
