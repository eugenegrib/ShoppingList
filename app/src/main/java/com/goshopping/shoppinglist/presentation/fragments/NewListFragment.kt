package com.goshopping.shoppinglist.presentation.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.EditShopListFragmentBinding
import com.goshopping.shoppinglist.domain.MoreFunctions
import com.goshopping.shoppinglist.domain.TaskItemTouchMoveCallback
import com.goshopping.shoppinglist.domain.adapters.ItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.MarkedItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.NewItemAdapter
import com.goshopping.shoppinglist.presentation.MainActivity
import java.util.*


class NewListFragment : Fragment(), MoreFunctions {

    private var _binding: EditShopListFragmentBinding? = null
    internal val binding get() = _binding!!
    lateinit var itemTouchHelper: ItemTouchHelper
    private var isDraggingNow = false
    private lateinit var recyclerViewAdapter: ItemListAdapter
    private lateinit var recyclerMarkedViewAdapter: MarkedItemListAdapter
    private lateinit var newItemAdapter: NewItemAdapter
    private val parentID111 = Random().nextInt()
    var main: MainItem? = null
    var allItems = 0
    var addItemorCategory = false
    override var isAddedNewTask = false // Чтобы добавить фокус новому элементу
    lateinit var sharedPreferences: SharedPreferences


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
        postponeEnterTransition()
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_down)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        when (sharedPreferences.getString("fontSize", "")) {
            "Very small" -> binding.etTitle.textSize = 16F
            "Small" ->  binding.etTitle.textSize = 20F
            "Normal" ->  binding.etTitle.textSize = 24F
            "Big" ->  binding.etTitle.textSize = 28F
            "Very Big" ->  binding.etTitle.textSize = 32F
        }
        /**
         * Переопределяем onBackPressed
         */
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.recyclerView.isFocusable = false
                    with((activity as MainActivity).viewModel) {
                        getListMain(parentID111)
                            .observe(viewLifecycleOwner) {
                                allItems = it.size + 1
                            }
                        setNameMainIfEmpty(parentID111)
                    }
                    (activity as MainActivity).viewModel.movedItems(recyclerViewAdapter.currentList)

                    (activity as MainActivity).onSupportNavigateUp()
                }
            })

        binding.etTitle.requestFocus()

        recyclerViewAdapter = ItemListAdapter(this)
        binding.recyclerView.adapter = recyclerViewAdapter

        itemTouchHelper = ItemTouchHelper(TaskItemTouchMoveCallback(recyclerViewAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        recyclerMarkedViewAdapter = MarkedItemListAdapter( this)

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 400
        itemAnimator.removeDuration = 400
        binding.recyclerView.itemAnimator = itemAnimator

        val itemAnimatorMarked = DefaultItemAnimator()
        itemAnimatorMarked.addDuration = 400
        itemAnimatorMarked.removeDuration = 400


        recyclerViewAdapter = ItemListAdapter(this)
        recyclerMarkedViewAdapter = MarkedItemListAdapter( this)
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

        with(binding) {

            etTitle.onFocusChangeListener = View.OnFocusChangeListener { p0, p1 ->
                if (p1) {
                    etTitle.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun afterTextChanged(p0: Editable?) {
                            etTitle.setSelection(etTitle.length())
                            (activity as MainActivity).viewModel.updateMainName(
                                main!!,
                                etTitle.text.toString()
                            )
                            addItemorCategory
                        }
                    })
                }
            }
        }

        recyclerViewAdapter.onMoved =
            {
                (activity as MainActivity).viewModel.movedItems(recyclerViewAdapter.currentList)
                isDraggingNow = false
            }

        with((activity as MainActivity).viewModel) {
            getCategory(parentID111).observe(viewLifecycleOwner) {
                if (it == null) {
                    (activity as MainActivity).viewModel.addCategory(parentID111)
                } else {
                    main = it
                }
            }
            getParent(parentID111).observe(viewLifecycleOwner) { items ->
                items.let {
                    recyclerViewAdapter.submitList(it)
                }
            }
            newItemAdapter.submitList(listOf("dfg"))

            getParentMarked(parentID111).observe(viewLifecycleOwner) { items ->
                items.let {
                    recyclerMarkedViewAdapter.submitList(it.toMutableList())
                    startPostponedEnterTransition()
                }
            }
        }
    }


    fun newItem() {
        binding.recyclerView.isFocusable = false
        addItemorCategory = true
        isAddedNewTask = true // Чтобы в адаптере установить фокус при создании заметки

        (activity as MainActivity).viewModel.getListMain(parentID111).observe(viewLifecycleOwner) {
            allItems = it.size + 1
        }
        (activity as MainActivity).viewModel.addNewItem(
            parentID111,
            recyclerViewAdapter.currentList.size + 1
        )
    }

    override fun updateItem(item: Item) {
        with((activity as MainActivity).viewModel) {
            updateItem1(item)
        }
    }

    override fun clearFocus() {
        binding.recyclerView.isFocusable = false
        activity?.currentFocus?.clearFocus()
    }

    override fun requestFocus(view: View) {
        view.requestFocus()
    }

    override fun deleteItem(item: Item, isChecked:Boolean) {
        (activity as MainActivity).viewModel.deleteItem(item, main, isChecked)
    }

    override fun onStartDrag(holder: ItemListAdapter.ViewHolder) {
        isDraggingNow = true
        itemTouchHelper.startDrag(holder)
    }

    override fun updateCheckBox(addOrRemove: Boolean, item: Item) {
        with((activity as MainActivity).viewModel) {
            updateItem1(item, addOrRemove, parentID111)
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
}