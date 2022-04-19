package com.goshopping.shoppinglist.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.EditShopListFragmentBinding
import com.goshopping.shoppinglist.domain.*
import com.goshopping.shoppinglist.domain.adapters.ItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.MarkedItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.NewItemAdapter
import com.goshopping.shoppinglist.presentation.MainActivity
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModel
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModelFactory


class EditShopListFragment : Fragment(), MoreFunctions {
    private var _binding: EditShopListFragmentBinding? = null
    private lateinit var recyclerViewAdapter: ItemListAdapter
    private lateinit var recyclerMarkedViewAdapter: MarkedItemListAdapter
    private  lateinit var newItemAdapter: NewItemAdapter
    private lateinit var concatAdapter: ConcatAdapter
    internal val binding get() = _binding!!
    lateinit var itemTouchHelper: ItemTouchHelper
    private var isDraggingNow = false
    private var parentparent: Int = 0
    private val navigationArgs: EditShopListFragmentArgs by navArgs()
    var main: MainItem? = null
    var allItems = 0
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    override var isAddedNewTask = false  // Чтобы добавить фокус новому элементу

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditShopListFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = MainViewModelFactory(
            (activity?.application as StartApplication).mainDatabase.mainItemDao(),
            (activity?.application as StartApplication).database.itemDao(),
            navigationArgs.id
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        val inflater = TransitionInflater.from(activity)
        enterTransition = inflater.inflateTransition(R.transition.slide_down)

        val resId: Int = R.anim.layout_animation_fall_down
        val animation: LayoutAnimationController =
            AnimationUtils.loadLayoutAnimation(activity, resId)

        //binding.cardViewNew.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //binding.markedCardView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        parentparent = navigationArgs.id
        binding.etTitle.setText(navigationArgs.name)

        recyclerViewAdapter = ItemListAdapter(this) { updateItem(it) }
        binding.recyclerView.layoutAnimation = animation

        /* val itemAnimator = DefaultItemAnimator()
         itemAnimator.addDuration = 400
         itemAnimator.removeDuration = 400
         binding.recyclerView.itemAnimator = itemAnimator*/

        val itemAnimatorMarked = DefaultItemAnimator()
        itemAnimatorMarked.addDuration = 400
        itemAnimatorMarked.removeDuration = 400
        //binding.markedRecyclerView.itemAnimator = itemAnimatorMarked


        recyclerMarkedViewAdapter = MarkedItemListAdapter({ updateItem(it) }, this)
        //binding.markedRecyclerView.adapter = recyclerMarkedViewAdapter
        //binding.markedRecyclerView.layoutAnimation = animation
        newItemAdapter = NewItemAdapter(this) { newItem() }
        newItemAdapter.submitList(listOf("dfg"))
        concatAdapter = ConcatAdapter(recyclerViewAdapter,newItemAdapter,recyclerMarkedViewAdapter)
        binding.recyclerView.adapter = concatAdapter

        itemTouchHelper = ItemTouchHelper(TaskItemTouchMoveCallback(recyclerViewAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        /*binding.newItem.setOnClickListener {
            binding.recyclerView.isFocusable = false

            isAddedNewTask = true // Чтобы в адаптере установить фокус при создании заметки
            viewModel.listMain.observe(viewLifecycleOwner) {
                allItems = it.size + 1
            }
            viewModel.addNewItem(parentparent, main!!, recyclerViewAdapter.currentList.size + 1)
        }*/



        binding.etTitle.onFocusChangeListener = View.OnFocusChangeListener { p0, p1 ->
            if (p1) {
                binding.etTitle.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.etTitle.setSelection(binding.etTitle.length())
                        viewModel.updateMainName(main!!, binding.etTitle.text.toString())
                    }
                })
            }
        }

        recyclerViewAdapter.onMoved = {
            viewModel.movedItems(recyclerViewAdapter.currentList)
            isDraggingNow = false
        }

        /**
         * Переопределяем onBackPressed
         */
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.reviewNewFragment(binding.etTitle.text.toString(), allItems)
                    viewModel.movedItems(recyclerViewAdapter.currentList)
                    (activity as MainActivity).onSupportNavigateUp()
                }
            })
        with(viewModel) {
            getCategory.observe(viewLifecycleOwner) {
                main = it
            }
            getParent().observe(viewLifecycleOwner) { items ->
                items.let {
                        recyclerViewAdapter.submitList(it)
                }
            }
            getParentMarked().observe(viewLifecycleOwner) { items ->
                items.let {
                        recyclerMarkedViewAdapter.submitList(it)
                        startPostponedEnterTransition()
                }
            }
        }
    }

    fun newItem(){
        binding.recyclerView.isFocusable = false
        isAddedNewTask = true // Чтобы в адаптере установить фокус при создании заметки
        viewModel.listMain.observe(viewLifecycleOwner) {
            allItems = it.size + 1
        }
        viewModel.addNewItem(parentparent, main!!, recyclerViewAdapter.currentList.size + 1)
    }

    override fun updateItem(item: Item) {
        with(viewModel) {
            updateItem(item)
        }
    }

    override fun clearFocus() {
        binding.recyclerView.isFocusable = false
        activity?.currentFocus?.clearFocus()
    }

    override fun requestFocus(view: View) {
        view.requestFocus()
    }

    override fun deleteItem(item: Item) {
        viewModel.deleteItem(item, main)
    }

    override fun onStartDrag(holder: ItemListAdapter.ViewHolder) {
        isDraggingNow = true
        itemTouchHelper.startDrag(holder)
    }

    override fun updateCheckBox(item: Item) {
        with(viewModel) {
            var list = 0
            getMarkedParent.observe(viewLifecycleOwner) {
                list = it.size
            }
            getCategory.observe(viewLifecycleOwner) {
                updateMarkedItems(list, it)
            }
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