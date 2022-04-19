package com.goshopping.shoppinglist.presentation.viewModels

import android.os.Handler
import com.goshopping.shoppinglist.data.room.mainScreen.MainItemDao


import androidx.lifecycle.*
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.items.ItemDao
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class MainViewModel(
    private val mainItemDao: MainItemDao,
    private val itemDao: ItemDao, private val parentID: Int
) : ViewModel() {

    var parent: MutableLiveData<Int>? = null

    //запрос списка main
    val allMainItems: LiveData<List<MainItem>> = mainItemDao.getAllItems().asLiveData()

    val getCategory: LiveData<MainItem> = mainItemDao.getCategory(parentID).asLiveData()

    val listMain: LiveData<List<Item>> = itemDao.getAllItems(parentID).asLiveData()

    val getMarkedParent: LiveData<List<Item>> =
        itemDao.getItemsCheckFromParent(parentID).asLiveData()

    //запрос неотмеченнных покупок
    val allItemsNotCheck: MutableLiveData<List<Item>> =
        itemDao.getItemsNotCheck().asLiveData() as MutableLiveData<List<Item>>

    var newPosition = 0


    /**
     *  Устанавливаем значение категории
     */
    fun setParent(parent1: Int) {
        parent?.value = parent1
    }

    fun getParent(): LiveData<List<Item>> {
        return itemDao.getItemsNotCheckFromParent(parentID)
            .asLiveData()
    }

    fun getParentMarked(): LiveData<List<Item>> {
        return itemDao.getItemsCheckFromParent(parentID)
            .asLiveData()
    }

    fun addCategory() {
        viewModelScope.launch {
            mainItemDao.insert(
                MainItem(
                    id = parentID,
                    parentName = "",
                    position = newPosition++,
                    allItems = 0,
                    checkedItems = 0
                )
            )
        }
    }

    /**
     * Обновляем имя категории
     */
    fun updateMainName(main: MainItem, name: String) {
        viewModelScope.launch {
            mainItemDao.update(main.copy(parentName = name))
        }
    }

    /**
     * Функция для заполнения полей покупки в БД
     */
    private fun newItem(
        itemName: String,
        itemCheck: Boolean,
        position: Int,
        parent: Int
    ): Item {
        return Item(
            itemName = itemName,
            itemCheck = itemCheck,
            parent = parent,
            position = position
        )
    }


    /**
     * Добавляем новую покупку
     */
    fun addNewItem(ID: Int, main: MainItem, position: Int) {
        viewModelScope.launch {
            val newItem = newItem(
                "",
                false,
                position = position,
                parent = ID
            )
            itemDao.insert(newItem)

            val new = main.copy(allItems = main.allItems + 1)
            updateCategory(new)
        }
    }

    /**
     * Удаляем покупку по нажатию на крестик
     */
    fun deleteItem(item: Item, main: MainItem?) {
        val new = main!!.copy(allItems = main.allItems - 1)
        updateCategory(new)

        viewModelScope.launch {
            itemDao.delete(item)
            updateCategory(new)
        }
    }


    fun updateMarkedItems(int: Int, main: MainItem) {
        viewModelScope.launch {
            val new = main.copy(checkedItems = int)
            updateCategory(new)
        }
    }

    private fun updateCategory(item: MainItem) {
        viewModelScope.launch {
            mainItemDao.update(item)
        }
    }

    /**
     * Удаляем всю категорию
     */
    fun deleteItemMain(id: Int, main: MainItem) {
        viewModelScope.launch {
            itemDao.deleteItemFromCategory(id)
            mainItemDao.delete(main)
        }
    }


    /**
     * Сохраняем позицию из RecyclerView в БД
     */
    fun movedItems(mutableList: List<Item>) {
        mutableList.forEachIndexed { index, item -> viewModelScope.launch {itemDao.update(item.copy(position = index))}}
    }


    /**
     * Обновляем покупку, если изменилось имя или покупка совершена
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }


    /**
     * Удаляем пустую заметку, если нет названия категории и ни одной покупки
     */
    fun reviewNewFragment(etTitle: String, allItems: Int) {
        if ((etTitle == "" || etTitle == " ") && allItems == 0) {
            deleteMainItem(getCategory.value!!)
        }
    }

    /**
     * Во время открытия главного фрагмента, просим предоставить нам список всех покупок и
     * извлекаем из каждой покупки ее категорию, затем запрашиваем уже имеющийся список категорий.
     * После этого сравниваем два списка и проверям, все ли категории вынесены в отдельную БД,
     * и если не все, то создаем новую категорию.
     */
    fun createMainItemsList(listParents: List<Item>, listMainParent: List<MainItem>) {
        //список категорий из полей БД покупок
        val listShopCategory: MutableList<Int> = mutableListOf()
        for (i in listParents) {
            if (!listShopCategory.contains(i.parent)) {
                listShopCategory.add(i.parent)
            }
        }
        //список категорий из БД категорий
        val listMain: MutableList<Int> = mutableListOf()
        for (i in listMainParent) {
            if (!listMain.contains(i.id)) {
                listMain.add(i.id)
            }
        }
    }

    /**
     * Удаляем категорию
     */
    fun deleteMainItem(item: MainItem) {
        viewModelScope.launch {
            mainItemDao.delete(item)
        }
    }
}

class MainViewModelFactory(
    private val mainItemDao: MainItemDao,
    private val itemDao: ItemDao,
    private val parentID: Int
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(mainItemDao, itemDao, parentID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}