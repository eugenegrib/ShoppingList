package com.goshopping.shoppinglist.presentation.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.NewListFragmentBinding
import com.goshopping.shoppinglist.domain.MoreFunctions
import com.goshopping.shoppinglist.domain.StartApplication
import com.goshopping.shoppinglist.domain.TaskItemTouchMoveCallback
import com.goshopping.shoppinglist.domain.adapters.ItemListAdapter
import com.goshopping.shoppinglist.domain.adapters.MarkedItemListAdapter
import com.goshopping.shoppinglist.presentation.MainActivity
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModel
import com.goshopping.shoppinglist.presentation.viewModels.MainViewModelFactory
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NewListFragment : Fragment(), MoreFunctions {

    private var param1: String? = null
    private var param2: String? = null
    private var _binding: NewListFragmentBinding? = null
    internal val binding get() = _binding!!
    lateinit var itemTouchHelper: ItemTouchHelper
    private var isDraggingNow = false
    private lateinit var recyclerViewAdapter: ItemListAdapter
    private lateinit var recyclerMarkedViewAdapter: MarkedItemListAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private val parentID = Random().nextInt()
    var main: MainItem? = null
    var allItems = 0
    var addItemorCategory = false
    override var isAddedNewTask = false // Чтобы добавить фокус новому элементу
    var idfromAdapter:Item? = null
    var textText = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewListFragmentBinding.inflate(inflater, container, false)
        viewModelFactory = MainViewModelFactory(
            (activity?.application as StartApplication).mainDatabase.mainItemDao(),
            (activity?.application as StartApplication).database.itemDao(), parentID
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_down)

        binding.cardViewNew.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.markedCardView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        /**
         * Переопределяем onBackPressed
         */
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.movedItems(recyclerViewAdapter.currentList)
                    viewModel.reviewNewFragment(binding.etTitle.text.toString(), allItems)
                    (activity as MainActivity).onSupportNavigateUp()
                }
            })

        binding.etTitle.requestFocus()

        recyclerViewAdapter = ItemListAdapter(this) { updateItem(it) }
        binding.recyclerView.adapter = recyclerViewAdapter

        itemTouchHelper = ItemTouchHelper(TaskItemTouchMoveCallback(recyclerViewAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        recyclerMarkedViewAdapter = MarkedItemListAdapter({ updateItem(it) }, this)
        binding.markedRecyclerView.adapter = recyclerMarkedViewAdapter

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 400
        itemAnimator.removeDuration = 400
        binding.recyclerView.itemAnimator = itemAnimator

        val itemAnimatorMarked = DefaultItemAnimator()
        itemAnimatorMarked.addDuration = 400
        itemAnimatorMarked.removeDuration = 400
        binding.markedRecyclerView.itemAnimator = itemAnimatorMarked
        
        with(binding) {

            newItem.setOnClickListener {
                binding.recyclerView.isFocusable = false
                addItemorCategory = true
                isAddedNewTask = true // Чтобы в адаптере установить фокус при создании заметки

                    viewModel.listMain.observe(viewLifecycleOwner) {
                        allItems = it.size + 1
                    }
                    viewModel.addNewItem(parentID, main!!, recyclerViewAdapter.currentList.size+1)

            }

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
                            viewModel.updateMainName(main!!, etTitle.text.toString())
                            addItemorCategory
                        }
                    })
                }
            }
        }

        recyclerViewAdapter.onMoved =
            { viewModel.movedItems(recyclerViewAdapter.currentList)
                isDraggingNow = false
            }

        with(viewModel) {
            getCategory.observe(viewLifecycleOwner) {
                if (it == null) {
                    viewModel.addCategory()
                } else {
                    main = it
                }
            }
            setParent(parentID)

            getParent().observe(viewLifecycleOwner) { items ->
                items.let {
                    recyclerViewAdapter.submitList(it)
                }
            }

            getParentMarked().observe(viewLifecycleOwner) { items ->
                items.let {
                    recyclerMarkedViewAdapter.submitList(it.toMutableList())
                    startPostponedEnterTransition()

                }
            }
        }
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
                val main = it
                updateMarkedItems(list, main)
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



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}