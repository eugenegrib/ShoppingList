package com.goshopping.shoppinglist.domain.adapters

import android.view.*
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goshopping.shoppinglist.R
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.databinding.MainRecyclerViewItemBinding
import com.goshopping.shoppinglist.domain.MoreFunctionsMainItem
import com.goshopping.shoppinglist.domain.TaskMainItemDiffCallback
import java.util.*


/**
 * [ListAdapter] implementation for the recyclerview.
 */

class MainListAdapter(
    private val moreFunctions: MoreFunctionsMainItem,
    private val onItemClicked: (MainItem) -> Unit
) : ListAdapter<MainItem, MainListAdapter.MainViewHolder>(TaskMainItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return createItem(parent)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        var current = getItem(position)
        val binding = MainRecyclerViewItemBinding.bind(holder.view)


        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }

        with(binding) {
            editText.text = current.parentName
            checkandall.text = "${current.checkedItems}/${current.allItems}"

            editText.setOnClickListener{
                onItemClicked(current)
            }
        }
    }

    companion object {
        fun createItem(parent: ViewGroup): MainViewHolder {
            return MainViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_recycler_view_item, parent, false)
            )
        }
    }

    class MainViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}