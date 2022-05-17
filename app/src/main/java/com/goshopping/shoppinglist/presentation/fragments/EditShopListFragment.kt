package com.goshopping.shoppinglist.presentation.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.EditShopListFragmentBinding
import com.goshopping.shoppinglist.domain.*
import com.goshopping.shoppinglist.domain.adapters.ItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.MarkedItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.NewItemAdapter
import com.goshopping.shoppinglist.presentation.MainActivity
import kotlinx.coroutines.launch


class EditShopListFragment : Fragment(), MoreFunctions {
    private var _binding: EditShopListFragmentBinding? = null
    private lateinit var recyclerViewAdapter: ItemListAdapter
    private lateinit var recyclerMarkedViewAdapter: MarkedItemListAdapter
    private lateinit var newItemAdapter: NewItemAdapter
    internal val binding get() = _binding!!
    lateinit var itemTouchHelper: ItemTouchHelper
    private var isDraggingNow = false
    private var parentparent: Int = 0
    private val navigationArgs: EditShopListFragmentArgs by navArgs()
    var main: MainItem? = null
    lateinit var sharedPreferences: SharedPreferences

    override var isAddedNewTask = false  // Чтобы добавить фокус новому элементу

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditShopListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*postponeEnterTransition()
        val inflater = TransitionInflater.from(activity)
        enterTransition = inflater.inflateTransition(R.transition.slide_down)
*/
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        when (sharedPreferences.getString("fontSize", "")) {
            "Very small" -> binding.etTitle.textSize = 16F
            "Small" ->  binding.etTitle.textSize = 20F
            "Normal" ->  binding.etTitle.textSize = 24F
            "Big" ->  binding.etTitle.textSize = 28F
            "Very Big" ->  binding.etTitle.textSize = 32F
        }
        parentparent = navigationArgs.id

        binding.etTitle.setText(navigationArgs.name)

        val itemAnimatorMarked = DefaultItemAnimator()
        itemAnimatorMarked.addDuration = 400
        itemAnimatorMarked.removeDuration = 400

        recyclerViewAdapter = ItemListAdapter(this)
        recyclerMarkedViewAdapter = MarkedItemListAdapter(this)
        newItemAdapter = NewItemAdapter { newItem() }

        binding.recyclerView.adapter = ConcatAdapter(
            recyclerViewAdapter,
            newItemAdapter,
            recyclerMarkedViewAdapter
        )

        binding.recyclerView.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(activity, R.anim.layout_animation_fall_down)

        itemTouchHelper = ItemTouchHelper(TaskItemTouchMoveCallback(recyclerViewAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.etTitle.onFocusChangeListener = View.OnFocusChangeListener { p0, p1 ->
            if (p1) {
                binding.etTitle.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.etTitle.setSelection(binding.etTitle.length())
                        (activity as MainActivity).viewModel.updateMainName(
                            main!!,
                            binding.etTitle.text.toString()
                        )
                    }
                })
            }
        }
        with((activity as MainActivity).viewModel) {
            getCategory(navigationArgs.id).observe(viewLifecycleOwner) {
                main = it
            }
            getParent(navigationArgs.id).observe(viewLifecycleOwner) {
                recyclerViewAdapter.submitList(it)
            }
            getParentMarked(navigationArgs.id).observe(viewLifecycleOwner) {
                recyclerMarkedViewAdapter.submitList(it)
                newItemAdapter.submitList(listOf("dfg"))
                //startPostponedEnterTransition()
            }
        }

        recyclerViewAdapter.onMoved = {
            (activity as MainActivity).viewModel.movedItems(recyclerViewAdapter.currentList)
            isDraggingNow = false
        }

        /**
         * Переопределяем onBackPressed
         */
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.recyclerView.isFocusable = false

                    /*(activity as MainActivity).viewModel.getListMain(navigationArgs.id)
                        .observe(viewLifecycleOwner) {
                            allItems = it.size + 1
                        }*/

                    (activity as MainActivity).viewModel.movedItems(recyclerViewAdapter.currentList)
                    (activity as MainActivity).onSupportNavigateUp()
                }
            })
    }


    fun newItem() {
        clearFocus()
        /*val list = recyclerViewAdapter.currentList
        var boolean = true
        for (i in list) {
            if (i.itemName.trim() == "") {
                boolean = false
            }
        }
        if (boolean) {*/
            isAddedNewTask = true // Чтобы в адаптере установить фокус при создании заметки
            (activity as MainActivity).viewModel.addNewItem(
                navigationArgs.id,
                recyclerViewAdapter.currentList.size + 1
            )
      //  }
    }

    override fun updateItem(item: Item) {
        (activity as MainActivity).viewModel.updateItem1(item)
    }

    override fun clearFocus() {
        binding.recyclerView.isFocusable = false
        activity?.currentFocus?.clearFocus()
    }

    override fun requestFocus(view: View) {
        view.requestFocus()
    }

    override fun deleteItem(item: Item, isChecked: Boolean) {
        (activity as MainActivity).viewModel.deleteItem(item, main, isChecked)
    }

    override fun onStartDrag(holder: ItemListAdapter.ViewHolder) {
        isDraggingNow = true
        itemTouchHelper.startDrag(holder)
    }

    override fun updateCheckBox(addOrRemove: Boolean, item: Item) {
        with((activity as MainActivity).viewModel) {
            updateItem1(item, addOrRemove, navigationArgs.id)
        }
    }

    override fun showKeyboard(edittext: EditText) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edittext, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun closeKeyboard(edittext: EditText) {
        if (view != null) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}